package ai.quantumics.api.service;

import java.util.Optional;
import ai.quantumics.api.model.EmailTemplate;

public interface EmailTemplateService {
  
  Optional<EmailTemplate> getEmailTemplate(int templateId);
  
  Optional<EmailTemplate> getEmailTemplate(String action);

}
