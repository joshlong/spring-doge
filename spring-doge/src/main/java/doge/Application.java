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

package doge;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import javax.servlet.MultipartConfigElement;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.MultiPartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import doge.domain.User;
import doge.domain.UserRepository;
import doge.photo.DogePhotoManipulator;

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

	@Bean
	public InitializingBean populateTestData(UserRepository repository) {
		return () -> {
			repository.save(new User("joshlong", "Josh Long"));
			repository.save(new User("philwebb", "Phil Webb"));
			repository.findAll().forEach(System.err::println);
		};
	}

	@Bean
	public HealthIndicator<Object> mongoHealthIndicator(Mongo mongo) {
		return () -> {
			try {
				mongo.getDatabaseNames();
				return "ok";
			}
			catch (MongoException ex) {
				return "error";
			}
		};
	}

	@Configuration
	@EnableWebSocketMessageBroker
	static class WebSocketConfiguration extends AbstractWebSocketMessageBrokerConfigurer {

		@Override
		public void registerStompEndpoints(StompEndpointRegistry registry) {
			registry.addEndpoint("/doge").withSockJS();
		}

		@Override
		public void configureMessageBroker(MessageBrokerRegistry registry) {
			registry.enableSimpleBroker("/queue/", "/topic/");
			registry.setApplicationDestinationPrefixes("/app");
		}

	}

	@Configuration
	static class MetricsConfiguration {

		private static final InetSocketAddress ADDRESS = new InetSocketAddress(
				"localhost", 2003);

		@Bean
		@Conditional(GraphiteCondition.class)
		public GraphiteReporter graphiteReporter(MetricRegistry registry) {
			GraphiteReporter reporter = GraphiteReporter.forRegistry(registry)
					.prefixedWith("doge.spring.io").build(new Graphite(ADDRESS));
			reporter.start(2, TimeUnit.SECONDS);
			return reporter;
		}

		public static class GraphiteCondition implements Condition {
			@Override
			public boolean matches(ConditionContext context,
					AnnotatedTypeMetadata metadata) {
				Socket socket = new Socket();
				try {
					socket.connect(ADDRESS);
					socket.close();
					return true;
				}
				catch (Exception ex) {
					return false;
				}
			}
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
