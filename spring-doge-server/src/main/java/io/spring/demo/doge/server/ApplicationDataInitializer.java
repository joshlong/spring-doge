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

import io.spring.demo.doge.server.users.User;
import io.spring.demo.doge.server.users.UserRepository;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Insert some sample data to use.
 *
 * @author Josh Long
 * @author Phillip Webb
 */
@Component
public class ApplicationDataInitializer implements InitializingBean {

	private final UserRepository userRepository;

	@Autowired
	public ApplicationDataInitializer(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		save(new User("joshlong", "Josh Long"));
		save(new User("philwebb", "Phil Webb"));
	}

	private void save(User user) {
		this.userRepository.save(user);
	}

}
