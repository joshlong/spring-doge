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

import io.spring.demo.doge.photo.MultipartFilePhoto;
import io.spring.demo.doge.server.domain.Doge;
import io.spring.demo.doge.server.domain.DogeRepository;
import io.spring.demo.doge.server.domain.User;
import io.spring.demo.doge.server.domain.UserRepository;
import io.spring.demo.doge.server.service.DogeService;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

/**
 * @author Phillip Webb
 */
@RestController
@RequestMapping("/users")
public class UserController {

	private static final UserController call = MvcUriComponentsBuilder
			.controller(UserController.class);

	private final UserRepository userRepository;

	private final DogeRepository dogeRepository;

	private final DogeService dogeService;

	@Autowired
	public UserController(UserRepository userRepository, DogeRepository dogeRepository,
			DogeService dogeService) {
		this.userRepository = userRepository;
		this.dogeRepository = dogeRepository;
		this.dogeService = dogeService;
	}

	@RequestMapping(method = RequestMethod.GET, value = "{id}")
	public User getUser(@PathVariable String id) {
		return this.userRepository.findOne(id);
	}

	@RequestMapping(method = RequestMethod.POST, value = "{id}/doge")
	public ResponseEntity<Void> putDoge(@PathVariable String id,
			@RequestParam MultipartFile file) {
		User user = this.userRepository.findOne(id);
		Doge doge = this.dogeService.addDoge(user, new MultipartFilePhoto(file));
		return getResponseEntity(id, doge);
	}

	private ResponseEntity<Void> getResponseEntity(String id, Doge doge) {
		UriComponents location = MvcUriComponentsBuilder.fromMethodCall(
				call.getDoge(id, doge.getId())).build();
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(location.toUri());
		return new ResponseEntity<Void>(null, headers, HttpStatus.FOUND);
	}

	@RequestMapping(method = RequestMethod.GET, value = "{id}/doge/{doge}")
	public ResponseEntity<Resource> getDoge(@PathVariable String id,
			@PathVariable BigInteger doge) {
		User user = this.userRepository.findOne(id);
		this.dogeRepository.findOneByUserAndId(user, doge);
		return null;
	}

}
