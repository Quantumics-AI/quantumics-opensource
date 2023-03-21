/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.standard.TomcatRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import javax.annotation.PostConstruct;

//@Configuration
//@EnableWebSocketMessageBroker
public class QsWebSocketConfiguration implements WebSocketMessageBrokerConfigurer {
  
  private WebSocketMessageBrokerStats webSocketMessageBrokerStats;

  public void setWebSocketMessageBrokerStats(WebSocketMessageBrokerStats webSocketMessageBrokerStats) {
	  this.webSocketMessageBrokerStats = webSocketMessageBrokerStats;
	  init();
  }
  
  @Override
  public void configureClientInboundChannel(final ChannelRegistration registration) {
    registration.taskExecutor().corePoolSize(10);
    registration.taskExecutor().maxPoolSize(20);
  }

  @Override
  public void configureClientOutboundChannel(final ChannelRegistration registration) {
    registration.taskExecutor().maxPoolSize(20);
  }

  @Override
  public void configureMessageBroker(final MessageBrokerRegistry configRegistry) {
    configRegistry
        /*   .setApplicationDestinationPrefixes("/app")
        .enableStompBrokerRelay("/topic")
        .setRelayHost("localhost")
        .setRelayPort(61613)
        .setClientLogin("guest")
        .setClientPasscode("guest");*/
        .setApplicationDestinationPrefixes("/app")
        .setPreservePublishOrder(true)
        .enableSimpleBroker("/topic", "/queue");
  }

  //@PostConstruct
  public void init() {
    webSocketMessageBrokerStats.setLoggingPeriod(600000); // 30Min
  }

  @Override
  public void registerStompEndpoints(final StompEndpointRegistry registry) {
    registry
        .addEndpoint("/wsinit")
        .setAllowedOrigins("*")
        // .setHandshakeHandler(new QsCustomHandShakeHandler())
        .setHandshakeHandler(new DefaultHandshakeHandler(new TomcatRequestUpgradeStrategy()))
        .withSockJS();
  }
}
