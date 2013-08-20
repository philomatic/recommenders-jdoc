package org.eclipse.recommenders.internal.livedoc.aether;

import static org.eclipse.core.runtime.IProgressMonitor.UNKNOWN;
import static org.eclipse.recommenders.internal.livedoc.l10n.Messages.ARTIFACT_ADDED_TO_LIST;
import static org.eclipse.recommenders.internal.livedoc.l10n.Messages.CREATING_NEW_INDEX;
import static org.eclipse.recommenders.internal.livedoc.l10n.Messages.FETCH_LIST_BEGIN;
import static org.eclipse.recommenders.internal.livedoc.l10n.Messages.INDEX_FOUND;
import static org.eclipse.recommenders.internal.livedoc.l10n.Messages.INDEX_NOT_FOUND;
import static org.eclipse.recommenders.internal.livedoc.l10n.Messages.LIST_ARTIFACTS_FAILED_EXCEPTION;
import static org.eclipse.recommenders.internal.livedoc.l10n.Messages.UPDATING_INDEX;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.ArtifactInfoGroup;
import org.apache.maven.index.GroupedSearchRequest;
import org.apache.maven.index.GroupedSearchResponse;
import org.apache.maven.index.MAVEN;
import org.apache.maven.index.NexusIndexer;
import org.apache.maven.index.SearchType;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.search.grouping.GAGrouping;
import org.apache.maven.index.updater.IndexUpdater;
import org.apache.maven.wagon.Wagon;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.Throws;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

public abstract class AbstractRepositoryContentsProvider implements IRepositoryContentsProvider {

    public static final String CLASS_WORLD = "com.codetrails";

    private static final Logger log = LoggerFactory.getLogger(AbstractRepositoryContentsProvider.class);
    private NexusIndexer nexusIndexer;

    private DefaultPlexusContainer plexus;

    private final File indexDir;

    private final URL sourceRepository;

    public AbstractRepositoryContentsProvider(URL sourceRepository, File indexBasedir) {
        File allIndexDir = new File(indexBasedir, "contents");
        this.indexDir = new File(allIndexDir, sourceRepository.toExternalForm().replaceAll("\\W", "_"));
        this.sourceRepository = sourceRepository;
        initializePlexusContainer();
    }

    private void initializePlexusContainer() {
        try {
            ClassLoader cl = FilesystemBasedRepositoryContentsProvider.class.getClassLoader();
            ContainerConfiguration cc = new DefaultContainerConfiguration().setClassWorld(
                    new ClassWorld(CLASS_WORLD, cl)).setName("plexus");
            plexus = new DefaultPlexusContainer(cc);
            nexusIndexer = plexus.lookup(NexusIndexer.class);
        } catch (Exception e) {
            throw Throws.throwUnhandledException(e);
        }
    }

    @Override
    public final List<Artifact> listPrimaryArtifacts(IProgressMonitor monitor, String... extensions) {
        monitor.beginTask("Listing primary artifacts", UNKNOWN);
        try {
            GroupedSearchResponse response = queryIndex(constructAllClassifiersQuery());

            final List<String> artifactExtensions = Arrays.asList(extensions);

            return filterArtifacts(response, new Predicate<ArtifactInfo>() {

                @Override
                public boolean apply(ArtifactInfo ai) {
                    return artifactExtensions.contains(ai.fextension) && ai.classifier == null;
                }
            });
        } catch (Exception e) {
            log.error(LIST_ARTIFACTS_FAILED_EXCEPTION, sourceRepository, e);
            return Collections.emptyList();
        } finally {
            monitor.done();
        }
    }

    @Override
    public final List<Artifact> listSecondaryArtifacts(IProgressMonitor monitor, final String classifier,
            final String... extensions) {
        monitor.beginTask("Listing secondary artifacts", UNKNOWN);
        try {
            Checks.ensureIsNotNull(classifier);
            Checks.ensureIsNotEmpty(classifier, "Empty classifier not allowed for secondary artifacts");

            GroupedSearchResponse response = queryIndex(constructClassifierQuery(classifier));

            final List<String> artifactExtensions = Arrays.asList(extensions);

            return filterArtifacts(response, new Predicate<ArtifactInfo>() {

                @Override
                public boolean apply(ArtifactInfo ai) {
                    return artifactExtensions.contains(ai.fextension) && ai.classifier != null
                            && classifier.equals(ai.classifier);
                }

            });
        } catch (Exception e) {
            log.error(LIST_ARTIFACTS_FAILED_EXCEPTION, sourceRepository, e);
            return Collections.emptyList();
        } finally {
            monitor.done();
        }
    }

    @Override
    public final List<Artifact> listAllArtifacts(IProgressMonitor monitor) {
        monitor.beginTask("Listing all artifacts", UNKNOWN);
        try {
            GroupedSearchResponse response = queryIndex(constructAllClassifiersQuery());
            return filterArtifacts(response, Predicates.<ArtifactInfo>alwaysTrue());
        } catch (Exception e) {
            log.error(LIST_ARTIFACTS_FAILED_EXCEPTION, sourceRepository, e);
            return Collections.emptyList();
        } finally {
            monitor.done();
        }
    }

    private GroupedSearchResponse queryIndex(Query query) throws Exception {
        log.debug(FETCH_LIST_BEGIN);
        return nexusIndexer.searchGrouped(new GroupedSearchRequest(query, new GAGrouping()));
    }

    @Override
    public List<Artifact> listMatchingArtifacts(Artifact coordinate, IProgressMonitor monitor,
            final String... extensions) {
        try {
            Query query = constructArtifactQuery(coordinate);

            GroupedSearchResponse response = queryIndex(query);

            final List<String> artifactExtensions = Arrays.asList(extensions);

            return filterArtifacts(response, new Predicate<ArtifactInfo>() {

                @Override
                public boolean apply(ArtifactInfo ai) {
                    return artifactExtensions.contains(ai.fextension);
                }
            });
        } catch (Exception e) {
            log.error(LIST_ARTIFACTS_FAILED_EXCEPTION, sourceRepository, e);
            return Collections.emptyList();
        }
    }

    private Query constructArtifactQuery(Artifact coordinate) {
        BooleanQuery query = new BooleanQuery();
        if (coordinate.getGroupId() != null && !coordinate.getGroupId().isEmpty())
            query.add(nexusIndexer.constructQuery(MAVEN.GROUP_ID, coordinate.getGroupId(), SearchType.EXACT),
                    Occur.MUST);

        if (coordinate.getArtifactId() != null && !coordinate.getArtifactId().isEmpty())
            query.add(nexusIndexer.constructQuery(MAVEN.ARTIFACT_ID, coordinate.getArtifactId(), SearchType.EXACT),
                    Occur.MUST);

        if (coordinate.getVersion() != null && !coordinate.getVersion().isEmpty())
            query.add(nexusIndexer.constructQuery(MAVEN.VERSION, coordinate.getVersion(), SearchType.EXACT), Occur.MUST);

        if (coordinate.getClassifier() != null && !coordinate.getClassifier().isEmpty())
            query.add(nexusIndexer.constructQuery(MAVEN.CLASSIFIER, coordinate.getClassifier(), SearchType.EXACT),
                    Occur.MUST);

        if (query.getClauses() == null || query.getClauses().length == 0)
            return constructAllClassifiersQuery();

        return query;
    }

    private Query constructAllClassifiersQuery() {
        return new MatchAllDocsQuery();
    }

    private Query constructClassifierQuery(String classifier) {
        BooleanQuery query = new BooleanQuery();
        query.add(nexusIndexer.constructQuery(MAVEN.CLASSIFIER, classifier, SearchType.EXACT), Occur.MUST);
        return query;
    }

    private List<Artifact> filterArtifacts(GroupedSearchResponse response, Predicate<ArtifactInfo> matcher) {
        List<Artifact> result = Lists.newArrayList();
        for (ArtifactInfoGroup entry : response.getResults().values()) {
            for (ArtifactInfo ai : entry.getArtifactInfos()) {
                if (matcher.apply(ai)) {
                    Artifact artifact = new DefaultArtifact(ai.groupId, ai.artifactId, ai.classifier, ai.fextension,
                            ai.version);
                    result.add(artifact);
                    if (log.isTraceEnabled()) {
                        log.trace(ARTIFACT_ADDED_TO_LIST, artifact);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void index(IProgressMonitor monitor) throws Exception {
        if (!indexExists()) {
            log.info(INDEX_NOT_FOUND);
            log.info(CREATING_NEW_INDEX);
            createIndex(monitor);
        } else {
            log.info(INDEX_FOUND);
            log.info(UPDATING_INDEX);
            updateIndex(monitor);
        }
    }

    private boolean indexExists() {
        // TODO This is a rather simple heuristic
        File timestamp = new File(getIndexDir(), "timestamp");
        return timestamp.exists();
    }

    protected abstract void createIndex(IProgressMonitor monitor) throws Exception;

    protected abstract void updateIndex(IProgressMonitor monitor) throws Exception;

    protected Wagon getWagon(String protocol) throws ComponentLookupException {
        return plexus.lookup(Wagon.class, protocol);
    }

    protected ArrayList<IndexCreator> getIndexCreators() throws ComponentLookupException {
        return Lists.newArrayList(plexus.lookup(IndexCreator.class, "min"),
                plexus.lookup(IndexCreator.class, "jarContent"));
    }

    protected IndexUpdater getIndexUpdater() throws ComponentLookupException {
        return plexus.lookup(IndexUpdater.class);
    }

    public NexusIndexer getNexusIndexer() {
        return nexusIndexer;
    }

    protected File getIndexDir() {
        return indexDir;
    }
}
