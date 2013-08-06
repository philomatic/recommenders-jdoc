package org.eclipse.recommenders.livedoc.extdoc;

import java.util.List;

import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.formats.html.ConfigurationImpl;
import com.sun.tools.doclets.formats.html.HtmlDoclet;
import com.sun.tools.doclets.internal.toolkit.Configuration;
import com.sun.tools.doclets.internal.toolkit.taglets.Taglet;
import com.sun.tools.doclets.standard.Standard;



public class RecommendersDoclet extends Standard{

    private static HtmlDoclet doclet;
    private static RecommendersDoclet instance;
    private ICustomTagletManager manager;
    
    public RecommendersDoclet() {
        manager = new PluginTagletManager();
    }

    /**
     * We want to use the standard HtmlDoclet, but call setOptions() first to
     * make sure that doclet.configuration.tagletManager != null
     * for adding custom taglets on TagletManager.
     * 
     * 
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
        }
    }
    

    private void configureCustomTaglets() {

        Configuration conf = doclet.configuration;

        List<Taglet> customTaglets = manager.getCustomTaglets();

        for (Taglet taglet : customTaglets) {
            conf.tagletManager.addCustomTag(taglet);
        }
    }

}
