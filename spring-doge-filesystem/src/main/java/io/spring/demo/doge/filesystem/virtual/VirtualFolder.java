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

package io.spring.demo.doge.filesystem.virtual;

import io.spring.demo.doge.filesystem.File;
import io.spring.demo.doge.filesystem.Folder;
import io.spring.demo.doge.filesystem.store.FolderStore;
import io.spring.demo.doge.filesystem.store.StoredFolder;
import io.spring.demo.doge.filesystem.virtual.VirtualResourceStore.VirtualFolderStore;

import org.springframework.util.Assert;

/**
 * A virtual {@link Folder} that exists only in memory. Virtual folders provide a
 * convenient method for manipulating existing {@link File}s and {@link Folder}s without
 * needing to create physical copies. Memory consumption for {@link VirtualFile}s is kept
 * to a minimum by only storing data when it is changed.
 * 
 * @author Phillip Webb
 */
public class VirtualFolder extends StoredFolder {

	private final VirtualFolderStore store;

	public VirtualFolder() {
		this.store = new VirtualFolderStore();
	}

	VirtualFolder(VirtualFolderStore store) {
		Assert.notNull(store, "Store must not be null");
		this.store = store;
	}

	@Override
	protected FolderStore getStore() {
		return this.store;
	}

}
