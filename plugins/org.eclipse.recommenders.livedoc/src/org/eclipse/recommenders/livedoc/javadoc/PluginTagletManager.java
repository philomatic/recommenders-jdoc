package org.eclipse.recommenders.livedoc.javadoc;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import com.sun.tools.doclets.internal.toolkit.taglets.Taglet;


public class PluginTagletManager implements ICustomTagletManager {

    @Override
    public List<IRecommendersTaglet> getCustomTaglets() {

        IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
                "org.eclipse.recommenders.livedoc.taglets");
        List<IRecommendersTaglet> taglets = new LinkedList<IRecommendersTaglet>();

        for (IConfigurationElement element : elements) {
            try {
                IRecommendersTaglet taglet = (IRecommendersTaglet) element.createExecutableExtension("taglet");
                
                taglet.setGroupId(System.getProperty("recommenders.livedoc.groupId"));
                taglet.setArtifactId(System.getProperty("recommenders.livedoc.artifactId"));
                taglet.setArtifactVersion(System.getProperty("recommenders.livedoc.artifactVersion"));
                
                URL modelsRepo = null;
                try {
                    modelsRepo = new URL(System.getProperty("recommenders.livedoc.modelsRepo"));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                taglet.setModelsRepo(modelsRepo);
                
                taglets.add(taglet);
            } catch (CoreException e) {
                e.printStackTrace();
            }

        }

        return taglets;
    }

}
