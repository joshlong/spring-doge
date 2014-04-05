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

import java.math.BigInteger;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

/**
 * A Doge submitted by a {@link User}.
 * 
 * @author Phillip Webb
 */
public class Doge {

	@Id
	private BigInteger id;

	@DBRef
	private User user;

	private String title;

	public Doge(User user, String title) {
		this.user = user;
		this.title = title;
	}

	public User getUser() {
		return this.user;
	}

	public String getTitle() {
		return this.title;
	}
}
