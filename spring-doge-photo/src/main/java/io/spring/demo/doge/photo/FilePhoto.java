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

package io.spring.demo.doge.photo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.util.Assert;

/**
 * {@link Photo} backed by a {@link java.io.File}.
 * 
 * @author Phillip Webb
 */
public class FilePhoto implements Photo {

	private final File file;

	public FilePhoto(File file) {
		Assert.notNull(file, "File must not be null");
		Assert.isTrue(file.exists(), "File '" + file + "' does not exist");
		Assert.isTrue(file.isFile(), "File '" + file + "' is not a file");
		this.file = file;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(this.file);
	}

}
