package org.eclipse.recommenders.livedoc.javadoc;

import java.io.File;
import java.util.List;

import com.sun.tools.doclets.Taglet;

public interface ILiveDoc {
    
    public boolean generate();
    public void setVerbose(boolean verbose);
    public boolean getVerbose();
    public void setTaglets(List<Taglet> taglets);
    public List<Taglet> getTaglets();
    public void setOutputDir(File dir);
    public File getOutputDir();
    public void setSourceDir(File sourcePath);
    public File getSourceDir();
}
