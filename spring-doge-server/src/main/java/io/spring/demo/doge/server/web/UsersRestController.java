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

import io.spring.demo.doge.photo.Photo;
import io.spring.demo.doge.photo.PhotoResource;
import io.spring.demo.doge.server.domain.DogePhoto;
import io.spring.demo.doge.server.domain.User;
import io.spring.demo.doge.server.service.DogeService;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * MVC Controller for '/users' REST endpoints.
 *
 * @author Josh Long
 * @author Phillip Webb
 */
@RestController
@RequestMapping("/users")
public class UsersRestController {

	private final DogeService dogePhotoService;

	private final SimpMessagingTemplate messaging;

	@Autowired
	public UsersRestController(DogeService dogePhotoService,
			SimpMessagingTemplate messaging) {
		this.dogePhotoService = dogePhotoService;
		this.messaging = messaging;
	}

	@RequestMapping(method = RequestMethod.GET, value = "{userId}")
	public User getUser(@PathVariable String userId) {
		return this.dogePhotoService.findOne(userId);
	}

	@RequestMapping(method = RequestMethod.POST, value = "{userId}/doge")
	public ResponseEntity<?> postDogePhoto(@PathVariable String userId,
			@RequestParam MultipartFile file, UriComponentsBuilder uriBuilder)
			throws IOException {

		Photo photo = file::getInputStream;
		DogePhoto doge = this.dogePhotoService.addDogePhoto(userId, photo);
		URI uri = uriBuilder.path("/users/{userId}/doge/{dogeId}")
				.buildAndExpand(userId, doge.getId()).toUri();

		this.messaging.convertAndSend("/topic/alarms",
				Collections.singletonMap("dogePhotoUri", uri));

		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(uri);
		return new ResponseEntity<Void>(null, headers, HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.GET, value = "{userId}/doge/{dogeId}")
	public ResponseEntity<Resource> getDogePhoto(@PathVariable String userId,
			@PathVariable String dogeId) throws IOException {
		Photo photo = this.dogePhotoService.getDogePhoto(userId, dogeId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_JPEG);
		return new ResponseEntity<Resource>(new PhotoResource(photo), headers,
				HttpStatus.OK);
	}

}
