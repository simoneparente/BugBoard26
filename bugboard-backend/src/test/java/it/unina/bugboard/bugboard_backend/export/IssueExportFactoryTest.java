package it.unina.bugboard.bugboard_backend.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.unina.bugboard.bugboard_backend.export.strategy.IssueExporterStrategy;
import it.unina.bugboard.bugboard_backend.export.strategy.StreamingIssueExporterStrategy;

@ExtendWith(MockitoExtension.class)
class IssueExportFactoryTest {

    @Mock
    private IssueExporterStrategy csvExporter;

    @Mock
    private StreamingIssueExporterStrategy streamingCsvExporter;

    private IssueExportFactory factory;

    @BeforeEach
    void setUp() {
        when(csvExporter.getSupportedFormat()).thenReturn(ExportFormat.CSV);
        when(streamingCsvExporter.getSupportedFormat()).thenReturn(ExportFormat.CSV);

        factory = new IssueExportFactory(List.of(csvExporter), List.of(streamingCsvExporter));
    }

    @Test
    void getExporter_WithSupportedFormat_ReturnsExporter() {
        IssueExporterStrategy exporter = factory.getExporter(ExportFormat.CSV);

        assertSame(csvExporter, exporter);
        verify(csvExporter).getSupportedFormat();
    }

    @Test
    void getExporter_WithUnsupportedFormat_ThrowsUnsupportedOperationException() {
        IssueExportFactory emptyFactory = new IssueExportFactory(List.of(), List.of());

        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                () -> emptyFactory.getExporter(ExportFormat.CSV));

        assertEquals("Export format not supported: CSV", exception.getMessage());
    }

    @Test
    void getStreamingExporter_WithSupportedFormat_ReturnsStreamingExporter() {
        StreamingIssueExporterStrategy exporter = factory.getStreamingExporter(ExportFormat.CSV);

        assertSame(streamingCsvExporter, exporter);
        verify(streamingCsvExporter).getSupportedFormat();
    }

    @Test
    void getStreamingExporter_WithUnsupportedFormat_ThrowsUnsupportedOperationException() {
        IssueExportFactory emptyFactory = new IssueExportFactory(List.of(), List.of());

        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                () -> emptyFactory.getStreamingExporter(ExportFormat.CSV));

        assertEquals("Streaming export format not supported: CSV", exception.getMessage());
    }

    @Test
    void constructor_WithNullExportersLists_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new IssueExportFactory(null, List.of(streamingCsvExporter)));

        assertThrows(NullPointerException.class,
                () -> new IssueExportFactory(List.of(csvExporter), null));
    }
}
