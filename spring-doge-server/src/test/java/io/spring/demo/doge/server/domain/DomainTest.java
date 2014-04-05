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

package io.spring.demo.doge.server.domain;

import io.spring.demo.doge.server.Application;

import java.util.Iterator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Phillip Webb
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@MongoDbIntegrationTest
public class DomainTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DogeRepository dogeRepository;

	@Test
	public void addUser() throws Exception {
		this.userRepository.save(new User("philwebb", "Phil Webb"));
		User user = this.userRepository.findOne("philwebb");
		assertThat(user.getName(), equalTo("Phil Webb"));
	}

	@Test
	public void addUserAndDoge() throws Exception {
		User user = new User("joshlong", "Josh Long");
		Doge doge = new Doge(user, "test");
		this.userRepository.save(user);
		this.dogeRepository.save(doge);
		Iterator<Doge> found = this.dogeRepository.findByUser(user).iterator();
		assertThat(found.next().getTitle(), equalTo("test"));
		assertThat(found.hasNext(), equalTo(false));
	}

}
