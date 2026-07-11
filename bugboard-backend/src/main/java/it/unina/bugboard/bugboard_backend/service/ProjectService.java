package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.dto.ProjectRequest;
import it.unina.bugboard.bugboard_backend.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional
    public Project createProject(ProjectRequest projectrequest){
        if(projectRepository.existsByName(projectrequest.getName())){
            throw new IllegalArgumentException("Project with the same name already exists.");
        }
        Project project = Project.builder()
                .name(projectrequest.getName())
                .description(projectrequest.getDescription())
                .build();
        return projectRepository.save(project);
    }   

    @Transactional(readOnly = true)
    public Project getProjectById(UUID id){
        return projectRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Project not found.")); 
    }

    @Transactional(readOnly = true)
    public Page<Project> getAllProjects(Pageable pageable){
        return projectRepository.findAll(pageable);
    }



}
