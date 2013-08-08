package org.eclipse.recommenders.livedoc;

import java.io.File;
import java.net.URL;
import java.util.List;

import com.sun.tools.doclets.Taglet;

public interface ILiveDoc {
    

    boolean generate();
    void setVerbose(boolean verbose);
    boolean getVerbose();
    void setOutputDir(File dir);
    File getOutputDir();
    void setSourceDir(File sourcePath);
    File getSourceDir();
    void setGroupId(String groupId);
    void setArtifactId(String artifactId);
    void setArtifactVersion(String version);
    void setModelsRepo(URL modelsRepo);
}
