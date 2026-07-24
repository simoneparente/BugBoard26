package it.unina.bugboard.bugboard_backend.export.strategy;

import it.unina.bugboard.bugboard_backend.dto.IssueResponse;
import it.unina.bugboard.bugboard_backend.export.ExportFormat;

import java.util.List;

public interface IssueExporterStrategy {

    byte[] export(List<IssueResponse> issues);

    ExportFormat getSupportedFormat();
}
