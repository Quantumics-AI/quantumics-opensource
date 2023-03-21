package ai.quantumics.api.user.service;

import java.util.Optional;

import ai.quantumics.api.user.model.EmailTemplate;

public interface EmailTemplateService {
  
  Optional<EmailTemplate> getEmailTemplate(int templateId);
  
  Optional<EmailTemplate> getEmailTemplate(String action);

}
