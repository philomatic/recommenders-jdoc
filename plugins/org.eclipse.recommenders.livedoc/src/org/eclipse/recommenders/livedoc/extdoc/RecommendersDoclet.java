package org.eclipse.recommenders.livedoc.extdoc;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;

public class RecommendersDoclet extends com.sun.tools.doclets.standard.Standard{

        public static boolean start(RootDoc root) {
            ClassDoc[] classes = root.classes();
            for (int i = 0; i < classes.length; ++i) {
                System.out.println(classes[i]);
            }
            return true;
        }
    }