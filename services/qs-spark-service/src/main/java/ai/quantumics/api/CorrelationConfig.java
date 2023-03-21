/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class CorrelationConfig implements WebMvcConfigurer {
  final CorrelationInterceptor corInterceptor;

  public CorrelationConfig(CorrelationInterceptor corInterceptorCi) {
      corInterceptor = corInterceptorCi;
  }

  @Override
  public void addInterceptors(final InterceptorRegistry registry) {
    registry.addInterceptor(corInterceptor);
  }

  @Override
  public void configureAsyncSupport(final AsyncSupportConfigurer configurer) {
    configurer.setTaskExecutor(getAsyncExecutor());
    configurer.setDefaultTimeout(50_000);
  }

  @Bean(name = "qsThreadPool")
  public ThreadPoolTaskExecutor getAsyncExecutor() {
    final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(500);
    executor.setMaxPoolSize(1000);
    executor.setQueueCapacity(500);
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.setThreadNamePrefix("qsThreadPool-");
    executor.initialize();
    return executor;
  }
}
