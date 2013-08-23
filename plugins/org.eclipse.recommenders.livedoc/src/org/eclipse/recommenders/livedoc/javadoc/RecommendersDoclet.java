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
    private List<RecommendersTaglet> customTaglets;
    
    private RecommendersDoclet() {
        manager = new PluginTagletManager();
    }
    
    public static RecommendersDoclet instance(){
        
        if (instance == null){
            instance = new RecommendersDoclet();
        }
        return instance;
    }

    /**
     * Javadoc calls this Method
     * 
     * We want to use the standard HtmlDoclet, but call setOptions() first to
     * make sure that doclet.configuration.tagletManager != null
     * for adding custom taglets on this TagletManager.
     **/
    public static boolean start(RootDoc root) {
        try {
            doclet = new HtmlDoclet();
            
            // root shouldn't be null for setOptions()
            doclet.configuration.root = root;
            doclet.configuration.setOptions();
            
            // load custom taglets via extension point
            instance().configureCustomTaglets();
            return doclet.start(doclet, root);
        } finally {
            ConfigurationImpl.reset();
            instance.closeTaglets();
        }
    }
    

    private void closeTaglets() {
        for (RecommendersTaglet taglet : customTaglets) {
            taglet.finish();
        }
    }

    private void configureCustomTaglets() {

        Configuration conf = doclet.configuration;
        customTaglets = getTagletManager().getCustomTaglets();

        for (RecommendersTaglet taglet : customTaglets) {
            
            taglet.initialize();
            conf.tagletManager.addCustomTag(taglet);
        }
    }

    public ICustomTagletManager getTagletManager() {
        return manager;
    }
}
