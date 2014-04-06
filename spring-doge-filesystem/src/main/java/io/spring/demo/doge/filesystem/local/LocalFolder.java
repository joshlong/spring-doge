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

package io.spring.demo.doge.filesystem.local;

import io.spring.demo.doge.filesystem.Folder;
import io.spring.demo.doge.filesystem.JailedResourcePath;
import io.spring.demo.doge.filesystem.exception.ResourceException;
import io.spring.demo.doge.filesystem.local.LocalResourceStore.LocalFolderStore;
import io.spring.demo.doge.filesystem.store.FolderStore;
import io.spring.demo.doge.filesystem.store.StoredFolder;

import java.io.File;
import java.io.IOException;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * A {@link Folder} implementation backed by standard {@link File java.io.File}s.
 * 
 * @author Phillip Webb
 * @see LocalFile
 */
public class LocalFolder extends StoredFolder {

	private final LocalFolderStore store;

	/**
	 * Package level constructor used when accessing nested folders.
	 * @param store the store
	 */
	LocalFolder(LocalFolderStore store) {
		this.store = store;
	}

	/**
	 * Create a new {@link LocalFolder} for the specified folder.
	 * @param folder the underlying folder, eg '/home/username'
	 */
	public LocalFolder(String folder) {
		this(new java.io.File(folder));
	}

	/**
	 * Create a new {@link LocalFolder} for the specified folder.
	 * @param folder the underlying folder
	 */
	public LocalFolder(java.io.File folder) {
		this.store = new LocalFolderStore(folder, new JailedResourcePath());
	}

	@Override
	protected FolderStore getStore() {
		return this.store;
	}

	/**
	 * Returns access to the underlying local {@link File}.
	 * @return the underlying {@link File}
	 */
	public File getLocalFile() {
		return this.store.getFile();
	}

	/**
	 * @return The user home folder
	 */
	public static LocalFolder home() {
		String home = System.getProperty("user.home");
		Assert.state(StringUtils.hasLength(home), "Unable to locate home folder");
		return new LocalFolder(home);
	}

	/**
	 * Create a temporary folder.
	 * @param prefix the folder prefix
	 * @return a new temporary folder
	 */
	public static LocalFolder createTempFolder(String prefix) {
		return createTempFolder(prefix, null);
	}

	/**
	 * Create a temporary folder.
	 * @param prefix the folder prefix
	 * @param suffix the folder suffix
	 * @return a new temporary folder
	 */
	public static LocalFolder createTempFolder(String prefix, String suffix) {
		LocalFolder tempFolder;
		try {
			File tempFile = File.createTempFile(prefix, suffix);
			tempFile.delete();
			tempFile.mkdir();
			tempFolder = new LocalFolder(tempFile);
			tempFolder.createIfMissing();
			return tempFolder;
		}
		catch (IOException e) {
			throw new ResourceException(e);
		}
	}
}
