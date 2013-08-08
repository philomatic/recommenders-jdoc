package org.eclipse.recommenders.livedoc.javadoc;

import java.util.List;

import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.formats.html.ConfigurationImpl;
import com.sun.tools.doclets.formats.html.HtmlDoclet;
import com.sun.tools.doclets.internal.toolkit.Configuration;
import com.sun.tools.doclets.standard.Standard;

public class RecommendersDoclet extends Standard{

    private static HtmlDoclet doclet;
    private static RecommendersDoclet instance;
    private ICustomTagletManager manager;
    private List<IRecommendersTaglet> customTaglets;
    
    public RecommendersDoclet() {
        manager = new PluginTagletManager();
    }

    /**
     * We want to use the standard HtmlDoclet, but call setOptions() first to
     * make sure that doclet.configuration.tagletManager != null
     * for adding custom taglets on this TagletManager.
     **/
    public static boolean start(RootDoc root) {
        try {
            doclet = new HtmlDoclet();
            instance = new RecommendersDoclet();
            
            // root shouldn't be null for setOptions()
            doclet.configuration.root = root;
            doclet.configuration.setOptions();
            
            // load custom taglets via extension point
            instance.configureCustomTaglets();
            return doclet.start(doclet, root);
        } finally {
            ConfigurationImpl.reset();
            instance.closeTaglets();
        }
    }
    

    private void closeTaglets() {
        for (IRecommendersTaglet taglet : customTaglets) {
            taglet.finish();
        }
    }

    private void configureCustomTaglets() {

        Configuration conf = doclet.configuration;
        customTaglets = manager.getCustomTaglets();

        for (IRecommendersTaglet taglet : customTaglets) {
            
            taglet.initialize();
            conf.tagletManager.addCustomTag(taglet);
        }
    }
}
