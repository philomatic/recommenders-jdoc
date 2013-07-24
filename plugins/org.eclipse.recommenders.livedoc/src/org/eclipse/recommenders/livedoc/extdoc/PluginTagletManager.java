package org.eclipse.recommenders.livedoc.extdoc;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import com.sun.tools.doclets.internal.toolkit.taglets.Taglet;


public class PluginTagletManager implements ICustomTagletManager {

    @Override
    public List<Taglet> getCustomTaglets() {

        IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
                "org.eclipse.recommenders.livedoc.customTaglets");
        List<Taglet> taglets = new LinkedList<Taglet>();

        for (IConfigurationElement element : elements) {
            try {
                Taglet taglet = (Taglet) element.createExecutableExtension("customTaglet");
                taglets.add(taglet);
            } catch (CoreException e) {
                e.printStackTrace();
            }

        }

        return taglets;
    }

}
