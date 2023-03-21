package ai.quantumics.gateway.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
public class APIHandler {


    private RestTemplate restTemplate;
    private GatewayProperties gatewayProperties;

    @Autowired
    public APIHandler(RestTemplate restTemplate, GatewayProperties gatewayProperties) {
        super();
        this.restTemplate = restTemplate;
        this.gatewayProperties = gatewayProperties;
    }

    @Retryable(value = ResourceAccessException.class, maxAttemptsExpression = "#{${retry.max.attempts}}", backoff = @Backoff(delayExpression = "#{${retry.backoff.delay}}"))
    public ResponseEntity<String> validateToken(HttpHeaders headers) throws HttpClientErrorException, ResourceAccessException {
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        List<RouteDefinition> routeDefinitions = gatewayProperties.getRoutes();
        RouteDefinition routeDefinition = routeDefinitions.stream().filter(routeDefinition1 -> routeDefinition1.getId().equals("userservicesapi")).findFirst().get();
        String url = routeDefinition.getUri().toString() + "/QSUserService/api/v2/authorization/validateToken";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return response;
    }

}
