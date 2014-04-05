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

import org.springframework.http.HttpStatus;

/**
 * @author pwebb
 */
public class Response<T> {

	// getHeaders

	// getHeaders()

	public static Response<Void> ok() {
		return Response.forStatus(HttpStatus.OK);
	}

	public static <T> Response<T> ok(T body) {
		return forStatus(HttpStatus.OK, body);
	}

	private static <T> Response<T> forStatus(HttpStatus status) {
		return forStatus(status, null);
	}

	private static <T> Response<T> forStatus(HttpStatus status, T body) {
		return null;
	}
}
