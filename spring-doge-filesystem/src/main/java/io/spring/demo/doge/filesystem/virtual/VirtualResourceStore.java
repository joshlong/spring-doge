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
import io.spring.demo.doge.filesystem.JailedResourcePath;
import io.spring.demo.doge.filesystem.Resource;
import io.spring.demo.doge.filesystem.ResourcePath;
import io.spring.demo.doge.filesystem.store.FileStore;
import io.spring.demo.doge.filesystem.store.FolderStore;
import io.spring.demo.doge.filesystem.store.ResourceStore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * {@link ResourceStore} for {@link VirtualFolder}s and {@link VirtualFile}s.
 * 
 * @author Phillip Webb
 */
abstract class VirtualResourceStore implements ResourceStore {

	private final RootFolderData root;

	private final JailedResourcePath path;

	public VirtualResourceStore(RootFolderData root, JailedResourcePath path) {
		this.root = root;
		this.path = path;
	}

	/**
	 * @return the root for this store
	 */
	protected final RootFolderData getRoot() {
		return this.root;
	}

	/**
	 * @return the data for this store or <tt>null</tt>
	 */
	protected Data getData() {
		return this.root.get(this.path.getUnjailedPath());
	}

	@Override
	public JailedResourcePath getPath() {
		return this.path;
	}

	@Override
	public Resource getExisting(JailedResourcePath path) {
		Data data = this.root.get(path.getUnjailedPath());
		if (data == null) {
			return null;
		}
		return data instanceof FolderData ? getFolder(path) : getFile(path);
	}

	@Override
	public Folder getFolder(JailedResourcePath path) {
		VirtualFolderStore store = new VirtualFolderStore(this.root, path);
		return new VirtualFolder(store);
	}

	@Override
	public File getFile(JailedResourcePath path) {
		VirtualFileStore store = new VirtualFileStore(this.root, path);
		return new VirtualFile(store);
	}

	@Override
	public Resource rename(String name) {
		Data data = getData();
		Assert.state(data != null, "Unable to rename missing resource "
				+ getPath().getUnjailedPath());
		JailedResourcePath destPath = getPath().getParent().get(name);
		data.setName(name);
		return getRenamedResource(destPath);
	}

	protected abstract Resource getRenamedResource(JailedResourcePath path);

	@Override
	public boolean exists() {
		Data data = getData();
		return data == null ? false : data.exists();
	}

	@Override
	public void delete() {
		Data data = getData();
		if (data != null) {
			data.delete();
		}
	}

	@Override
	public int hashCode() {
		return getPath().getUnjailedPath().hashCode();
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
		VirtualResourceStore other = (VirtualResourceStore) obj;
		boolean rtn = true;
		rtn &= ObjectUtils.nullSafeEquals(getRoot(), other.getRoot());
		rtn &= ObjectUtils.nullSafeEquals(getPath().getUnjailedPath(), other.getPath()
				.getUnjailedPath());
		return rtn;
	}

	/**
	 * {@link FileStore} for {@link VirtualFile}s.
	 */
	static class VirtualFileStore extends VirtualResourceStore implements FileStore {

		public VirtualFileStore(RootFolderData root, JailedResourcePath path) {
			super(root, path);
		}

		@Override
		protected FileData getData() {
			return (FileData) super.getData();
		}

		@Override
		protected Resource getRenamedResource(JailedResourcePath path) {
			VirtualFileStore store = new VirtualFileStore(getRoot(), path);
			return new VirtualFile(store);
		}

		@Override
		public void create() {
			getRoot().createFile(getPath().getUnjailedPath());
		}

		@Override
		public InputStream getInputStream() {
			FileData data = getData();
			Assert.state(data != null, "Unable to read from missing resource "
					+ getPath().getUnjailedPath());
			return data.getInputStream();
		}

		@Override
		public OutputStream getOutputStream() {
			FileData data = getOrCreateFileData();
			return data.getOutputStream();
		}

		public void write(File file) {
			FileData data = getOrCreateFileData();
			data.write(file);
		}

		private FileData getOrCreateFileData() {
			FileData data = getData();
			if (data == null) {
				create();
				data = getData();
			}
			return data;
		}

		@Override
		public long getSize() {
			FileData data = getData();
			if (data == null) {
				return 0L;
			}
			return data.getSize();
		}

		@Override
		public long getLastModified() {
			FileData data = getData();
			if (data == null) {
				return -1L;
			}
			return data.getLastModified();
		}

		@Override
		public void touch() {
			FileData data = getData();
			if (data == null) {
			}
			data.touch();
		}
	}

	/**
	 * {@link FolderStore} for {@link VirtualFolder}s.
	 */
	static class VirtualFolderStore extends VirtualResourceStore implements FolderStore {

		public VirtualFolderStore(RootFolderData root, JailedResourcePath path) {
			super(root, path);
		}

		public VirtualFolderStore() {
			super(new RootFolderData(null, null), new JailedResourcePath());
		}

		@Override
		protected FolderData getData() {
			return (FolderData) super.getData();
		}

		@Override
		protected Resource getRenamedResource(JailedResourcePath path) {
			VirtualFolderStore store = new VirtualFolderStore(getRoot(), path);
			return new VirtualFolder(store);
		}

		@Override
		public void create() {
			getRoot().getOrCreateFolder(getPath().getUnjailedPath());
		}

		@Override
		public Iterable<String> list() {
			FolderData data = getData();
			if (data == null) {
				return Collections.emptyList();
			}
			return data.list();
		}
	}

	/**
	 * The actual data for virtual resources. References to {@link Data} should be
	 * obtained anew as required, never cache or keep reference to data objects (with the
	 * exception of the {@link RootFolderData root}.
	 */
	private static abstract class Data {

		private final FolderData parent;

		private String name;

		public Data(FolderData parent, String name) {
			this.parent = parent;
			this.name = name;
			if (parent != null) {
				parent.getChildren().add(this);
			}
		}

		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean exists() {
			return true;
		}

		public void delete() {
			this.parent.getChildren().remove(this);
		}
	}

	private static class FolderData extends Data {

		private final List<Data> children = new ArrayList<Data>();

		public FolderData(FolderData parent, String name) {
			super(parent, name);
		}

		protected List<Data> getChildren() {
			return this.children;
		}

		public Iterable<String> list() {
			return new Iterable<String>() {

				@Override
				public Iterator<String> iterator() {
					final Iterator<Data> iterator = getChildren().iterator();

					return new Iterator<String>() {

						@Override
						public boolean hasNext() {
							return iterator.hasNext();
						}

						@Override
						public String next() {
							return iterator.next().getName();
						}

						@Override
						public void remove() {
							throw new UnsupportedOperationException();
						}
					};
				}
			};
		}

	}

	private static class RootFolderData extends FolderData {

		private boolean exists;

		public RootFolderData(FolderData parent, String name) {
			super(parent, name);
		}

		@Override
		public boolean exists() {
			return this.exists;
		}

		@Override
		public void setName(String name) {
			throw new UnsupportedOperationException("Unable to set name of root folder");
		}

		@Override
		public void delete() {
			getChildren().clear();
			this.exists = false;
		}

		public Data get(ResourcePath path) {
			if (path.isRootPath()) {
				return this;
			}
			Data parent = get(path.getParent());
			if (parent != null && parent instanceof FolderData) {
				for (Data child : ((FolderData) parent).getChildren()) {
					if (path.getName().equals(child.getName())) {
						return child;
					}
				}
			}
			return null;
		}

		public FolderData getOrCreateFolder(ResourcePath path) {
			if (path.isRootPath()) {
				this.exists = true;
				return this;
			}
			Data existing = get(path);
			if (existing == null) {
				return new FolderData(getOrCreateFolder(path.getParent()), path.getName());
			}
			return (FolderData) existing;
		}

		public FileData createFile(ResourcePath path) {
			Assert.state(!path.isRootPath(), "File path must not be root");
			Data existing = get(path);
			if (existing == null) {
				return new FileData(getOrCreateFolder(path.getParent()), path.getName());
			}
			return (FileData) existing;
		}
	}

	private static class FileData extends Data {

		private File source;

		private byte[] bytes;

		private long lastModified = -1;

		public FileData(FolderData parent, String name) {
			super(parent, name);
		}

		public long getLastModified() {
			return this.lastModified;
		}

		public void touch() {
			this.lastModified = System.currentTimeMillis();
		}

		public long getSize() {
			if (this.source != null) {
				return this.source.getSize();
			}
			return this.bytes == null ? 0L : this.bytes.length;
		}

		public void write(File source) {
			this.bytes = null;
			this.source = source;
			this.lastModified = this.source.getLastModified();
		}

		public OutputStream getOutputStream() {
			return new ByteArrayOutputStream() {

				@Override
				public void close() throws IOException {
					super.close();
					FileData.this.source = null;
					FileData.this.bytes = toByteArray();
					FileData.this.lastModified = System.currentTimeMillis();
				}
			};
		}

		public InputStream getInputStream() {
			if (this.source != null) {
				return this.source.getContent().asInputStream();
			}
			Assert.state(this.bytes != null, "File does not exist");
			return new ByteArrayInputStream(this.bytes);
		}
	}

}
