package it.unina.bugboard.bugboard_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequestDTO {
    //invio dati per creazione nuovo progetto
    private String name;
    private String description;

}

