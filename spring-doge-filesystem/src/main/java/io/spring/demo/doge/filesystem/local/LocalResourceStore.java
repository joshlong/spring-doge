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

import io.spring.demo.doge.filesystem.File;
import io.spring.demo.doge.filesystem.Folder;
import io.spring.demo.doge.filesystem.JailedResourcePath;
import io.spring.demo.doge.filesystem.Resource;
import io.spring.demo.doge.filesystem.exception.ResourceException;
import io.spring.demo.doge.filesystem.exception.ResourceTypeMismatchException;
import io.spring.demo.doge.filesystem.store.FileStore;
import io.spring.demo.doge.filesystem.store.FolderStore;
import io.spring.demo.doge.filesystem.store.ResourceStore;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * {@link ResourceStore}s for {@link LocalFile} and {@link LocalFolder}.
 * 
 * @author Phillip Webb
 */
abstract class LocalResourceStore implements ResourceStore {

	private final java.io.File root;

	private final JailedResourcePath path;

	private final java.io.File file;

	public LocalResourceStore(java.io.File root, JailedResourcePath path) {
		Assert.notNull(root, "Root must not be null");
		Assert.notNull(path, "Path must not be null");
		Assert.state(root.exists(), "The root folder '" + root + "' does not exist");
		Assert.state(root.isDirectory(), "The root '" + root + "' is not a folder");
		this.root = root;
		this.path = path;
		this.file = getFileForPath(path);
	}

	protected final java.io.File getRoot() {
		return this.root;
	}

	protected final java.io.File getFile() {
		return this.file;
	}

	protected final java.io.File getFileForPath(JailedResourcePath path) {
		return new java.io.File(getRoot(), path.getUnjailedPath().toString());
	}

	@Override
	public JailedResourcePath getPath() {
		return this.path;
	}

	@Override
	public Resource getExisting(JailedResourcePath path) {
		java.io.File file = getFileForPath(path);
		if (!file.exists()) {
			return null;
		}
		return file.isDirectory() ? getFolder(path) : getFile(path);
	}

	@Override
	public Folder getFolder(JailedResourcePath path) {
		LocalFolderStore store = new LocalFolderStore(getRoot(), path);
		return new LocalFolder(store);
	}

	@Override
	public File getFile(JailedResourcePath path) {
		LocalFileStore store = new LocalFileStore(getRoot(), path);
		return new LocalFile(store);
	}

	@Override
	public Resource rename(String name) {
		java.io.File dest = new java.io.File(getFile().getParent(), name);
		JailedResourcePath destPath = getPath().getParent().get(name);
		if (!getFile().renameTo(dest)) {
			throw new ResourceException("Unable to rename file '" + getFile() + "' to '"
					+ dest + "'");
		}
		return getRenamedResource(destPath);
	}

	protected abstract Resource getRenamedResource(JailedResourcePath path);

	@Override
	public boolean exists() {
		return this.file.exists();
	}

	@Override
	public void delete() {
		if (!this.file.delete()) {
			throw new ResourceException("Unable to delete " + this.file);
		}
	}

	@Override
	public int hashCode() {
		return getFile().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		LocalResourceStore other = (LocalResourceStore) obj;
		return ObjectUtils.nullSafeEquals(getFile(), other.getFile());
	}

	static class LocalFileStore extends LocalResourceStore implements FileStore {

		public LocalFileStore(java.io.File root, JailedResourcePath path) {
			super(root, path);
			if (exists() && !getFile().isFile()) {
				throw new ResourceTypeMismatchException(path.getUnjailedPath(), false);
			}
		}

		@Override
		protected Resource getRenamedResource(JailedResourcePath path) {
			LocalFileStore store = new LocalFileStore(getRoot(), path);
			return new LocalFile(store);
		}

		@Override
		public void create() {
			try {
				if (!getFile().createNewFile()) {
					throw new ResourceException("Unable to create file " + getFile());
				}
			}
			catch (IOException e) {
				throw new ResourceException(e);
			}
		}

		@Override
		public InputStream getInputStream() {
			try {
				return new BufferedInputStream(new FileInputStream(getFile()));
			}
			catch (FileNotFoundException e) {
				throw new ResourceException(e);
			}
		}

		@Override
		public OutputStream getOutputStream() {
			try {
				return new FileOutputStream(getFile(), false);
			}
			catch (FileNotFoundException e) {
				throw new ResourceException(e);
			}
		}

		@Override
		public long getSize() {
			return getFile().length();
		}

		@Override
		public long getLastModified() {
			return getFile().lastModified();
		}

		@Override
		public void touch() {
			getFile().setLastModified(System.currentTimeMillis());
		}
	}

	static class LocalFolderStore extends LocalResourceStore implements FolderStore {

		public LocalFolderStore(java.io.File root, JailedResourcePath path) {
			super(root, path);
			if (exists() && !getFile().isDirectory()) {
				throw new ResourceTypeMismatchException(path.getUnjailedPath(), true);
			}
		}

		@Override
		protected Resource getRenamedResource(JailedResourcePath path) {
			return getFolder(path);
		}

		@Override
		public void create() {
			if (!getFile().mkdirs()) {
				throw new ResourceException("Unable to create folder " + getFile());
			}
		}

		@Override
		public Iterable<String> list() {
			java.io.File[] files = getFile().listFiles();
			Assert.state(files != null, "Unable to list files for " + getFile());
			List<String> filenames = new ArrayList<String>();
			for (java.io.File file : files) {
				if (file.exists()) {
					filenames.add(file.getName());
				}
			}
			return Collections.unmodifiableList(filenames);
		}
	}
}
