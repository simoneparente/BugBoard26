package it.unina.bugboard.bugboard_backend.export;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import it.unina.bugboard.bugboard_backend.export.strategy.IssueExporterStrategy;
import it.unina.bugboard.bugboard_backend.export.strategy.StreamingIssueExporterStrategy;

@Service
public class IssueExportFactory {

    private final Map<ExportFormat, IssueExporterStrategy> exportersByFormat;
    private final Map<ExportFormat, StreamingIssueExporterStrategy> streamingExportersByFormat;

    public IssueExportFactory(
            List<IssueExporterStrategy> exporters,
            List<StreamingIssueExporterStrategy> streamingExporters) {
        this.exportersByFormat = exporters.stream()
                .collect(Collectors.toMap(
                        IssueExporterStrategy::getSupportedFormat,
                        exporter -> exporter
                ));
        this.streamingExportersByFormat = streamingExporters.stream()
                .collect(Collectors.toMap(
                        StreamingIssueExporterStrategy::getSupportedFormat,
                        exporter -> exporter
                ));
    }

    public IssueExporterStrategy getExporter(ExportFormat format) {
        IssueExporterStrategy exporter = exportersByFormat.get(format);
        if (exporter == null) {
            throw new UnsupportedOperationException(
                    "Export format not supported: " + format
            );
        }
        return exporter;
    }

    /**
     * Gets the streaming exporter for the specified format.
     * Prefer this for large datasets to avoid OutOfMemory.
     */
    public StreamingIssueExporterStrategy getStreamingExporter(ExportFormat format) {
        StreamingIssueExporterStrategy exporter = streamingExportersByFormat.get(format);
        if (exporter == null) {
            throw new UnsupportedOperationException(
                    "Streaming export format not supported: " + format
            );
        }
        return exporter;
    }
}
