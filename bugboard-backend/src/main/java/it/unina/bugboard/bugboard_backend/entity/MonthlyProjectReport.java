package it.unina.bugboard.bugboard_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("MONTHLY_PROJECT")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyProjectReport extends ProjectReport {

    @Column(name = "reference_month")
    private Integer referenceMonth;

    @Column(name = "reference_year")
    private Integer referenceYear;
}
