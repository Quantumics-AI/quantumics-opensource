package com.qs.api.repo;

import com.qs.api.model.Projects;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Projects, Long> {

    Projects findByProjectId(int projectId);

    List<Projects> findByUserId(int userId);
}
