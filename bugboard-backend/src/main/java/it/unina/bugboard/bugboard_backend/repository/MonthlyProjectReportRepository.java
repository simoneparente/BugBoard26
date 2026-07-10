package it.unina.bugboard.bugboard_backend.repository;

import it.unina.bugboard.bugboard_backend.entity.MonthlyProjectReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MonthlyProjectReportRepository extends JpaRepository<MonthlyProjectReport, UUID> {

    // Find an existing monthly report for a specific project, month, and year
    Optional<MonthlyProjectReport> findByProjectIdAndReferenceMonthAndReferenceYear(
            UUID projectId, Integer referenceMonth, Integer referenceYear);
}
