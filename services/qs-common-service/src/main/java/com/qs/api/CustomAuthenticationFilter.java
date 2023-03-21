package com.qs.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class CustomAuthenticationFilter {
    public void doFilter(
            final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain chain)
            throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        final String uri = httpRequest.getRequestURL().toString();
        final String Auth = httpRequest.getHeader("Authorization");
        log.info("{}", uri);
        if (isContains(uri)
                || isContains(uri, "/authorization")
                || isContains(uri, "/health")
                || uri.endsWith(".html")
                || isContains(uri, "actuator")
                || isContains(uri, "/wsinit")
                || uri.endsWith("api-docs")) {
            log.debug("Intercepter for URL --  {}", uri);
            chain.doFilter(servletRequest, servletResponse);
        } else if (!StringUtils.isEmpty(Auth)) {
            log.debug("Interceptor Found Auth Header! {}", Auth);
            chain.doFilter(httpRequest, servletResponse);
        } else {
            ((HttpServletResponse) servletResponse).setHeader("Content-Type", "application/json");
            ((HttpServletResponse) servletResponse).setStatus(401);
            servletResponse
                    .getOutputStream()
                    .write(
                            "UnAuthorized Operation, you are not allowed to perform this operation".getBytes());
            return;
        }
    }

    private boolean isContains(final String uri) {
        return isContains(uri, "/users");
    }

    private boolean isContains(final String uri, final String s) {
        return uri.contains(s);
    }
}
