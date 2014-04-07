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

import io.spring.demo.doge.filesystem.File;
import io.spring.demo.doge.filesystem.mongo.MongoFolder;
import io.spring.demo.doge.photo.Photo;
import io.spring.demo.doge.photo.manipulate.DogePhotoManipulator;
import io.spring.demo.doge.server.photos.DogePhoto;
import io.spring.demo.doge.server.photos.DogePhotoRepository;
import io.spring.demo.doge.server.users.User;
import io.spring.demo.doge.server.users.UserRepository;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

/**
 * @author Josh Long
 * @author Phillip Webb
 */
@Service
public class DogeService {

	private final UserRepository userRepository;
	private final DogePhotoRepository dogePhotoRepository;
	private final MongoFolder folder;
	private final DogePhotoManipulator dogePhotoManipulator;

	@Autowired
	public DogeService(UserRepository userRepository,
			DogePhotoRepository dogePhotoRepository, MongoFolder resources,
			DogePhotoManipulator dogePhotoManipulator) {
		this.userRepository = userRepository;
		this.dogePhotoManipulator = dogePhotoManipulator;
		this.dogePhotoRepository = dogePhotoRepository;
		this.folder = resources;
	}

	public DogePhoto getDogePhotoById(BigInteger id) {
		return this.dogePhotoRepository.findOne(id);
	}

	public User getUserById(String user) {
		return this.userRepository.findOne(user);
	}

	public DogePhoto addDogePhoto(String id, String title, MediaType mediaType,
			Photo uploadedPhoto) throws IOException {
		User user = this.userRepository.findOne(id);
		String finalTitle = StringUtils.hasText(title) ? title : "";
		DogePhoto photo = this.dogePhotoRepository.save(new DogePhoto(user, mediaType
				.toString(), finalTitle));
		BigInteger photoId = photo.getId();
		if (null != uploadedPhoto && uploadedPhoto.getInputStream() != null) {
			File file = this.folder.getFile(fileNameForFile(photoId));
			file.createIfMissing();
			Photo somethingToWrite = this.dogePhotoManipulator.manipulate(uploadedPhoto);
			try (InputStream manipulatedPhotoBytes = somethingToWrite.getInputStream();
					OutputStream fileOutputStream = file.getContent().asOutputStream()) {
				StreamUtils.copy(manipulatedPhotoBytes, fileOutputStream);
			}
		}
		return photo;

	}

	public InputStream readDogePhotoContents(String username, BigInteger bigInteger) {
		File file = this.folder.getFile(fileNameForFile(bigInteger));
		return file.getContent().asInputStream();
	}

	public DogePhoto readDogePhoto(String username, BigInteger dogeId) {
		return this.dogePhotoRepository.findOne(dogeId);
	}

	private String fileNameForFile(BigInteger photoId) {
		return photoId.toString();
	}
}
