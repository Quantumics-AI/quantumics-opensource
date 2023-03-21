/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.user.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import ai.quantumics.api.user.model.Projects;
import ai.quantumics.api.user.model.QsUserV2;
import ai.quantumics.api.user.repo.ProjectRepository;
import ai.quantumics.api.user.service.ProjectService;
import ai.quantumics.api.user.service.UserServiceV2;
import ai.quantumics.api.user.vo.UserInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProjectServiceImpl implements ProjectService {

  private final ProjectRepository projectRepo;
  
  private final UserServiceV2 userServiceV2;
  
  //@Autowired
  //private EntityManagerFactory entityManagerFactory;

  public ProjectServiceImpl(ProjectRepository projectRepo, UserServiceV2 userServiceV2) {
    this.projectRepo = projectRepo;
    this.userServiceV2 = userServiceV2;
  }

  @Override
  public List<Projects> getEmAll() {
    return projectRepo.findAll();
  }

  @Override
  public Projects getProject(final int projectId) {
    Optional<Projects> projectsOp = projectRepo.findByProjectId(projectId); 
    
    return (projectsOp.isPresent()) ? projectsOp.get() : null; 
  }
  
  @Override
  public Projects getProject(int projectId, int userId) {
    QsUserV2 qsUserV2 = null;
    Optional<Projects> projectsOp = Optional.ofNullable(null);
    try {
      qsUserV2 = userServiceV2.getActiveUserById(userId);
      userId = "user".equals(qsUserV2.getUserRole()) ? qsUserV2.getUserParentId() : qsUserV2.getUserId();
      projectsOp = projectRepo.findByProjectIdAndUserId(projectId, userId); 
    } catch (SQLException sQLException) {
      log.error("User is either inactive or not exists", sQLException);
    }
    return (projectsOp.isPresent()) ? projectsOp.get() : null; 
  }
  
  @Override
  public Optional<Projects> getProjectOp(int projectId) {
    return projectRepo.findByProjectId(projectId);
  }

  @Override
  public List<Projects> getProjectsForUser(final int userId) throws SQLException {
    final List<Projects> findByUserId = projectRepo.findByUserIdAndActiveTrue(userId);
    return findByUserId;
  }

  @Override
  public String getSchemaName(final int projectId) {
    final Optional<Projects> findByProjectIdOp = projectRepo.findByProjectId(projectId);
    return (findByProjectIdOp.isPresent()) ? findByProjectIdOp.get().getDbSchemaName() : "";
  }

  @Override
  public String getSchemaNameFromUser(final int userId) {
    final List<Projects> findByProjectId = projectRepo.findByUserIdAndActiveTrue(userId);
    return findByProjectId.get(0).getDbSchemaName();
  }

  @Override
  public Projects saveProject(final Projects project) {
    return projectRepo.saveAndFlush(project);
  }

  @Override
  public List<UserInfo> getProjectMembers(int projectId) {
    List<UserInfo> projectMembers = new ArrayList<>();
    
    /*Query query = (TypedQuery<UserInfo>) entityManagerFactory.createEntityManager().createQuery(PROJECT_MEMBERS_QUERY);
    query.setParameter("projectId", projectId);
    List<Object[]> resultsets = query.getResultList();
    List<UserInfo> projectMembers = new ArrayList<>();
    resultsets.forEach(
          resultset -> {
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId((int) resultset[0]);
            userInfo.setRole((String) resultset[1]);
            userInfo.setFirstName((String) resultset[2]);
            userInfo.setLastName((String) resultset[3]);
            userInfo.setEmail((String) resultset[4]);
            userInfo.setPhone((String) resultset[5]);
            userInfo.setCompany((String) resultset[6]);
            userInfo.setCountry((String) resultset[7]);
            String fullName = userInfo.getFirstName() + (userInfo.getLastName()!=null ? ", "+userInfo.getLastName() : "");
            userInfo.setFullName(fullName);
            projectMembers.add(userInfo);
          }
        );*/
    
    return projectMembers;
  }
  
  @Override
  public List<Projects> getDeletedProjects() {
    List<Projects> deletedProjects = projectRepo.findByDeletionDateNotNull();
    if(deletedProjects != null && !deletedProjects.isEmpty()) {
      List<Projects> finalList = new ArrayList<>();
      
      deletedProjects.stream().forEach((project) -> {
        Date deletionDate = project.getDeletionDate();
        String str = deletionDate.toString();
        str = str.substring(0, str.indexOf(" "));
        LocalDate dt1 = DateTime.now().toLocalDate();
        DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate dt2 = DateTime.parse(str, df).toLocalDate();
        //int daysBtn = Days.daysBetween(dt1, dt2).getDays();
        int daysBtn = Minutes.minutesBetween(dt2, dt1).getMinutes();
        
        log.info("Days between the project deletion date: {} and current date: {} is: {}", dt2, dt1, daysBtn);
        
        if(daysBtn >= 3) {
          finalList.add(project);
        }
      });
      
      log.info("Projects to be deleted are: {}", finalList);
      
      return finalList;
    }
    
    log.info("No projects to cleanup..");
    return Collections.emptyList(); 
  }
  
  @Override
  public boolean deleteProjects(List<Projects> deletedProjects) {
    try {
      projectRepo.deleteAll(deletedProjects);
      
      return true;
    }catch(Exception e) {
      return false;
    }
  }
  
}
