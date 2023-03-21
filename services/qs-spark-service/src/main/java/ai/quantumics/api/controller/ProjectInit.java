package ai.quantumics.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@Component
public class ProjectInit implements ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    ProjectController projectController;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        projectController.createProject();
    }

}