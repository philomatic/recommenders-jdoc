package org.eclipse.recommenders.livedoc.aether;

import java.net.URL;

public class RepositoryDescriptor {

    private final String id;

    private final URL url;

    /**
     * @param id
     *            a short, human-readable identifier for the repository (e.g., <code>central</code>)
     * @param url
     *            the repository's URL (e.g., <code>http://repo1.maven.org/maven2/</code>)
     */
    public RepositoryDescriptor(String id, URL url) {
        this.id = id;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return id + " <" + url + ">";
    }
}
