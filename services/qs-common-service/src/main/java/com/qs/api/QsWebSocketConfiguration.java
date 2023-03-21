/*
 * Copyright (c) 2020. Quantumspark.ai, http://quantumspark.ai.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.qs.api;

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
    	this.init();
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
                .setHandshakeHandler(new DefaultHandshakeHandler(new TomcatRequestUpgradeStrategy()))
                .withSockJS();
    }
}
