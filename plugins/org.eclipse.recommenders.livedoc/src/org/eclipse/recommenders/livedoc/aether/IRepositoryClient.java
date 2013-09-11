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

package org.eclipse.recommenders.livedoc.aether;


import org.eclipse.core.runtime.IProgressMonitor;
import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.artifact.Artifact;


public interface IRepositoryClient {

    /**
     * Downloads (<em>resolves</em>) an artifact present in the given remote repository to the local working directory.
     * 
     * 
     * @return the resolved artifact. This artifact has its file property set to a local file in the working directory.
     */
    Artifact download(Artifact coordinate, RepositoryDescriptor repo, IProgressMonitor monitor)
            throws RepositoryException;

    /**
     * Uploads (<em>deploys</em>) a local artifact to the given remote repository.
     * 
     * @param artifact
     *            the artifact to deploy. This artifact must have its file property set.
     */
    void upload(Artifact artifact, RepositoryDescriptor repo, IProgressMonitor monitor) throws RepositoryException;
}
