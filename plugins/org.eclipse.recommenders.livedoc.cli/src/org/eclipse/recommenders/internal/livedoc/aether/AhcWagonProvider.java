/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.livedoc.aether;

import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.providers.file.FileWagon;
import org.sonatype.maven.wagon.AhcWagon;

/**
 * A simplistic provider for wagon instances when no Plexus-compatible IoC container is used.
 */
public class AhcWagonProvider implements org.sonatype.aether.connector.wagon.WagonProvider {

    @Override
    public Wagon lookup(String roleHint) throws Exception {
        if ("http".equals(roleHint) || "https".equals(roleHint)) {
            return new AhcWagon();
        } else if ("file".equals(roleHint)) {
            return new FileWagon();
        } else {
            return null;
        }
    }

    @Override
    public void release(Wagon wagon) {
    }
}
