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
import io.spring.demo.doge.filesystem.store.FileStore;
import io.spring.demo.doge.filesystem.store.StoredFile;
import io.spring.demo.doge.filesystem.virtual.VirtualResourceStore.VirtualFileStore;

import org.springframework.util.Assert;

/**
 * A virtual {@link File} implementation that exists only in memory.
 * 
 * @author Phillip Webb
 * @see VirtualFolder
 */
public class VirtualFile extends StoredFile {

	private final VirtualFileStore store;

	/**
	 * Package scope constructor, files should only be accessed via the
	 * {@link VirtualFolder},
	 * @param store the file store
	 */
	VirtualFile(VirtualFileStore store) {
		Assert.notNull(store, "Store must not be null");
		this.store = store;
	}

	@Override
	protected boolean write(File file) {
		this.store.write(file);
		return true;
	}

	@Override
	protected FileStore getStore() {
		return this.store;
	}

}
