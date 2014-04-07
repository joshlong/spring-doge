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

import io.spring.demo.doge.filesystem.Folder;
import io.spring.demo.doge.photo.Photo;
import io.spring.demo.doge.photo.ResourcePhoto;
import io.spring.demo.doge.photo.manipulate.PhotoManipulator;
import io.spring.demo.doge.server.domain.Doge;
import io.spring.demo.doge.server.domain.DogeRepository;
import io.spring.demo.doge.server.domain.User;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * @author Josh Long
 * @author Phillip Webb
 */
@Service
public class DogePhotoService {

	private final DogeRepository dogeRepository;

	private final Folder photosFolder;

	private final PhotoManipulator photoManipulator;

	@Autowired
	public DogePhotoService(DogeRepository dogeRepository, Folder photosFolder,
			PhotoManipulator photoManipulator) {
		this.dogeRepository = dogeRepository;
		this.photosFolder = photosFolder;
		this.photoManipulator = photoManipulator;
	}

	public Photo getDogePhoto(User user, String dogeId) {
		Assert.notNull(user, "User must not be null");
		Assert.notNull(dogeId, "DogeId must not be null");
		Doge doge = this.dogeRepository.findOneByIdAndUser(dogeId, user);
		Assert.state(doge != null, "No Doge for ID " + dogeId);
		return new ResourcePhoto(this.photosFolder.getFile(doge.getFileRef())
				.getContent().asResource());
	}

	public Doge addDogePhoto(User user, Photo photo) throws IOException {
		Assert.notNull(user, "User must not be null");
		Assert.notNull(photo, "Photo must not be null");
		Photo manipulated = this.photoManipulator.manipulate(photo);
		String fileRef = UUID.randomUUID() + ".jpg";
		this.photosFolder.getFile(fileRef).getContent()
				.write(manipulated.getInputStream());
		return this.dogeRepository.save(new Doge(user, fileRef));
	}

}
