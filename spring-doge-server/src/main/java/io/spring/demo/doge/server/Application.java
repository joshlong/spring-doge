/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.demo.doge.server;

import io.spring.demo.doge.photo.manipulate.DogePhotoManipulator;

import javax.servlet.MultipartConfigElement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.MultiPartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import com.mongodb.Mongo;

/**
 * Application configuration and main method.
 *
 * @author Josh Long
 * @author Phillip Webb
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {

	@Bean
	public MultipartConfigElement multipartConfigElement() {
		MultiPartConfigFactory factory = new MultiPartConfigFactory();
		factory.setMaxFileSize("10Mb");
		return factory.createMultipartConfig();
	}

	@Bean
	public DogePhotoManipulator dogePhotoManipulator() {
		return new DogePhotoManipulator();
	}

	@Bean
	public GridFsTemplate gridFsTemplate(Mongo mongo, MongoTemplate mongoTemplate) {
		return new GridFsTemplate(new SimpleMongoDbFactory(mongo, "fs"),
				mongoTemplate.getConverter());
	}

	@Configuration
	@EnableScheduling
	@EnableWebSocketMessageBroker
	static class WebSocketConfiguration extends AbstractWebSocketMessageBrokerConfigurer
			implements SchedulingConfigurer {

		@Bean
		public ThreadPoolTaskScheduler reservationPool() {
			return new ThreadPoolTaskScheduler();
		}

		@Override
		public void registerStompEndpoints(StompEndpointRegistry registry) {
			registry.addEndpoint("/doge").withSockJS();
		}

		@Override
		public void configureClientOutboundChannel(ChannelRegistration registration) {
			registration.taskExecutor().corePoolSize(4).maxPoolSize(10);
		}

		@Override
		public void configureMessageBroker(MessageBrokerRegistry registry) {
			registry.enableSimpleBroker("/queue/", "/topic/");
			registry.setApplicationDestinationPrefixes("/app");
		}

		@Override
		public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
			taskRegistrar.setTaskScheduler(reservationPool());
		}

	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
