package org.eclipse.recommenders.livedoc.utils;

import java.io.File;
import java.io.IOException;

import org.eclipse.recommenders.internal.livedoc.aether.DefaultRepositoryBroker;
import org.eclipse.recommenders.livedoc.aether.IRepositoryBroker;

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
