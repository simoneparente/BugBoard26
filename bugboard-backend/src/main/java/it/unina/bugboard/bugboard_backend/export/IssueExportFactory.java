package it.unina.bugboard.bugboard_backend.export;

import it.unina.bugboard.bugboard_backend.export.strategy.IssueExporterStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IssueExportFactory {

    private final Map<ExportFormat, IssueExporterStrategy> exportersByFormat;

    public IssueExportFactory(List<IssueExporterStrategy> exporters) {
        this.exportersByFormat = exporters.stream()
                .collect(Collectors.toMap(
                        IssueExporterStrategy::getSupportedFormat,
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
}
