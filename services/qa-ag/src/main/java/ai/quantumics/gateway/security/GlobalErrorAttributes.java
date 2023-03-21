package ai.quantumics.gateway.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.net.ConnectException;
import java.util.Map;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable error = this.getError(request);
        System.out.println("Error :" + error);
        Map<String, Object> errorMap = super.getErrorAttributes(request, options);
        if(error instanceof HttpClientErrorException) {
            HttpClientErrorException exception = (HttpClientErrorException) error;
            System.out.println(exception.getMessage());
            String[] message = exception.getMessage().split(": ");
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                ErrorResponse errorResponse = objectMapper.readValue(message[1].substring(1, message[1].length() - 1), ErrorResponse.class);
                errorMap.put("code", exception.getRawStatusCode());
                errorMap.put("message",errorResponse.getMessage());
                errorMap.put("details", errorResponse.getDetails());
                errorMap.remove("status");
                errorMap.remove("requestId");
                errorMap.remove("path");
                errorMap.remove("error");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else if(error instanceof ResourceAccessException) {
            ResourceAccessException exception = (ResourceAccessException) error;
            errorMap.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorMap.put("message", "Connection refused");
            errorMap.put("details", exception.getMessage());
            errorMap.remove("status");
            errorMap.remove("requestId");
            errorMap.remove("path");
            errorMap.remove("error");
        } else if (error instanceof ConnectException) {
            ConnectException exception = (ConnectException) error;
            errorMap.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorMap.put("message", "Connection refused");
            errorMap.put("details", exception.getMessage());
            errorMap.remove("status");
            errorMap.remove("requestId");
            errorMap.remove("path");
            errorMap.remove("error");
        }
        return errorMap;
    }



}
