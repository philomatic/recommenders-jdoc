package org.eclipse.recommenders.internal.livedoc.aether;

import static org.eclipse.core.runtime.IProgressMonitor.UNKNOWN;
import static org.eclipse.recommenders.internal.livedoc.l10n.Messages.INITIALIZE_INDEXING_CONTEXT_FAILURE;
import static org.eclipse.recommenders.internal.livedoc.l10n.Messages.INITIALIZE_INDEXING_CONTEXT_SUCCESS;
import static org.eclipse.recommenders.internal.livedoc.l10n.Messages.SCANNING_ARTIFACT_EXCEPTION;
import static org.eclipse.recommenders.internal.livedoc.l10n.Messages.SCANNING_ARTIFACT_FAILED;
import static org.eclipse.recommenders.internal.livedoc.l10n.Messages.SCANNING_ARTIFACT_FOUND;
import static org.eclipse.recommenders.internal.livedoc.l10n.Messages.SCANNING_BEGIN;
import static org.eclipse.recommenders.internal.livedoc.l10n.Messages.SCANNING_END;
import static org.eclipse.recommenders.internal.livedoc.l10n.Messages.SCANNING_RESULTS;

import java.io.File;
import java.io.IOException;

import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactScanningListener;
import org.apache.maven.index.ScanningResult;
import org.apache.maven.index.context.IndexingContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.recommenders.utils.Throws;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FilesystemBasedRepositoryContentsProvider extends AbstractRepositoryContentsProvider {

    private static final Logger log = LoggerFactory.getLogger(FilesystemBasedRepositoryContentsProvider.class);

    private static final String INDEX_UPDATE_URL = null;

    private final IndexingContext context;

    public FilesystemBasedRepositoryContentsProvider(File repoBasedir, File indexBasedir) throws Exception {
        super(repoBasedir.toURI().toURL(), indexBasedir);
        String repositoryUrl = repoBasedir.toURI().toURL().toExternalForm();
        try {
            context = getNexusIndexer().addIndexingContext("context-id", "repository-id", repoBasedir, getIndexDir(),
                    repositoryUrl, INDEX_UPDATE_URL, getIndexCreators());
            log.debug(INITIALIZE_INDEXING_CONTEXT_SUCCESS, repositoryUrl);
        } catch (Exception e) {
            log.error(INITIALIZE_INDEXING_CONTEXT_FAILURE, repositoryUrl, e);
            throw Throws.throwUnhandledException(e);
        }
    }

    @Override
    protected void createIndex(IProgressMonitor monitor) throws IOException {
        monitor.beginTask("Creating index", UNKNOWN);
        try {
            getNexusIndexer().scan(context, new ScanningListener());
        } finally {
            monitor.done();
        }
    }

    @Override
    protected void updateIndex(IProgressMonitor monitor) throws IOException {
        monitor.beginTask("Updating index", UNKNOWN);
        try {
            getNexusIndexer().scan(context, new ScanningListener(), true);
        } finally {
            monitor.done();
        }
    }

    @Override
    public void close() throws IOException {
        if (context != null) {
            context.close(false);
        }
    }

    private static final class ScanningListener implements ArtifactScanningListener {

        @Override
        public void scanningStarted(IndexingContext context) {
            log.info(SCANNING_BEGIN, context.getRepositoryUrl());
        }

        @Override
        public void artifactDiscovered(ArtifactContext context) {
            log.debug(SCANNING_ARTIFACT_FOUND, context.getArtifactInfo(), context.getArtifact());
        }

        @Override
        public void artifactError(ArtifactContext context, Exception e) {
            log.error(SCANNING_ARTIFACT_FAILED, context.getGav());
            log.debug(SCANNING_ARTIFACT_EXCEPTION, e);
        }

        @Override
        public void scanningFinished(IndexingContext context, ScanningResult result) {
            log.info(SCANNING_END, context.getRepositoryUrl());
            log.info(SCANNING_RESULTS, result.getTotalFiles(), result.getDeletedFiles());
        }
    }
}
