package org.eclipse.recommenders.livedoc;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.recommenders.livedoc.javadoc.RecommendersDoclet;

import com.google.common.collect.Lists;

/**
 * @recommenders.test
 * 
 * 
 */
public class LiveDoc implements ILiveDoc {
    
    private boolean verbose;
    private File sourceDir;
    private File outputDir;
    private List<String> subpackages;
    private String groupId;
    private String artifactId;
    private String artifactVersion;
    private URL modelsRepo;
    private String[] selectedTaglets;
    
    public LiveDoc(boolean verbose, File sourceFiles, File outputDir, List<String> subpackages) {
        super();
        this.verbose = verbose;
        this.sourceDir = sourceFiles;
        this.outputDir = outputDir;
        this.subpackages = subpackages;
    }

    /**
     * @recommenders.test
     * 
     */
    public boolean generate() {
        
        // set system properties for groupId, artifactId, artifactVersion
        
        if (System.getProperty("recommenders.livedoc.groupId") == null) {
            System.setProperty("recommenders.livedoc.groupId", groupId);
        }
        if (System.getProperty("recommenders.livedoc.artifactId") == null) {
            System.setProperty("recommenders.livedoc.artifactId", artifactId);
        }
        if (System.getProperty("recommenders.livedoc.artifactVersion") == null) {
            System.setProperty("recommenders.livedoc.artifactVersion", artifactVersion);
        }
        
        if (System.getProperty("recommenders.livedoc.modelsRepo") == null) {
            System.setProperty("recommenders.livedoc.modelsRepo", modelsRepo.toExternalForm());
        }
        
        RecommendersDoclet.instance().getTagletManager().setSelectedTaglets(selectedTaglets);
        
        @SuppressWarnings("unused")
        int returnCode = com.sun.tools.javadoc.Main.execute(this.getClass().getClassLoader(), buildArgs());
        // returnCode handling
        return true;
    }

    private String[] buildArgs() {
        
        ArrayList<String> javadocArgs = Lists.newArrayList();
        
        if (verbose){
            javadocArgs.add("-verbose");
        }else{
            javadocArgs.add("-quiet");
        }
        
        javadocArgs.add("-d");
        javadocArgs.add(getOutputDir().getAbsolutePath());
        
        
        javadocArgs.add("-doclet");
        javadocArgs.add("org.eclipse.recommenders.livedoc.javadoc.RecommendersDoclet");
        
        
        javadocArgs.add("-subpackages");
        
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = subpackages.iterator();
        
        while (it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(":");
            }
        }
        javadocArgs.add(sb.toString());
        
        javadocArgs.add("-private");
        javadocArgs.add("-sourcepath");
        javadocArgs.add(sourceDir.getAbsolutePath());

        return javadocArgs.toArray(new String[0]);
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public boolean getVerbose() {
        return verbose; 
    }

    @Override
    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    @Override
    public File getOutputDir() {
        return outputDir;
    }

    @Override
    public void setSourceDir(File sourceDir) {
        this.sourceDir = sourceDir;
    }

    @Override
    public File getSourceDir() {
        return this.sourceDir;
    }

    public List<String> getSubpackages() {
        return subpackages;
    }

    public void setSubpackages(List<String> subpackages) {
        this.subpackages = subpackages;
    }

    @Override
    public void setGroupId(String groupId) {
        this.groupId = groupId;
        
    }

    @Override
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        
    }

    @Override
    public void setArtifactVersion(String version) {
        this.artifactVersion = version;
        
    }

    @Override
    public void setModelsRepo(URL modelsRepo) {
        this.modelsRepo = modelsRepo;
        
        
    }

    @Override
    public void setSelectedTaglets(String[] taglets) {
        this.selectedTaglets = taglets;
    }
}
