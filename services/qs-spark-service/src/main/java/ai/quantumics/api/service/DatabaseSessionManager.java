/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

@Slf4j
@Service
public class DatabaseSessionManager {

  @PersistenceUnit private EntityManagerFactory entityManagerFactory;

  public void bindSession() {

    if (!TransactionSynchronizationManager.hasResource(entityManagerFactory)) {
      log.debug("  DatabaseSessionManager bindSession ");
      final EntityManager entityManager = entityManagerFactory.createEntityManager();
      TransactionSynchronizationManager.bindResource(
          entityManagerFactory, new EntityManagerHolder(entityManager));
    }
  }

  public void unbindSession() {
    log.debug(
        "Resource Manger verification {}", TransactionSynchronizationManager.getResourceMap());
    log.debug(
        "Resource Manger verification {}",
        TransactionSynchronizationManager.hasResource(entityManagerFactory));
    if (TransactionSynchronizationManager.hasResource(entityManagerFactory)) {

      final EntityManagerHolder emHolder =
          (EntityManagerHolder)
              TransactionSynchronizationManager.unbindResource(entityManagerFactory);
      EntityManagerFactoryUtils.closeEntityManager(emHolder.getEntityManager());
    }
  }
  
  public EntityManager getEntityManager() {
	if (entityManagerFactory != null) {
		log.debug("  DatabaseSessionManager bindSession ");
		final EntityManager entityManager = entityManagerFactory.createEntityManager();
		
		return entityManager;
	}
	
	return null;
  }
  
}
