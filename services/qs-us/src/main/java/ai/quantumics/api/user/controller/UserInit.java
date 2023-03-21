package ai.quantumics.api.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@Component
public class UserInit implements ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    UserControllerV2 userControllerV2;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        userControllerV2.createUser();
    }

}