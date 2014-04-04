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
import io.spring.demo.doge.filesystem.Folder;
import io.spring.demo.doge.filesystem.JailedResourcePath;
import io.spring.demo.doge.filesystem.Resource;
import io.spring.demo.doge.filesystem.ResourcePath;
import io.spring.demo.doge.filesystem.exception.ResourceException;
import io.spring.demo.doge.filesystem.exception.ResourceTypeMismatchException;
import io.spring.demo.doge.filesystem.store.FileStore;
import io.spring.demo.doge.filesystem.store.FolderStore;
import io.spring.demo.doge.filesystem.store.ResourceStore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

/**
 * {@link ResourceStore}s for {@link MongoFile} and {@link MongoFolder}.
 * 
 * @author Phillip Webb
 */
abstract class MongoResourceStore implements ResourceStore {

	private static final String PARENT = "parent";

	private static final String RESOURCE_TYPE = "resourceType";

	private final GridFS fs;

	private final JailedResourcePath path;

	public MongoResourceStore(GridFS fs, JailedResourcePath path) {
		Assert.notNull(fs, "FS must not be null");
		Assert.notNull(path, "Path must not be null");
		this.fs = fs;
		this.path = path;
	}

	protected final GridFS getFs() {
		return this.fs;
	}

	protected final GridFSDBFile getGridFSDBFile(JailedResourcePath path, boolean required) {
		String filename = getFilename(path);
		GridFSDBFile file = getFs().findOne(filename);
		if (file == null && required) {
			throw new ResourceException("Unable to find mogo entry for " + filename);
		}
		return file;
	}

	protected GridFSInputFile create(Type type, boolean createEmptyFile) {
		Assert.notNull(type, "Type must not be null");
		GridFSInputFile file = this.fs.createFile(getFilename(getPath()));
		JailedResourcePath parent = this.path.getParent();
		if (parent != null) {
			file.put(PARENT, parent.getUnjailedPath().toString());
		}
		file.put(RESOURCE_TYPE, type.name());
		if (createEmptyFile) {
			try {
				file.getOutputStream().close();
			}
			catch (IOException e) {
				throw new ResourceException(e);
			}
		}
		return file;
	}

	private String getFilename(JailedResourcePath path) {
		return path.getUnjailedPath().toString();
	}

	@Override
	public JailedResourcePath getPath() {
		return this.path;
	}

	@Override
	public Resource getExisting(JailedResourcePath path) {
		Type type = getType(path);
		if (type == null) {
			return null;
		}
		return type == Type.FILE ? getFile(path) : getFolder(path);
	}

	/**
	 * Return the file type or <tt>null</tt>.
	 * 
	 * @param path the path to test
	 * @return the file type
	 */
	protected final Type getType(JailedResourcePath path) {
		GridFSDBFile file = getGridFSDBFile(path, false);
		if (file == null) {
			return null;
		}
		Type type = Type.valueOf((String) file.get(RESOURCE_TYPE));
		return type;
	}

	@Override
	public Folder getFolder(JailedResourcePath path) {
		MongoFolderStore store = new MongoFolderStore(getFs(), path);
		return new MongoFolder(store);
	}

	@Override
	public File getFile(JailedResourcePath path) {
		MongoFileStore store = new MongoFileStore(getFs(), path);
		return new MongoFile(store);
	}

	@Override
	public Resource rename(String name) {
		GridFSDBFile gridFSDBFile = getGridFSDBFile(getPath(), true);
		JailedResourcePath renamed = getPath().unjail().getParent().get(name);
		gridFSDBFile.put("filename", getFilename(renamed));
		gridFSDBFile.save();
		return getRenamedResource(renamed);
	}

	protected abstract Resource getRenamedResource(JailedResourcePath path);

	@Override
	public boolean exists() {
		return getGridFSDBFile(getPath(), false) != null;
	}

	@Override
	public void delete() {
		getFs().remove(getFilename(getPath()));
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
		MongoResourceStore other = (MongoResourceStore) obj;
		boolean rtn = true;
		rtn &= ObjectUtils.nullSafeEquals(getPath().getUnjailedPath(), other.getPath()
				.getUnjailedPath());
		rtn &= ObjectUtils.nullSafeEquals(getFs().getDB(), other.getFs().getDB());
		rtn &= ObjectUtils.nullSafeEquals(getFs().getBucketName(), other.getFs()
				.getBucketName());
		return rtn;
	}

	static class MongoFileStore extends MongoResourceStore implements FileStore {

		public MongoFileStore(GridFS fs, JailedResourcePath path) {
			super(fs, path);
			if (Type.FOLDER.equals(getType(path))) {
				throw new ResourceTypeMismatchException(path.getUnjailedPath(), false);
			}
		}

		@Override
		protected Resource getRenamedResource(JailedResourcePath path) {
			MongoFileStore store = new MongoFileStore(getFs(), path);
			return new MongoFile(store);
		}

		@Override
		public void create() {
			create(Type.FILE, true);
		}

		@Override
		public InputStream getInputStream() {
			return getGridFSDBFile(getPath(), true).getInputStream();
		}

		@Override
		public OutputStream getOutputStream() {
			delete();
			GridFSInputFile file = create(Type.FILE, false);
			return file.getOutputStream();
		}

		@Override
		public long getSize() {
			return getGridFSDBFile(getPath(), true).getLength();
		}

		@Override
		public long getLastModified() {
			return getGridFSDBFile(getPath(), true).getUploadDate().getTime();
		}

		@Override
		public void touch() {
			GridFSDBFile gridFSDBFile = getGridFSDBFile(getPath(), true);
			gridFSDBFile.put("uploadDate", new Date());
			gridFSDBFile.save();
		}
	}

	static class MongoFolderStore extends MongoResourceStore implements FolderStore {

		public MongoFolderStore(GridFS fs, JailedResourcePath path) {
			super(fs, path);
			if (Type.FILE.equals(getType(path))) {
				throw new ResourceTypeMismatchException(path.getUnjailedPath(), true);
			}
		}

		@Override
		protected Resource getRenamedResource(JailedResourcePath path) {
			return getFolder(path);
		}

		@Override
		public void create() {
			create(Type.FOLDER, true);
		}

		@Override
		public Iterable<String> list() {
			BasicDBObject query = new BasicDBObject(PARENT, getPath().getUnjailedPath()
					.toString());
			final DBCursor list = getFs().getFileList(query);
			return new FileListIterable(list);
		}
	}

	private static class FileListIterable implements Iterable<String> {

		private final DBCursor list;

		public FileListIterable(DBCursor list) {
			this.list = list;
		}

		@Override
		public Iterator<String> iterator() {
			return new FileListIterator(this.list.iterator());
		}
	}

	private static class FileListIterator implements Iterator<String> {

		private final Iterator<DBObject> iterator;

		public FileListIterator(Iterator<DBObject> iterator) {
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext() {
			return this.iterator.hasNext();
		}

		@Override
		public String next() {
			DBObject next = this.iterator.next();
			String filename = (String) next.get("filename");
			ResourcePath path = new ResourcePath().get(filename);
			return path.getName();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private static enum Type {
		FILE, FOLDER
	}
}
