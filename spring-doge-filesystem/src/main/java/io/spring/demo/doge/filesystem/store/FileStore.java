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

package io.spring.demo.doge.filesystem.store;

import io.spring.demo.doge.filesystem.File;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Store for a single {@link File}.
 * 
 * @author Phillip Webb
 * @see StoredFile
 */
public interface FileStore extends ResourceStore {

	/**
	 * Access the file content as an input stream.
	 * @return an input stream to read content
	 */
	InputStream getInputStream();

	/**
	 * Access the file content as an output stream.
	 * @return an output stream to write content
	 */
	OutputStream getOutputStream();

	/**
	 * Return the size of the file.
	 * @return the file size
	 */
	long getSize();

	/**
	 * Return the date/time that the file was last modified.
	 * @return the last modified timestamp
	 */
	long getLastModified();

	/**
	 * Touch the file to update the {@link #getLastModified()} date.
	 */
	void touch();
}
