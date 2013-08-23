package org.eclipse.recommenders.internal.livedoc.aether;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.sonatype.aether.artifact.Artifact;

public interface IRepositoryContentsProvider extends Closeable {

    List<Artifact> listPrimaryArtifacts(IProgressMonitor monitor, String... extensions);

    List<Artifact> listSecondaryArtifacts(IProgressMonitor monitor, String classifier, String... extensions);

    List<Artifact> listAllArtifacts(IProgressMonitor monitor);

    List<Artifact> listMatchingArtifacts(Artifact coordinate, IProgressMonitor monitor, String... extensions);

    void close() throws IOException;

    void index(IProgressMonitor monitor) throws Exception;
}
