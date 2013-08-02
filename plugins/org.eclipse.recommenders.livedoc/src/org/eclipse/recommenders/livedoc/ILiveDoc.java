package org.eclipse.recommenders.livedoc;

import java.io.File;
import java.util.List;

import com.sun.tools.doclets.Taglet;

public interface ILiveDoc {
    
    boolean generate();
    void setVerbose(boolean verbose);
    boolean getVerbose();
    void setTaglets(List<Taglet> taglets);
    List<Taglet> getTaglets();
    void setOutputDir(File dir);
    File getOutputDir();
    void setSourceDir(File sourcePath);
    File getSourceDir();
}
