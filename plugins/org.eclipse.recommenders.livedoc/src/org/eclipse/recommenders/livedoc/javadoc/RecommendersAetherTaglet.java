package org.eclipse.recommenders.livedoc.javadoc;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.recommenders.livedoc.aether.IRepositoryBroker;
import org.eclipse.recommenders.livedoc.aether.RepoBrokerProvider;
import org.eclipse.recommenders.livedoc.aether.RepositoryDescriptor;
import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;

public abstract class RecommendersAetherTaglet extends RecommendersTaglet {
    
    private static final File TEMP_DIR = new File("java.io.tmpdir", "livedoc");
    private static final File MODELSREPO_CACHE_DIR = new File(TEMP_DIR, "modelsRepo/cache");
    private static final File MODELSREPO_INDEX_DIR = new File(TEMP_DIR, "modelsRepo/indexes");


    private URL modelsRepo;
    protected String groupId;
    protected String artifactVersion;
    protected String artifactId;
    
    
    
    protected Artifact downloadModelsArtifact(String classifier) {
        MODELSREPO_CACHE_DIR.mkdirs();
        MODELSREPO_INDEX_DIR.mkdirs();

        IRepositoryBroker repoBroker = null;
        try {
            repoBroker = RepoBrokerProvider.create(MODELSREPO_CACHE_DIR, MODELSREPO_INDEX_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }

        RepositoryDescriptor modelsRepoDescriptor = new RepositoryDescriptor("modelsRepo", getModelsRepo());

        try {
            repoBroker.ensureIndexUpToDate(modelsRepoDescriptor, new NullProgressMonitor());
        } catch (Exception e) {
            e.printStackTrace();
        }

        StringBuffer sb = new StringBuffer();

        sb.append(groupId)
            .append(":")
            .append(artifactId)
            .append(":")
            .append("zip")
            .append(":")
            .append(classifier)
            .append(":")
            .append(calcBaseVersion(artifactVersion));

        Artifact artifact = new DefaultArtifact(sb.toString());

        try {
            artifact = repoBroker.download(artifact, modelsRepoDescriptor, new NullProgressMonitor());
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return artifact;
    }

    private String calcBaseVersion(String version) {
        return StringUtils.substringBefore(version, ".") + ".0.0";
    }
    
    public URL getModelsRepo() {
        return modelsRepo;
    }

    @Override
    public void setModelsRepo(URL modelsRepo) {
        this.modelsRepo = modelsRepo;
    }
    @Override
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    @Override
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }
    @Override
    public void setArtifactVersion(String version) {
        this.artifactVersion = version;
    }
    
    @Override
    public boolean inField() {
        return false;
    }

    @Override
    public boolean inConstructor() {
        return false;
    }

    @Override
    public boolean inMethod() {
        return false;
    }

    @Override
    public boolean inOverview() {
        return false;
    }

    @Override
    public boolean inPackage() {
        return false;
    }

    @Override
    public boolean inType() {
        return false;
    }

    @Override
    public boolean isInlineTag() {
        return false;
    }
}
