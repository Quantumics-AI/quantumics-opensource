package ai.quantumics.api.user.service.impl;

import java.util.Optional;
import org.springframework.stereotype.Service;
import ai.quantumics.api.user.model.EmailTemplate;
import ai.quantumics.api.user.repo.EmailTemplateRepository;
import ai.quantumics.api.user.service.EmailTemplateService;

@Service
public class EmailTemplateServiceImpl implements EmailTemplateService {
  
  private final EmailTemplateRepository emailTemplateRepo;
  
  public EmailTemplateServiceImpl(EmailTemplateRepository emailTemplateRepo) {
    this.emailTemplateRepo = emailTemplateRepo;
  }

  @Override
  public Optional<EmailTemplate> getEmailTemplate(int templateId) {
    return emailTemplateRepo.findByTemplateId(templateId);
  }

  @Override
  public Optional<EmailTemplate> getEmailTemplate(String action) {
    return emailTemplateRepo.findByAction(action);
  }

}
