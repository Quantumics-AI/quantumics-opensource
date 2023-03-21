package ai.quantumics.gateway.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@RefreshScope
@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final RouterValidator routerValidator;
    private final APIHandler apiHandler;

    @Autowired
    public AuthenticationFilter(RouterValidator routerValidator, APIHandler apiHandler) {
        super(Config.class);
        this.routerValidator = routerValidator;
        this.apiHandler = apiHandler;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (routerValidator.isSecured.test(exchange.getRequest())) {
                ResponseEntity<String> response = apiHandler.validateToken(exchange.getRequest().getHeaders());
                HttpStatus statusCode = response.getStatusCode();
                if (statusCode.is2xxSuccessful()) {
                    return chain.filter(exchange);
                }
            }
            return chain.filter(exchange);
        };
    }

    public static class Config {
        public Config(String name) {
            this.name = name;
        }

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
