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

package io.spring.demo.doge.filesystem;

import io.spring.demo.doge.filesystem.exception.ResourceDoesNotExistException;
import io.spring.demo.doge.filesystem.exception.ResourceExistsException;

/**
 * A file {@link Resource} that may be stored on a physical disk or using some other
 * mechanism.
 * 
 * @author Phillip Webb
 * @see Folder
 */
public interface File extends Resource {

	@Override
	File moveTo(Folder folder);

	@Override
	File copyTo(Folder folder);

	@Override
	File rename(String name) throws ResourceExistsException;

	/**
	 * Returns the size in bytes of the virtual file.
	 * @return the size in bytes
	 */
	long getSize();

	/**
	 * Gets the time this file object was last modified. The time is measured in
	 * milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
	 * @return the time this file object was last modified; or 0 if the file object does
	 * not exist, if an I/O error occurred, or if the operation is not supported
	 */
	long getLastModified();

	/**
	 * Update the {@link #getLastModified() last modified timestamp} of the file to now.
	 * @throws ResourceDoesNotExistException if the resource does not exist
	 */
	void touch() throws ResourceDoesNotExistException;

	/**
	 * Provides access to file content. Calling any method on a file that does not
	 * {@link #exists() exist} will cause it to be created.
	 * @return the file content
	 */
	FileContent getContent();
}
