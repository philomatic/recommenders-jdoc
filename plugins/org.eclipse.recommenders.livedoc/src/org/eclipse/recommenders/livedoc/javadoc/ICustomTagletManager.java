package org.eclipse.recommenders.livedoc.javadoc;

import java.util.List;

public interface ICustomTagletManager {

    List<RecommendersTaglet> getCustomTaglets();
    void setSelectedTaglets(String[] taglets);
}
