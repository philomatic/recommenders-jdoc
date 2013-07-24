package org.eclipse.recommenders.livedoc.extdoc;

import java.util.List;

import com.sun.tools.doclets.internal.toolkit.taglets.Taglet;

public interface ICustomTagletManager {

    public List<Taglet> getCustomTaglets();
}
