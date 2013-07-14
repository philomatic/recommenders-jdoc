package org.eclipse.recommenders.livedoc;

import com.sun.javadoc.Doc;

public interface IDocAppender {

    CharSequence render(Doc doc);

}
