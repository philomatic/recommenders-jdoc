package org.eclipse.recommenders.livedoc.aether;

import java.io.File;
import java.io.IOException;

import org.eclipse.recommenders.internal.livedoc.aether.DefaultRepositoryBroker;

public class RepoBrokerProvider {

    /**
     * @param cacheDir 
     * @param indexDir 
     * @throws IOException 
     * 
     */
    public static IRepositoryBroker create(File cacheDir, File indexDir) throws IOException{
        // TODO: so bad, guice over plugins maybe?
        return new DefaultRepositoryBroker(cacheDir, indexDir);
    }
}
