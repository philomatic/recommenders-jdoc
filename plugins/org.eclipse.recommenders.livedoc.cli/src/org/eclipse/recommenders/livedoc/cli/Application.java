package org.eclipse.recommenders.livedoc.cli;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.recommenders.livedoc.aether.IRepositoryBroker;
import org.eclipse.recommenders.livedoc.aether.RepositoryDescriptor;
import org.eclipse.recommenders.livedoc.args4j.CLIOptions;
import org.eclipse.recommenders.livedoc.args4j.ExtURLOptionHandler;
import org.eclipse.recommenders.livedoc.javadoc.ILiveDoc;
import org.eclipse.recommenders.livedoc.javadoc.LiveDoc;
import org.eclipse.recommenders.livedoc.utils.RepoBrokerProvider;
import org.eclipse.recommenders.livedoc.utils.ZipUtils;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

    public static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final File CACHE_DIR = new File(TEMP_DIR, "cache");
    private static final File INDEX_DIR = new File(TEMP_DIR, "indexes");
    private Logger log = LoggerFactory.getLogger(getClass());
    private CLIOptions settings;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app. IApplicationContext)
     */
    @SuppressWarnings({ "static-access", "unchecked" })
    public Object start(IApplicationContext context) throws Exception {

        // Parsing arguments

        Iterator<String[]> iter = context.getArguments().values().iterator();
        String[] args = null;
        if (iter.hasNext()) {
            args = iter.next();
        } else {
            System.err.println("No Arguments detected");
        }

        settings = new CLIOptions();
        CmdLineParser parser = new CmdLineParser(settings);
        parser.registerHandler(URL.class, ExtURLOptionHandler.class);
        try {
            parser.parseArgument(args);
        } catch (Exception e) {

            // handling of wrong arguments
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            this.stop();
        }

        CACHE_DIR.mkdirs();
        INDEX_DIR.mkdirs();

        IRepositoryBroker repoBroker = RepoBrokerProvider.create(CACHE_DIR, INDEX_DIR);
        RepositoryDescriptor sourceRepositoryDescriptor = new RepositoryDescriptor("sourceRepo",
                settings.getSourceRepo());

        repoBroker.ensureIndexUpToDate(sourceRepositoryDescriptor, new NullProgressMonitor());

        String coordinates = settings.getMavenCoordinates();

        StringBuffer sb = new StringBuffer(coordinates);
        int lastColon = coordinates.lastIndexOf(":");
        sb.insert(lastColon, ":jar:sources");

        Artifact artifact = new DefaultArtifact(sb.toString());
        artifact = repoBroker.download(artifact, sourceRepositoryDescriptor, new NullProgressMonitor());

        File sourceFiles = ZipUtils.unzipToSeparateFolder(artifact.getFile(), new File(TEMP_DIR));

        List<String> subpackages = Arrays.asList(sourceFiles.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return !name.equals("META-INF");
            }
        }));

        File livedocDir = new File(TEMP_DIR + File.separator + "livedoc_tmp");

        sb.delete(0, sb.length());
        sb.append(artifact.getArtifactId());
        sb.append("-");
        sb.append(artifact.getVersion());
        sb.append("-javadoc");

        File tmpOutput = new File(livedocDir, sb.toString());

        ILiveDoc livedoc = new LiveDoc(settings.isVerbose(), sourceFiles, tmpOutput, subpackages);
        livedoc.generate();

        if (settings.getOutputDir() != null) {
            File output = new File(settings.getOutputDir().getAbsolutePath() + File.separator + sb.toString());
            FileUtils.copyDirectory(tmpOutput, output);
        }

        sb.append(".jar");

        File tmpJar = new File(livedocDir, sb.toString());
        ZipUtils.zip(tmpOutput, tmpJar);

        if (settings.isJarOutput()) {
            File output = new File(settings.getOutputDir().getAbsolutePath() + File.separator + sb.toString());
            FileUtils.copyFile(tmpJar, output);
        }

        if (settings.getUploadRepo() != null) {
            Artifact uploadArtifact = new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), "javadoc",
                    "jar", artifact.getVersion());
            uploadArtifact = uploadArtifact.setFile(tmpOutput);
            RepositoryDescriptor uploadRepositoryDescriptor = new RepositoryDescriptor("uploadRepo",
                    settings.getUploadRepo());
            repoBroker.upload(uploadArtifact, uploadRepositoryDescriptor, new NullProgressMonitor());
        }

        System.out.println("Done.");

        return IApplication.EXIT_OK;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.equinox.app.IApplication#stop()
     */
    public void stop() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.exit(0);
    }
}
