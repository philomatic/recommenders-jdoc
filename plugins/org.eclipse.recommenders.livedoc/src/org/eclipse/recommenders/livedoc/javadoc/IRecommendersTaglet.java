package org.eclipse.recommenders.livedoc.javadoc;

import java.net.URL;

import com.sun.tools.doclets.internal.toolkit.taglets.Taglet;

public interface IRecommendersTaglet extends Taglet {
    
    void setGroupId(String groupId);
    
    void setArtifactId(String artifactId);
    
    void setArtifactVersion(String version);

    /**
     * Do everthing necessary to get ready for documentation output,
     * e.g. download models etc. 
     * @throws Exception 
     */
    void initialize();
    
    /**
     * Do everthing necessary when this taglet is finished (e.g. close streams etc.)
     */
    void finish();

    void setModelsRepo(URL modelsRepo);

}
