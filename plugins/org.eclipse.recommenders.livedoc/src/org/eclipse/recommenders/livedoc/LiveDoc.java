package org.eclipse.recommenders.livedoc;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;
import com.sun.tools.doclets.Taglet;

/**
 * @recommenders.test
 * 
 * 
 */
public class LiveDoc implements ILiveDoc {
    
    private boolean verbose;
    private File sourceDir;
    private File outputDir;
    private List<Taglet> taglets;
    private List<String> subpackages;
    
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
        javadocArgs.add("org.eclipse.recommenders.livedoc.extdoc.RecommendersDoclet");
        
        
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
    public void setTaglets(List<Taglet> taglets) {
        this.taglets = taglets;
    }

    @Override
    public List<Taglet> getTaglets() {
        return taglets;
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
}
