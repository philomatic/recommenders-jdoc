package org.eclipse.recommenders.livedoc.javadoc;

import com.sun.javadoc.Doc;

public interface IDocAppender {

    CharSequence render(Doc doc);

}
