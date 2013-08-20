package org.eclipse.recommenders.livedoc.javadoc;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;


public class PluginTagletManager implements ICustomTagletManager {
    

    private String[] selectedTaglets;

    @Override
    public List<RecommendersTaglet> getCustomTaglets() {

        IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
                "org.eclipse.recommenders.livedoc.taglets");
 
        ArrayList<IConfigurationElement> filteredElements = new ArrayList<IConfigurationElement>(elements.length);
        
        if (selectedTaglets != null){
            for (IConfigurationElement element : elements) {
                for (int i = 0; i < selectedTaglets.length; i++) {
                    if (element.getAttribute("id").equals(selectedTaglets[i])){
                        filteredElements.add(element);
                    }
                }
            }
            return createTaglets(filteredElements);
        }
        return createTaglets(Arrays.asList(elements));
    }

    private List<RecommendersTaglet> createTaglets(List<IConfigurationElement> elements) {
        
        List<RecommendersTaglet> taglets = new LinkedList<RecommendersTaglet>();
        String groupId = System.getProperty("recommenders.livedoc.groupId");
        String artifactId = System.getProperty("recommenders.livedoc.artifactId");
        String artifactVersion = System.getProperty("recommenders.livedoc.artifactVersion");
        URL modelsRepo = null;
        try {
            modelsRepo = new URL(System.getProperty("recommenders.livedoc.modelsRepo"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        
        for (IConfigurationElement element : elements) {
            try {
                RecommendersTaglet taglet = (RecommendersTaglet) element.createExecutableExtension("taglet");
                
                taglet.setGroupId(groupId);
                taglet.setArtifactId(artifactId);
                taglet.setArtifactVersion(artifactVersion);
                taglet.setModelsRepo(modelsRepo);

                taglets.add(taglet);
            } catch (CoreException e) {
                e.printStackTrace();
            }

        }
        Collections.sort(taglets);
        return taglets;
    }

    @Override
    public void setSelectedTaglets(String[] taglets) {
        this.selectedTaglets = taglets;
    }

}
