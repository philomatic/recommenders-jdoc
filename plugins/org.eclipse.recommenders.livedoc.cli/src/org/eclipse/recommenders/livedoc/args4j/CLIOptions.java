package org.eclipse.recommenders.livedoc.args4j;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

public class CLIOptions {
    
    @Option(name="-sr", handler=ExtURLOptionHandler.class, usage="URL of Maven Repo")
    private URL sourceRepo;
    
    @Option(name="-mr", handler=ExtURLOptionHandler.class, usage="URL of Maven Repo")
    private URL modelsRepo;
    
    @Option(name="-verbose", usage="Output messages about what Livedoc is doing")
    private boolean verbose;
    
    @Option(name="-d", usage="Destination directory for output files")
    private File outputDir;
    
    @Option(name="-jar", usage="Want to have an zipped jar output for javadoc? Warning: Requires '-d <OutputDir>'")
    private boolean jarOutput;
    
    @Option(name="-p", usage="A list of packages to javadoc, otherwise all found packages in the maven coordinate will be used.")
    private String packages;
    
    @Option(name="-u", handler=ExtURLOptionHandler.class, usage="...uploads the updated javadocs to the repository....")
    private URL uploadRepo;
    
    // MavenCoordinates
    @Argument(index=0, metaVar="[MavenCoordinate]", required=true, usage="<GroupId>:<ArtifactId>:<Version>")
    private String mavenCoordinates;
    
    // Selected Taglets
    @Option(name="-t", handler=StringArrayOptionHandler.class, metaVar="\"ovrm ovrp ...\"", usage="Choose Recommenders Taglets (Default: Use all registered Taglets): \n ovrm: Method Overrides \n ovrp: Method Override Patterns")
    private String[] taglets;
    
    public CLIOptions() {
        try {
            // mvn central as default URL for sources
            this.setSourceRepo(new URL("http://repo1.maven.org/maven2/"));
            
            // eclipse kepler as default URL for models
            this.setModelsRepo(new URL("http://download.eclipse.org/recommenders/models/kepler/"));
            
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setPackages(String packages) {
        this.packages = packages;
    }

    public String getPackages() {
        return packages;
    }

    public String getMavenCoordinates() {
        return mavenCoordinates;
    }

    public void setMavenCoordinates(String mavenCoordinates) {
        this.mavenCoordinates = mavenCoordinates;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public boolean isJarOutput() {
        return jarOutput;
    }

    public void setJarOutput(boolean jarOutput) {
        this.jarOutput = jarOutput;
    }

    public URL getSourceRepo() {
        return this.sourceRepo;
    }

    public void setSourceRepo(URL sourceRepo) {
        this.sourceRepo = sourceRepo;
    }

    public URL getUploadRepo() {
        return uploadRepo;
    }

    public void setUploadRepo(URL uploadRepo) {
        this.uploadRepo = uploadRepo;
    }

    public URL getModelsRepo() {
        return modelsRepo;
    }

    public void setModelsRepo(URL modelsRepo) {
        this.modelsRepo = modelsRepo;
    }

    public String[] getTaglets() {
        return taglets;
    }

    public void setTaglets(String[] taglets) {
        this.taglets = taglets;
    }

}
