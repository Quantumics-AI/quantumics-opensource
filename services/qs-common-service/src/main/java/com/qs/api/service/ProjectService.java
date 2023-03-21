package com.qs.api.service;

import com.qs.api.model.Projects;

import java.sql.SQLException;
import java.util.List;

public interface ProjectService {
    List<Projects> getProjectsForUser(int userId) throws SQLException;

    String getScheaNameFromUser(int userId);

    Projects saveProject(Projects project);

    Projects getProject(int projectId);

    String getScheaName(int projectId);

    List<Projects> getEmAll();
}
