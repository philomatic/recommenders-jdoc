package org.eclipse.recommenders.internal.livedoc.l10n;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.codetrails.internal.repo.l10n.messages";

    public static String INDEX_FOUND;
    public static String INDEX_NOT_FOUND;

    public static String SCANNING_BEGIN;
    public static String SCANNING_END;
    public static String SCANNING_RESULTS;
    public static String SCANNING_ARTIFACT_FOUND;
    public static String SCANNING_ARTIFACT_FAILED;
    public static String SCANNING_ARTIFACT_EXCEPTION;

    public static String CREATING_NEW_INDEX;
    public static String UPDATING_INDEX;
    public static String WRITING_INDEX_MONITOR;

    public static String LIST_ARTIFACTS_FAILED_EXCEPTION;
    public static String DOWNLOADING_RESOURCE;
    public static String DOWNLOADED_RESOURCE;
    public static String FULL_INDEX_DOWNLOAD;
    public static String INDEX_UP_TO_DATE;
    public static String PARTIAL_INDEX_DOWNLOAD;

    public static String FETCH_LIST_BEGIN;

    public static String ARTIFACT_ADDED_TO_LIST;
    public static String INITIALIZE_INDEXING_CONTEXT_SUCCESS;
    public static String INITIALIZE_INDEXING_CONTEXT_FAILURE;

    public static String CREATING_DEPENDENTS_INDEX_BEGIN;
    public static String CREATING_DEPENDENTS_INDEX_END;

    public static String COLLECTING_CLASSPATH_DEPENDENCIES;
    public static String RESOLVING_CLASSPATH_DEPENDENCIES;
    public static String LISTING_DIRECT_DEPENDENCIES;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}
