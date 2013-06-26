package org.eclipse.recommenders.livedoc.args4j;

import java.net.MalformedURLException;
import java.net.URL;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;
import org.kohsuke.args4j.spi.URLOptionHandler;

public class ExtURLOptionHandler extends URLOptionHandler {

    public ExtURLOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super URL> setter) {
        super(parser, option, setter);
    }
    
    @Override
    public int parseArguments(Parameters params) throws CmdLineException {
        try {
            return super.parseArguments(params);

        } catch (CmdLineException cmdLineException) {
            String param = params.getParameter(0);

            // try a file:// URL instead
            try {
                setter.addValue(new URL("file://" + param));
                return 1;
            } catch (MalformedURLException urlException) {
                throw cmdLineException;
            }

        }
    }

    @Override
    public String getDefaultMetaVariable() {
        return "URL";
    }

    

}
