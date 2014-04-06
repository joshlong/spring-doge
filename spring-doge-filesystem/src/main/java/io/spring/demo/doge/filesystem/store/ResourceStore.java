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
import io.spring.demo.doge.filesystem.Folder;
import io.spring.demo.doge.filesystem.JailedResourcePath;
import io.spring.demo.doge.filesystem.Resource;

/**
 * Base interface for {@link FileStore} and {@link FolderStore}.
 * 
 * @author Phillip Webb
 * @see FileStore
 * @see FolderStore
 */
public interface ResourceStore {

	/**
	 * Return the path of the current resource.
	 * @return the path.
	 */
	JailedResourcePath getPath();

	/**
	 * Return an existing resource for the specified path or <tt>null</tt> if no resource
	 * exists.
	 * @param path the path
	 * @return the resource
	 */
	Resource getExisting(JailedResourcePath path);

	/**
	 * Return a folder for the specified path.
	 * @param path the path
	 * @return the folder
	 */
	Folder getFolder(JailedResourcePath path);

	/**
	 * Return a file for the specified path.
	 * @param path the path
	 * @return the file
	 */
	File getFile(JailedResourcePath path);

	/**
	 * Returns <tt>true</tt> if the resource exists.
	 * @return if the resource exists.
	 */
	boolean exists();

	/**
	 * Rename the resource.
	 * @param name the new name
	 * @return the resource of the renamed item
	 */
	Resource rename(String name);

	/**
	 * Delete the resource.
	 */
	void delete();

	/**
	 * Create the resource when it does not exist.
	 */
	void create();

	/**
	 * Implementations must provide a suitable hashcode based on the underlying resource.
	 * @return the hash code
	 */
	@Override
	public int hashCode();

	/**
	 * Implementations must provide a suitable equals based on the underlying resource.
	 * @param obj the object to compare
	 * @return <tt>true</tt> if the items are equal
	 */
	@Override
	public boolean equals(Object obj);

}
