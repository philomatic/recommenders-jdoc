package org.eclipse.recommenders.livedoc.taglets.overrides;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.recommenders.livedoc.aether.IRepositoryBroker;
import org.eclipse.recommenders.livedoc.aether.RepoBrokerProvider;
import org.eclipse.recommenders.livedoc.aether.RepositoryDescriptor;
import org.eclipse.recommenders.livedoc.javadoc.IRecommendersTaglet;
import org.eclipse.recommenders.models.BasedTypeName;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.overrides.IOverrideModel;
import org.eclipse.recommenders.overrides.SingleZipOverrideModelProvider;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.Recommendations;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.google.common.base.Optional;
import com.sun.javadoc.Doc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.internal.toolkit.taglets.SeeTaglet;
import com.sun.tools.doclets.internal.toolkit.taglets.TagletOutput;
import com.sun.tools.doclets.internal.toolkit.taglets.TagletWriter;

public class OverridesTaglet implements IRecommendersTaglet {

    private static final File TEMP_DIR = new File("java.io.tmpdir", "livedoc");
    private static final File MODELSREPO_CACHE_DIR = new File(TEMP_DIR, "modelsRepo/cache");
    private static final File MODELSREPO_INDEX_DIR = new File(TEMP_DIR, "modelsRepo/indexes");
    
    private URL modelsRepo;
    private String groupId;
    private String artifactVersion;
    private String artifactId;
    private SingleZipOverrideModelProvider modelProvider;
    public OverridesTaglet() {
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
        return true;
    }

    @Override
    public boolean isInlineTag() {
        return false;
    }

    @Override
    public String getName() {
        return "overrides";
    }

    @Override
    public TagletOutput getTagletOutput(Tag tag, TagletWriter writer) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public TagletOutput getTagletOutput(Doc holder, TagletWriter writer) throws IllegalArgumentException {
        
        String typeName = "L" + holder.toString().replace(".", "/");

        ProjectCoordinate coordinate = new ProjectCoordinate(groupId, artifactId, artifactVersion);
        ITypeName name = VmTypeName.get(typeName);

        BasedTypeName key = new BasedTypeName(coordinate, name);

        Optional<IOverrideModel> model = modelProvider.acquireModel(key);

        StringBuilder sb = new StringBuilder();
        if (model.isPresent()) {
            List<Recommendation<IMethodName>> recommendOverrides = model.get().recommendOverrides();

            Collections.sort(recommendOverrides, new Comparator<Recommendation<IMethodName>>() {

                @Override
                public int compare(Recommendation<IMethodName> o1, Recommendation<IMethodName> o2) {
                    return (Recommendations.asPercentage(o2) - Recommendations.asPercentage(o1));
                }
            });

            sb.append("<dl>");
            sb.append("<dt>Method Overrides class documentation:</dt>");
            sb.append("<dd>");

            for (Iterator<Recommendation<IMethodName>> iterator = recommendOverrides.iterator(); iterator.hasNext();) {

                Recommendation<IMethodName> recommendation = (Recommendation<IMethodName>) iterator.next();

                int relevance = Recommendations.asPercentage(recommendation);

                // sb.append("{@link #");
                IMethodName method = recommendation.getProposal();

                sb.append(method.getName());
                sb.append("(");

                if (methodHasParameters(method)) {
                    sb.append("...");
                }
                sb.append(")");
                // sb.append("}");

                sb.append(" - ").append("<font color=\"#0000FF\">").append(relevance + "%").append("</font>");

                if (iterator.hasNext()) {
                    sb.append(", ");
                }

            }

            sb.append("</dd>");
        }

        TagletOutput output = writer.getOutputInstance();
        output.setOutput(sb.toString());
        return output;
    }

    private boolean methodHasParameters(IMethodName method) {
        return (method.getParameterTypes().length != 0);
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
    public void initialize() {
        
        Artifact artifact = getModelsArtifact();
        
        File models = artifact.getFile();
        modelProvider = new SingleZipOverrideModelProvider(models);
        
        try {
            modelProvider.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    private Artifact getModelsArtifact() {
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
            .append("ovrm")
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

    public void setModelsRepo(URL modelsRepo) {
        this.modelsRepo = modelsRepo;
    }

    @Override
    public void finish() {

        try {
            modelProvider.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
