package org.eclipse.recommenders.internal.livedoc.aether;

import static org.eclipse.core.runtime.IProgressMonitor.UNKNOWN;
import static org.eclipse.recommenders.internal.livedoc.l10n.Messages.DOWNLOADED_RESOURCE;
import static org.eclipse.recommenders.internal.livedoc.l10n.Messages.DOWNLOADING_RESOURCE;
import static org.eclipse.recommenders.internal.livedoc.l10n.Messages.FULL_INDEX_DOWNLOAD;
import static org.eclipse.recommenders.internal.livedoc.l10n.Messages.INDEX_UP_TO_DATE;
import static org.eclipse.recommenders.internal.livedoc.l10n.Messages.INITIALIZE_INDEXING_CONTEXT_FAILURE;
import static org.eclipse.recommenders.internal.livedoc.l10n.Messages.INITIALIZE_INDEXING_CONTEXT_SUCCESS;
import static org.eclipse.recommenders.internal.livedoc.l10n.Messages.PARTIAL_INDEX_DOWNLOAD;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.updater.IndexUpdateRequest;
import org.apache.maven.index.updater.IndexUpdateResult;
import org.apache.maven.index.updater.IndexUpdater;
import org.apache.maven.index.updater.ResourceFetcher;
import org.apache.maven.index.updater.WagonHelper;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.observers.AbstractTransferListener;
import org.apache.maven.wagon.resource.Resource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.recommenders.utils.Throws;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteRepositoryContentsProvider extends AbstractRepositoryContentsProvider {

    private static final Logger log = LoggerFactory.getLogger(RemoteRepositoryContentsProvider.class);

    private final URL url;
    private final IndexingContext context;

    public RemoteRepositoryContentsProvider(URL repositoryUrl, File indexBasedir) throws Exception {
        super(repositoryUrl, indexBasedir);
        url = repositoryUrl;
        try {
            context = getNexusIndexer().addIndexingContext("context-id", repositoryUrl.toString(), null, getIndexDir(),
                    repositoryUrl.toString(), null, getIndexCreators());
            log.debug(INITIALIZE_INDEXING_CONTEXT_SUCCESS, url);
        } catch (Exception e) {
            log.error(INITIALIZE_INDEXING_CONTEXT_FAILURE, url, e);
            throw Throws.throwUnhandledException(e);
        }
    }

    private void fetchIndex() throws Exception {
        Wagon wagon = getWagon(url.getProtocol());
        TransferListener listener = new AbstractTransferListener() {
            @Override
            public void transferStarted(TransferEvent transferEvent) {
                log.info(DOWNLOADING_RESOURCE, transferEvent.getResource().getName());
            }

            @Override
            public void transferProgress(TransferEvent transferEvent, byte[] buffer, int length) {
            }

            @Override
            public void transferCompleted(TransferEvent transferEvent) {
                Resource resource = transferEvent.getResource();
                long size = resource.getContentLength();
                log.info(DOWNLOADED_RESOURCE, resource.getName(), FileUtils.byteCountToDisplaySize(size));
            }
        };
        ResourceFetcher resourceFetcher = new WagonHelper.WagonFetcher(wagon, listener, null, null);
        IndexUpdateRequest request = new IndexUpdateRequest(context, resourceFetcher);
        IndexUpdater updater = getIndexUpdater();
        IndexUpdateResult updateResult = updater.fetchAndUpdateIndex(request);
        Date contextTimestamp = context.getTimestamp();
        if (updateResult.isFullUpdate()) {
            log.debug(FULL_INDEX_DOWNLOAD);
            // Note: updateResult.getTimestamp() may return null
        } else if (contextTimestamp.equals(updateResult.getTimestamp())) {
            log.debug(INDEX_UP_TO_DATE);
        } else {
            log.debug(PARTIAL_INDEX_DOWNLOAD, contextTimestamp, updateResult.getTimestamp());
        }
    }

    @Override
    protected void createIndex(IProgressMonitor monitor) throws Exception {
        monitor.beginTask("Creating index", UNKNOWN);
        try {
            fetchIndex();
        } finally {
            monitor.done();
        }
    }

    @Override
    protected void updateIndex(IProgressMonitor monitor) throws Exception {
        monitor.beginTask("Updating index", UNKNOWN);
        try {
            fetchIndex();
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
}
