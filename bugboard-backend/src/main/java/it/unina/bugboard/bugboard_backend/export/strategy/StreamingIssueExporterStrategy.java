package it.unina.bugboard.bugboard_backend.export.strategy;

import java.io.OutputStream;

import it.unina.bugboard.bugboard_backend.export.ExportFormat;

/**
 * Interface for streaming export of issues.
 * Writes data directly to OutputStream instead of accumulating in memory.
 */
public interface StreamingIssueExporterStrategy {

    /**
     * Exports issues by writing directly to OutputStream line by line.
     *
     * @param output OutputStream to write data to
     * @param fetchStrategy data fetch strategy (lazy loading)
     */
    void exportStream(OutputStream output, IssueStreamFetcher fetchStrategy);

    ExportFormat getSupportedFormat();

    /**
     * Functional interface for lazy data fetching.
     */
    @FunctionalInterface
    interface IssueStreamFetcher {
        /**
         * Fetches data and processes each page.
         *
         * @param pageProcessor callback receiving list of issues per page
         */
        void fetchPages(PageProcessor pageProcessor);
    }

    @FunctionalInterface
    interface PageProcessor {
        /**
         * Processes a page of issues.
         *
         * @param issues list of issues for this page
         * @return true if there are more pages, false if this is the last page
         */
        boolean processPage(java.util.List<it.unina.bugboard.bugboard_backend.dto.IssueResponse> issues);
    }
}
