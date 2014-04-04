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

package io.spring.demo.doge.filesystem.mongo;

import io.spring.demo.doge.filesystem.File;
import io.spring.demo.doge.filesystem.mongo.MongoResourceStore.MongoFileStore;
import io.spring.demo.doge.filesystem.store.FileStore;
import io.spring.demo.doge.filesystem.store.StoredFile;

import com.mongodb.gridfs.GridFS;

/**
 * A {@link File} implementation backed by a mongo {@link GridFS}.
 * 
 * @author Phillip Webb
 * @see MongoFolder
 */
public class MongoFile extends StoredFile {

	private final MongoFileStore store;

	/**
	 * Package scope constructor, files should only be accessed via the
	 * {@link MongoFolder},
	 * @param store the file store
	 */
	MongoFile(MongoFileStore store) {
		this.store = store;
	}

	@Override
	protected FileStore getStore() {
		return this.store;
	}

}
