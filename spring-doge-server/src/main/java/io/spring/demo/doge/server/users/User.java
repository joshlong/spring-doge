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

package io.spring.demo.doge.server.users;

import org.springframework.data.annotation.Id;

/**
 * A single user of the system. Each {@link User} may submit one or more
 * {@link io.spring.demo.doge.server.photos.DogePhoto}s.
 *
 * @author Josh Long
 * @author Phillip Webb
 * @see io.spring.demo.doge.server.photos.DogePhoto
 */
public class User {

	@Id
	private String id;

	private String name;

	public String getId() {
		return this.id;
	}

	@Override
	public String toString() {
		return "User{" + "id='" + this.id + '\'' + ", name='" + this.name + '\'' + '}';
	}

	public User(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

}
