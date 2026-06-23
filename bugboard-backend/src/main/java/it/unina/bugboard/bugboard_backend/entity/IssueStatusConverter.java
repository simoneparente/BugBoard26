package it.unina.bugboard.bugboard_backend.entity;

import it.unina.bugboard.bugboard_backend.entity.state.*;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class IssueStatusConverter implements AttributeConverter<Status, String> {
    @Override
    public String convertToDatabaseColumn(Status status) {
        return status != null ? status.getName() : null;
    }

    @Override
    public Status convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        
        return switch (dbData.toUpperCase()) {
            case "TO_BE_ASSIGNED" -> new ToBeAssigned();
            case "ASSIGNED" -> new Assigned();
            case "IN_PROGRESS" -> new InProgress();
            case "MARKED_FOR_REVIEW" -> new MarkedForReview();
            default -> throw new IllegalArgumentException("Stato sconosciuto: " + dbData);
        };
    }
}
