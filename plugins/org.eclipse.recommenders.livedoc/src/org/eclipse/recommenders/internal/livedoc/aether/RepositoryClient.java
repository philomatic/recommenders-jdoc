/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */

package org.eclipse.recommenders.internal.livedoc.aether;

import static org.eclipse.recommenders.utils.Checks.ensureIsDirectory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader;
import org.apache.maven.repository.internal.DefaultVersionRangeResolver;
import org.apache.maven.repository.internal.DefaultVersionResolver;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.recommenders.livedoc.aether.IRepositoryClient;
import org.eclipse.recommenders.livedoc.aether.RepositoryDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.deployment.DeployRequest;
import org.sonatype.aether.deployment.DeploymentException;
import org.sonatype.aether.impl.ArtifactDescriptorReader;
import org.sonatype.aether.impl.VersionRangeResolver;
import org.sonatype.aether.impl.VersionResolver;
import org.sonatype.aether.impl.internal.DefaultServiceLocator;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.installation.InstallationException;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.aether.resolution.ArtifactDescriptorRequest;
import org.sonatype.aether.resolution.ArtifactDescriptorResult;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.util.DefaultRepositorySystemSession;

public class RepositoryClient implements IRepositoryClient {

    private static final String REPOSITORY_LAYOUT = "default";
    private static final Logger log = LoggerFactory.getLogger(RepositoryClient.class);

    private final File cacheDir;
    private final RepositorySystem repositorySystem;

    public RepositoryClient(File cacheDir) throws IOException {
        this.cacheDir = ensureIsDirectory(cacheDir);
        repositorySystem = createRepositorySystem();
    }

    private RepositorySystem createRepositorySystem() {
        DefaultServiceLocator locator = new DefaultServiceLocator();
        locator.setServices(WagonProvider.class, new AhcWagonProvider());
        locator.addService(RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class);
        locator.addService(VersionResolver.class, DefaultVersionResolver.class);
        locator.addService(VersionRangeResolver.class, DefaultVersionRangeResolver.class);
        locator.addService(ArtifactDescriptorReader.class, IgnoreRepositoriesInPomArtifactDescriptorReader.class);
        return locator.getService(RepositorySystem.class);
    }

    /**
     * By default, repositories configured in an artifact's POM and ancestor POMs are consulted during dependency
     * resolution. This is undesirable in our case, as it leads to network traffic to an repository which the user has
     * not explicitly configured.
     * 
     * @see DefaultArtifactDescriptorReader
     */
    public static class IgnoreRepositoriesInPomArtifactDescriptorReader extends DefaultArtifactDescriptorReader {

        @Override
        public ArtifactDescriptorResult readArtifactDescriptor(RepositorySystemSession session,
                ArtifactDescriptorRequest request) throws ArtifactDescriptorException {
            ArtifactDescriptorResult result = super.readArtifactDescriptor(session, request);
            return result.setRepositories(Collections.<RemoteRepository>emptyList());
        }
    }

    @Override
    public Artifact download(Artifact coordinate, RepositoryDescriptor repo, IProgressMonitor monitor)
            throws ArtifactResolutionException {
        try {
            monitor.beginTask("downloadArtifact", IProgressMonitor.UNKNOWN);
            RepositorySystemSession session = newRepositorySystemSession(new SubProgressMonitor(monitor,
                    IProgressMonitor.UNKNOWN));
            List<RemoteRepository> repos = Collections.singletonList(toRemoteRepository(repo));
            ArtifactRequest request = new ArtifactRequest(coordinate, repos, null);
            ArtifactResult result = repositorySystem.resolveArtifact(session, request);
            return result.getArtifact();
        } finally {
            monitor.done();
        }
    }

    @Override
    public void upload(Artifact artifact, RepositoryDescriptor repo, IProgressMonitor monitor)
            throws InstallationException, DeploymentException {
        try {
            monitor.beginTask("uploadArtifact", (int) artifact.getFile().length());
            RepositorySystemSession session = newRepositorySystemSession(new NullProgressMonitor());

            // Add to cache...
            InstallRequest installRequest = new InstallRequest();
            installRequest.addArtifact(artifact);
            repositorySystem.install(session, installRequest);

            // ...and write-through
            DeployRequest deployRequest = new DeployRequest();
            deployRequest.addArtifact(artifact);
            deployRequest.setRepository(toRemoteRepository(repo));
            repositorySystem.deploy(session, deployRequest);
        } finally {
            monitor.done();
        }
    }

    private DefaultRepositorySystemSession newRepositorySystemSession(IProgressMonitor monitor) {
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();
        LocalRepository localRepo = new LocalRepository(cacheDir);
        session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(localRepo));
        session.setTransferListener(new TransferListener(monitor));
        return session;
    }

    private RemoteRepository toRemoteRepository(RepositoryDescriptor repo) {
        return new RemoteRepository(repo.getId(), REPOSITORY_LAYOUT, repo.getUrl().toExternalForm());
    }
}
