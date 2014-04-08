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

import com.mongodb.Mongo;
import io.spring.demo.doge.filesystem.mongo.MongoFolder;
import io.spring.demo.doge.photo.manipulate.DogePhotoManipulator;
import io.spring.demo.doge.server.users.DogeUserDetailsService;
import io.spring.demo.doge.server.users.User;
import io.spring.demo.doge.server.users.UserRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.MultiPartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.OAuth2ResourceServerConfigurer;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import javax.servlet.MultipartConfigElement;
import java.util.stream.Stream;

/**
 * Such doge!
 *
 * @author Josh Long
 * @author Phillip Webb
 */
@Configuration
@ComponentScan
@EnableScheduling
@EnableAutoConfiguration
public class Application {

    /**
     * Configure multi-part file uploads.
     */
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
    public MongoFolder photoFolder(Mongo mongo) {
        return new MongoFolder(mongo.getDB("photos"));
    }

    @Bean
    InitializingBean start(UserRepository userRepository,
                           @Qualifier("dogeTaskScheduler") TaskExecutor taskExecutor) {
        return () -> {
            taskExecutor.execute (() -> {

                System.out.println ("Running firstThing()");

                Stream.of("joshlong,Josh Long,cowbell;philwebb,Phil Webb,bootiful".split(";"))
                        .map(s -> s.split(","))
                        .map(parts -> new User(parts[0], parts[1], parts[2]))
                        .filter(u -> userRepository.findOne(u.getId()) == null)
                        .forEach(u -> System.out.println(userRepository.save(u)));
            });
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@EnableWebSocketMessageBroker
@Configuration
class WebSocketConfiguration
        extends AbstractWebSocketMessageBrokerConfigurer
        implements SchedulingConfigurer {

    @Bean
    @Qualifier("dogeTaskScheduler")
    public ThreadPoolTaskScheduler dogeTaskScheduler() {
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
        taskRegistrar.setTaskScheduler(dogeTaskScheduler());
    }
}

/**
 * Demonstrates an OAuth-secured REST API (accessible under {@literal /customers}).
 * <p>
 * Get an OAuth token from the service:
 * {@literal curl -X POST -vu js-doge:secret http://localhost:8080/oauth/token -H "Accept: application/json" -d "password=password&username=user&grant_type=password&scope=read"}
 * <p>
 * Then, make a REST call substituting and transmitting the {@literal access_token} returned from the last request where I've used the symbol $AT:
 * {@literal curl http://localhost:8080/customers -H"Authorization: Bearer $AT"}
 *
 * @author Dave Syer
 * @author Josh Long
 */
@Configuration
class SecurityConfiguration {

    private final static String DOGE_RESOURCE_ID = "doge";


    //@Order( )
    @Configuration
    @EnableWebSecurity
    static class DefaultSecurityConfiguration extends WebSecurityConfigurerAdapter {

        @Autowired
        private UserDetailsService userDetailsService;

        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(this.userDetailsService);
        }


    }

    @Configuration
    @EnableResourceServer
    static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Override
        public void configure(HttpSecurity http) throws Exception {
            // @formatter:off
            http
                    .requestMatchers().antMatchers("/*", "/admin/beans").and()
                    .authorizeRequests()
                    .anyRequest().access("#oauth2.hasScope('read')");
            // @formatter:on
        }

        @Override
        public void configure(OAuth2ResourceServerConfigurer resources) throws Exception {
            resources.resourceId(DOGE_RESOURCE_ID);
        }

    }

    @Configuration
    @EnableAuthorizationServer
    static class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

        @Autowired
        private AuthenticationManager authenticationManager;

        @Override
        public void configure(OAuth2AuthorizationServerConfigurer oauthServer) throws Exception {
            oauthServer.authenticationManager(authenticationManager);
        }

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            // @formatter:off
            clients.inMemory()
                    .withClient("js-doge")
                    .authorizedGrantTypes("client_credentials", "password")
                    .authorities(DogeUserDetailsService.ROLE_USER)
                    .scopes("read")
                    .resourceIds(DOGE_RESOURCE_ID)
                    .secret("secret");
            // @formatter:on
        }

    }


}

