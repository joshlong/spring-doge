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

package io.spring.demo.doge.server.service;

import io.spring.demo.doge.photo.Photo;
import io.spring.demo.doge.photo.manipulate.PhotoManipulator;
import io.spring.demo.doge.server.domain.DogePhoto;
import io.spring.demo.doge.server.domain.DogePhotoRepository;
import io.spring.demo.doge.server.domain.User;
import io.spring.demo.doge.server.domain.UserRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * @author Josh Long
 * @author Phillip Webb
 */
@Service
public class DogeService {

	private final UserRepository userRepository;

	private final DogePhotoRepository dogePhotoRepository;

	private final PhotoManipulator photoManipulator;

	private final GridFsTemplate filesystem;

	@Autowired
	public DogeService(UserRepository userRepository,
			DogePhotoRepository dogePhotoRepository, PhotoManipulator photoManipulator,
			GridFsTemplate filesystem) {
		this.userRepository = userRepository;
		this.dogePhotoRepository = dogePhotoRepository;
		this.photoManipulator = photoManipulator;
		this.filesystem = filesystem;
	}

	public User findOne(String userId) {
		return this.userRepository.findOne(userId);
	}

	public Photo getDogePhoto(String userId, String dogeId) throws IOException {
		Assert.notNull(userId, "UserID must not be null");
		Assert.notNull(dogeId, "DogeId must not be null");
		User user = this.userRepository.findOne(userId);
		DogePhoto dogePhoto = this.dogePhotoRepository.findOneByIdAndUser(dogeId, user);
		Assert.state(dogePhoto != null, "No Doge for ID " + dogeId);
		return () -> this.filesystem.getResource(dogePhoto.getFileRef()).getInputStream();
	}

	public DogePhoto addDogePhoto(String userId, Photo photo) throws IOException {
		Assert.notNull(userId, "UserId must not be null");
		Assert.notNull(photo, "Photo must not be null");
		User user = this.userRepository.findOne(userId);
		photo = this.photoManipulator.manipulate(photo);
		String fileRef = UUID.randomUUID() + ".jpg";
		try (InputStream inputStream = photo.getInputStream()) {
			this.filesystem.store(inputStream, fileRef);
		}
		return this.dogePhotoRepository.save(new DogePhoto(user, fileRef));
	}

}
