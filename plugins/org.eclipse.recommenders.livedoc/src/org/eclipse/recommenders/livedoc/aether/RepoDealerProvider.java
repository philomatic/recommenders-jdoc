package org.eclipse.recommenders.livedoc.aether;

import java.io.File;
import java.io.IOException;

import org.eclipse.recommenders.internal.livedoc.aether.RepositoryClient;

public class RepoDealerProvider {

    /**
     * @param cacheDir 
     * @param indexDir 
     * @throws IOException 
     * 
     */
    public static IRepositoryClient create(File cacheDir) throws IOException{
        // TODO: so bad, guice over plugins maybe?
        return new RepositoryClient(cacheDir);
    }
}
