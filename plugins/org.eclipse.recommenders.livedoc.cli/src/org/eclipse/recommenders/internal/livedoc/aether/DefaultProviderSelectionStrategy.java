package org.eclipse.recommenders.internal.livedoc.aether;

import java.io.File;
import java.net.URL;

import org.eclipse.recommenders.livedoc.aether.RepositoryDescriptor;
import org.eclipse.recommenders.utils.Throws;


public class DefaultProviderSelectionStrategy {

    public IRepositoryContentsProvider select(RepositoryDescriptor repo, File indexBasedir) {
        try {
            URL url = repo.getUrl();
            if ("file".equals(url.getProtocol())) {
                File local = new File(url.toURI());
                return new FilesystemBasedRepositoryContentsProvider(local, indexBasedir);
            } else {
                return new RemoteRepositoryContentsProvider(url, indexBasedir);
            }
        } catch (Exception e) {
            throw Throws.throwIllegalStateException("Failed to create content provider", e);
        }
    }
}
