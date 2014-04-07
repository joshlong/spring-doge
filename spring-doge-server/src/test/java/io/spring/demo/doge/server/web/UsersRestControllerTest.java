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

package io.spring.demo.doge.server.web;

import io.spring.demo.doge.server.domain.User;
import io.spring.demo.doge.server.domain.UserRepository;
import io.spring.demo.doge.server.service.DogePhotoService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Tests for {@link UsersRestController}.
 *
 * @author Josh Long
 * @author Phillip Webb
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfiguration.class })
@WebAppConfiguration
public class UsersRestControllerTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DogePhotoService dogePhotoService;

	private MockMvc mvc;

	@Before
	public void setup() {
		this.mvc = webAppContextSetup(this.context).build();
	}

	@Test
	public void getUser() throws Exception {
		given(this.userRepository.findOne("1")).willReturn(new User("1", "Phil Webb"));
		ResultActions result = this.mvc.perform(get("/users/1").accept(
				MediaType.APPLICATION_JSON));
		result.andExpect(status().isOk());
		result.andExpect(content().string(containsString("Phil Webb")));
	}

	// FIXME additional tests

}

@Configuration
@EnableAutoConfiguration
@Import(UsersRestController.class)
class TestConfiguration {

	@Bean
	public UserRepository userRepository() {
		return mock(UserRepository.class);
	}

	@Bean
	public DogePhotoService dogePhotoService() {
		return mock(DogePhotoService.class);
	}

}
