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

import io.spring.demo.doge.filesystem.File;
import io.spring.demo.doge.filesystem.Folder;
import io.spring.demo.doge.photo.MultipartFilePhoto;
import io.spring.demo.doge.photo.Photo;
import io.spring.demo.doge.photo.manipulate.DogePhotoManipulator;
import io.spring.demo.doge.photo.manipulate.PhotoManipulator;
import io.spring.demo.doge.server.domain.User;
import io.spring.demo.doge.server.domain.UserRepository;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Phillip Webb
 */
@RestController
@RequestMapping("/user")
public class UserController {

	private final UserRepository userRepository;

	private final Folder folder;

	private final PhotoManipulator manipulator = new DogePhotoManipulator();

	@Autowired
	public UserController(UserRepository userRepository,
			@Qualifier("photoFolder") Folder folder) {
		this.userRepository = userRepository;
		this.folder = folder;
	}

	@RequestMapping(method = RequestMethod.GET, value = "{id}")
	public User getUser(@PathVariable String id) {
		return this.userRepository.findOne(id);
	}

	@RequestMapping(method = RequestMethod.POST, value = "{id}/doge")
	public void putDoge(@PathVariable String id, @RequestParam MultipartFile file)
			throws IOException {
		Photo doge = this.manipulator.manipulate(new MultipartFilePhoto(file));
		this.folder.getFile("temp.jpg").getContent().write(doge.getInputStream());
	}

	@RequestMapping(method = RequestMethod.GET, value = "{id}/doge")
	public ResponseEntity<Resource> getDoge(@PathVariable String id) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_JPEG);
		File file = this.folder.getFile("temp.jpg");
		ResponseEntity<Resource> response = new ResponseEntity<Resource>(file
				.getContent().asResource(), headers, HttpStatus.OK);
		return response;
	}

}
