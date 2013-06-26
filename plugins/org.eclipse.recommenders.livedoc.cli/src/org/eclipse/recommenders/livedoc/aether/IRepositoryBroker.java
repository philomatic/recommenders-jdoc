package org.eclipse.recommenders.livedoc.aether;


import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.artifact.Artifact;


/**
 * Facade to interact with remote repositories. A single repository broker can manage any number of remote repositories.
 * All remote repositories are identified by a URL (e.g., <code>http://</code>, <code>ftp://</code>, or
 * <code>file:</code>).
 * 
 * The broker furthermore manages a local working directory, which must be a location different from all remote
 * repository URLs, even if they are identified by a <code>file:</code>-based URL.
 */
public interface IRepositoryBroker {

    /**
     * Checks whether an artifact is present in the given remote repository.
     * 
     * This is an index-based operation, so exists() should only be called when the index is up-to-date.
     * 
     * @see index(RepositoryDescriptor, IProgressMonitor)
     */
    boolean exists(Artifact coordinate, RepositoryDescriptor repo, IProgressMonitor monitor);

    /**
     * Downloads (<em>resolves</em>) an artifact present in the given remote repository to the local working directory.
     * 
     * Note that this method does not download the artifact's (classpath) dependencies. Use
     * {@link #resolveClasspathDependencies(Artifact, RepositoryDescriptor, IProgressMonitor)} for this.
     * 
     * @return the resolved artifact. This artifact has its file property set to a local file in the working directory.
     */
    Artifact download(Artifact coordinate, RepositoryDescriptor repo, IProgressMonitor monitor)
            throws RepositoryException;

    /**
     * Downloads (<em>resolves</em>) multiple artifacts present in the given remote repository to the local working
     * directory.
     * 
     * This is a convenience method.
     * 
     * @see IRepositoryBroker#download(Artifact, RepositoryDescriptor, IProgressMonitor)
     * @return the resolved artifacts. These artifacts haves their file property set to a local file in the working
     *         directory.
     */
    List<Artifact> download(Collection<Artifact> coordinates, RepositoryDescriptor repo, IProgressMonitor monitor)
            throws RepositoryException;

    /**
     * Uploads (<em>deploys</em>) a local artifact to the given remote repository.
     * 
     * @param artifact
     *            the artifact to deploy. This artifact must have its file property set.
     */
    void upload(Artifact artifact, RepositoryDescriptor repo, IProgressMonitor monitor) throws RepositoryException;

    /**
     * Lists the primary artifacts the given remote repository.
     * 
     * Depending on the value of {@link RepositoryDescriptor#isIndexUpdateEnabled()}, calling this method may be
     * <strong>costly</strong>, as it triggers an index update.
     * 
     * @param extensions
     *            the artifacts' extensions (at least one). Note that an artifact's extension may be different from its
     *            project's packaging.
     */
    List<Artifact> listPrimaryArtifacts(RepositoryDescriptor repo, IProgressMonitor monitor, String... extensions);

    /**
     * Lists the artifacts with the specified classifier of the given remote repository.
     * 
     * Depending on the value of {@link RepositoryDescriptor#isIndexUpdateEnabled()}, calling this method may be
     * <strong>costly</strong>, as it triggers an index update.
     * 
     * @param classifier
     *            the secondary artifacts' classifier.
     * @param extensions
     *            the artifacts' extensions (at least one). Note that an artifact's extension may be different from its
     *            project's packaging.
     */
    List<Artifact> listSecondaryArtifacts(RepositoryDescriptor repo, IProgressMonitor monitor, String classifier,
            String... extensions);

    /**
     * Lists the contents of the given remote repository. This includes all primary and secondary artifacts with any
     * extension.
     * 
     * Depending on the value of {@link RepositoryDescriptor#isIndexUpdateEnabled()}, calling this method may be
     * <strong>costly</strong>, as it triggers an index update.
     */
    List<Artifact> listAllArtifacts(RepositoryDescriptor repo, IProgressMonitor monitor);

    /**
     * Creates an index or updates an existing one for the given repository.
     * 
     * @param repo
     * @param monitor
     * @throws Exception
     */
    void ensureIndexUpToDate(RepositoryDescriptor repo, IProgressMonitor monitor) throws Exception;
    
    /**
     * TODO: Javadoc
     * @param repo
     * @param groupId
     * @param monitor
     * @return
     */
    public List<Artifact> listArtifactsForGroupId(RepositoryDescriptor repo, final String groupId, IProgressMonitor monitor);
}
