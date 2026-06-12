package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.repository.ProjectRepository;
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
    public Project createProject(String name, String description){
        if(projectRepository.existsByName(name)){
            throw new RuntimeException("Project with the same name already exists.");
        }
        Project project = Project.builder()
                .name(name)
                .description(description)
                .build();
        return projectRepository.save(project);
    }   

    @Transactional(readOnly = true)
    public Project getProjectById(UUID id){
        return projectRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Project not found.")); 
    }

    @Transactional(readOnly = true)
    public List<Project> getAllProjects(){
        return projectRepository.findAll();
    }



}
