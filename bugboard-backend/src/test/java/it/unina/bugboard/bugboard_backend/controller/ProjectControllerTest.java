package it.unina.bugboard.bugboard_backend.controller;
import it.unina.bugboard.bugboard_backend.mapper.ProjectMapper;
import it.unina.bugboard.bugboard_backend.dto.ProjectRequest;
import it.unina.bugboard.bugboard_backend.dto.ProjectResponse;
import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    @Mock
    private ProjectMapper projectMapper;

    private Project dummyProject;
    private ProjectRequest dummyRequest;

    @BeforeEach
    void setUp() {
        dummyProject = Project.builder()
                .id(UUID.randomUUID())
                .name("BugBoard Core")
                .description("Quality Gate test project")
                .build();

        dummyRequest = new ProjectRequest();
        dummyRequest.setName("BugBoard Core");
        dummyRequest.setDescription("Quality Gate test project");
    }

    @Test
    void getProjects_ReturnsOkResponse() {
        List<Project> projects = List.of(dummyProject);
        Page<Project> page = new PageImpl<>(projects);
        Pageable pageable = PageRequest.of(0, 10);
        when(projectService.getProjects(pageable)).thenReturn(page);

        ResponseEntity<?> responseEntity = projectController.getProjects(pageable);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        verify(projectService, times(1)).getProjects(pageable);
    }

    @Test
    void createProject_ReturnsCreatedResponse() {
        when(projectService.createProject(any(ProjectRequest.class))).thenReturn(dummyProject);
        
        when(projectMapper.toResponse(any(Project.class))).thenReturn(ProjectResponse.builder()
                .id(dummyProject.getId())
                .name(dummyProject.getName())
                .description(dummyProject.getDescription())
                .createdAt(dummyProject.getCreatedAt())
                .updatedAt(dummyProject.getUpdatedAt())
            .build());

        ResponseEntity<?> responseEntity = projectController.createProject(dummyRequest);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        verify(projectService, times(1)).createProject(any(ProjectRequest.class));
    }
}