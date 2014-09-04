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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import doge.domain.User;
import doge.domain.UserRepository;
import doge.photo.DogePhotoManipulator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

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

   // public static final String CLOUD_PROFILE = "cloud" ;

    @Bean
    WebMvcConfigurerAdapter mvcViewConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
                registry.addViewController("/client").setViewName("client");
                registry.addViewController("/monitor").setViewName("monitor");
            }
        };
    }

    @Bean
    DogePhotoManipulator dogePhotoManipulator() {
        return new DogePhotoManipulator();
    }

    @Bean
    InitializingBean populateTestData(UserRepository repository) {
        return () -> {
            repository.save(new User("philwebb", "Phil Webb"));
            repository.save(new User("joshlong", "Josh Long"));
            repository.findAll().forEach(System.err::println);
        };
    }

    @Bean
    HealthIndicator mongoHealthIndicator(Mongo mongo) {
        return () -> {
            try {
                mongo.getDatabaseNames();
                return Health.status("ok").build();
            } catch (MongoException ex) {
                return Health.status("error").build();
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
                } catch (Exception ex) {
                    return false;
                }
            }
        }
    }


  //  @Profile(CLOUD_PROFILE )
    @Configuration
    static class CloudConfiguration extends AbstractCloudConfig {

        @Bean
        MongoDbFactory mongo() {
            return this.connectionFactory().mongoDbFactory();
        }
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(Application.class)
             //   .profiles(CLOUD_PROFILE)
                .run(args);
    }

}
