package com.qs.api.service.impl;

import com.qs.api.model.Projects;
import com.qs.api.repo.ProjectRepository;
import com.qs.api.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectRepository projectRepo;

    @Override
    public List<Projects> getEmAll() {
        return projectRepo.findAll();
    }

    @Override
    public Projects getProject(final int projectId) {
        return projectRepo.findByProjectId(projectId);
    }

    @Override
    public List<Projects> getProjectsForUser(final int userId) throws SQLException {
        final List<Projects> findByUserId = projectRepo.findByUserId(userId);
        return findByUserId;
    }

    @Override
    public String getScheaName(final int projectId) {
        final Projects findByProjectId = projectRepo.findByProjectId(projectId);
        return findByProjectId.getDbSchemaName();
    }

    @Override
    public String getScheaNameFromUser(final int userId) {
        final List<Projects> findByProjectId = projectRepo.findByUserId(userId);
        return findByProjectId.get(0).getDbSchemaName();
    }

    @Override
    public Projects saveProject(final Projects project) {
        return projectRepo.saveAndFlush(project);
    }
}
