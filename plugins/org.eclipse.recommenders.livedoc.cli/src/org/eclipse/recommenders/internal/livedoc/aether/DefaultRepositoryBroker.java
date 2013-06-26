package org.eclipse.recommenders.internal.livedoc.aether;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newLinkedList;
import static org.eclipse.recommenders.utils.Checks.ensureIsDirectory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader;
import org.apache.maven.repository.internal.DefaultVersionRangeResolver;
import org.apache.maven.repository.internal.DefaultVersionResolver;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.recommenders.livedoc.aether.IRepositoryBroker;
import org.eclipse.recommenders.livedoc.aether.RepositoryDescriptor;
import org.eclipse.recommenders.utils.Checks;
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

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class DefaultRepositoryBroker implements IRepositoryBroker {

    private static final String REPOSITORY_LAYOUT = "default";
    private static final Logger log = LoggerFactory.getLogger(DefaultRepositoryBroker.class);

    private final DefaultProviderSelectionStrategy providerSelectionStrategy = new DefaultProviderSelectionStrategy();

    private final File cacheDir;
    private final File indexDir;
    private final RepositorySystem repositorySystem;

    public DefaultRepositoryBroker(File cacheDir, File indexDir) throws IOException {
        this.cacheDir = ensureIsDirectory(cacheDir);
        this.indexDir = ensureIsDirectory(indexDir);
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
            return result.setRepositories(Collections.<RemoteRepository> emptyList());
        }
    }

    @Override
    public boolean exists(Artifact coordinate, RepositoryDescriptor repo, IProgressMonitor monitor) {
        IRepositoryContentsProvider contentsProvider = providerSelectionStrategy.select(repo, indexDir);
        List<Artifact> artifacts = contentsProvider.listMatchingArtifacts(coordinate, new NullProgressMonitor(),
                coordinate.getExtension());

        if (artifacts.isEmpty())
            return false;

        return true;
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
    public List<Artifact> download(Collection<Artifact> coordinates, RepositoryDescriptor repo, IProgressMonitor monitor)
            throws ArtifactResolutionException {
        try {
            monitor.beginTask("downloadArtifacts", coordinates.size() * 100);
            List<Artifact> artifacts = Lists.newArrayListWithCapacity(coordinates.size());
            for (Artifact coordinate : coordinates)
                artifacts.add(download(coordinate, repo, new SubProgressMonitor(monitor, 100)));
            return artifacts;
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

    @Override
    public List<Artifact> listPrimaryArtifacts(RepositoryDescriptor repo, IProgressMonitor monitor,
            String... extensions) {
        Checks.ensureIsTrue(extensions.length > 0);
        IRepositoryContentsProvider contentsProvider = providerSelectionStrategy.select(repo, indexDir);
        List<Artifact> artifacts = contentsProvider.listPrimaryArtifacts(monitor, extensions);
        IOUtils.closeQuietly(contentsProvider);
        return artifacts;
    }

    @Override
    public List<Artifact> listSecondaryArtifacts(RepositoryDescriptor repo, IProgressMonitor monitor,
            String classifier, String... extensions) {
        Checks.ensureIsTrue(extensions.length > 0);
        IRepositoryContentsProvider contentsProvider = providerSelectionStrategy.select(repo, indexDir);
        List<Artifact> artifacts = contentsProvider.listSecondaryArtifacts(monitor, classifier, extensions);
        IOUtils.closeQuietly(contentsProvider);
        return artifacts;
    }

    @Override
    public List<Artifact> listAllArtifacts(RepositoryDescriptor repo, IProgressMonitor monitor) {
        IRepositoryContentsProvider contentsProvider = providerSelectionStrategy.select(repo, indexDir);
        List<Artifact> artifacts = contentsProvider.listAllArtifacts(monitor);
        IOUtils.closeQuietly(contentsProvider);
        return artifacts;
    }
    
    public List<Artifact> listArtifactsForGroupId(RepositoryDescriptor repo, final String groupId, IProgressMonitor monitor){
        List<Artifact> allCoordinates = listAllArtifacts(repo, monitor);
        
        List<Artifact> matchingCoordinates = newLinkedList(filter(allCoordinates, new Predicate<Artifact>() {

            @Override
            public boolean apply(Artifact artifact) {
                return (artifact.getGroupId().equals(groupId));
            }
        }));
        
        return matchingCoordinates;
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

    @Override
    public void ensureIndexUpToDate(RepositoryDescriptor repo, IProgressMonitor monitor) throws Exception {
        IRepositoryContentsProvider contentsProvider = providerSelectionStrategy.select(repo, indexDir);
        contentsProvider.index(monitor);
    }
}
