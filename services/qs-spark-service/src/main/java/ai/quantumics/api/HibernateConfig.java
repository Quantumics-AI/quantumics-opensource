/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.MultiTenancyStrategy;
import org.hibernate.cfg.Environment;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration
@EnableJpaRepositories
public class HibernateConfig {
  private final JpaProperties jpaProperties;

  public HibernateConfig(JpaProperties jpaPropertiesCi) {
    jpaProperties = jpaPropertiesCi;
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(
      final DataSource dataSource,
      final MultiTenantConnectionProvider multiTenantConnectionProviderImpl,
      final CurrentTenantIdentifierResolver currentTenantIdentifierResolverImpl) {
    final Map<String, Object> properties = new HashMap<>(jpaProperties.getProperties());
    // Multi Tenant
    properties.put(Environment.MULTI_TENANT, MultiTenancyStrategy.SCHEMA);
    properties.put(Environment.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProviderImpl);
    properties.put(
        Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolverImpl);
    // Hbm2ddl
    properties.put(Environment.HBM2DDL_AUTO, "none");
    properties.put(Environment.FORMAT_SQL, "true");
    properties.put(Environment.SHOW_SQL, "true");
    properties.put(Environment.USE_SQL_COMMENTS, "true");
    properties.put(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
    // properties.put(Environment.CONNECTION_HANDLING, "DELAYED_ACQUISITION_AND_RELEASE_AFTER_STATEMENT");
    // properties.put(Environment.CONNECTION_HANDLING, );
    properties.put(
        Environment.IMPLICIT_NAMING_STRATEGY,
        "org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy");
    properties.put(
        Environment.PHYSICAL_NAMING_STRATEGY,
        "org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy");

    final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource(dataSource);
    em.setPackagesToScan("ai.quantumics");
    em.setJpaVendorAdapter(jpaVendorAdapter());
    em.setPersistenceUnitName("QsPersistenceUnit");
    em.setJpaPropertyMap(properties);
    return em;
  }

  @Bean
  public JpaVendorAdapter jpaVendorAdapter() {
    return new HibernateJpaVendorAdapter();
  }
}
