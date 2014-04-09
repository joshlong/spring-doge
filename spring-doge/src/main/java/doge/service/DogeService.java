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

package doge.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import doge.domain.DogePhoto;
import doge.domain.DogePhotoRepository;
import doge.domain.User;
import doge.domain.UserRepository;
import doge.photo.Photo;
import doge.photo.PhotoManipulator;

/**
 * @author Josh Long
 * @author Phillip Webb
 */
@Service
public class DogeService {

	private final UserRepository userRepository;

	private final DogePhotoRepository dogePhotoRepository;

	private final PhotoManipulator manipulator;

	private final GridFsTemplate fs;

	@Autowired
	public DogeService(UserRepository userRepository,
			DogePhotoRepository dogePhotoRepository, PhotoManipulator manipulator,
			GridFsTemplate fs) {
		this.userRepository = userRepository;
		this.dogePhotoRepository = dogePhotoRepository;
		this.manipulator = manipulator;
		this.fs = fs;
	}

	public User findOne(String userId) {
		return this.userRepository.findOne(userId);
	}

	public Photo getDogePhoto(String userId, String dogeId) throws IOException {
		User user = this.userRepository.findOne(userId);
		DogePhoto dogePhoto = this.dogePhotoRepository.findOneByIdAndUser(dogeId, user);
		return () -> this.fs.getResource(dogePhoto.getFileRef()).getInputStream();
	}

	public DogePhoto addDogePhoto(String userId, Photo photo) throws IOException {
		User user = this.userRepository.findOne(userId);
		photo = this.manipulator.manipulate(photo);
		String fileRef = UUID.randomUUID() + ".jpg";
		try (InputStream inputStream = photo.getInputStream()) {
			this.fs.store(inputStream, fileRef);
		}
		return this.dogePhotoRepository.save(new DogePhoto(user, fileRef));
	}

}
