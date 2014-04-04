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

import io.spring.demo.doge.filesystem.AbstractResources;
import io.spring.demo.doge.filesystem.File;
import io.spring.demo.doge.filesystem.Folder;
import io.spring.demo.doge.filesystem.JailedResourcePath;
import io.spring.demo.doge.filesystem.Resource;
import io.spring.demo.doge.filesystem.ResourcePath;
import io.spring.demo.doge.filesystem.ResourceStringFormat;
import io.spring.demo.doge.filesystem.Resources;
import io.spring.demo.doge.filesystem.ResourcesCollection;
import io.spring.demo.doge.filesystem.exception.ResourceDoesNotExistException;
import io.spring.demo.doge.filesystem.exception.ResourceExistsException;

import java.util.Collections;
import java.util.Iterator;

import org.springframework.util.Assert;

/**
 * A {@link Folder} that is backed by a {@link FolderStore}. Allows developers to use the
 * simpler {@link FolderStore} interface to provide a full {@link Folder} implementation.
 * Subclasses must provide a suitable {@link FolderStore} implementation via the
 * {@link #getStore()} method.
 * 
 * @author Phillip Webb
 * @see FolderStore
 * @see StoredFile
 */
public abstract class StoredFolder extends StoredResource implements Folder {

	@Override
	protected abstract FolderStore getStore();

	@Override
	public Resource getExisting(String name) throws ResourceDoesNotExistException {
		Assert.hasLength(name, "Name must not be empty");
		JailedResourcePath resourcePath = getPath().get(name);
		Resource resource = getStore().getExisting(resourcePath);
		if (resource == null) {
			throw new ResourceDoesNotExistException(this, name);
		}
		return resource;
	}

	@Override
	public boolean hasExisting(String name) {
		Assert.hasLength(name, "Name must not be empty");
		JailedResourcePath resourcePath = getPath().get(name);
		Resource existing = getStore().getExisting(resourcePath);
		return existing != null;
	}

	@Override
	public Folder getFolder(String name) {
		Assert.hasLength(name, "Name must not be empty");
		JailedResourcePath folderPath = getPath().get(name);
		return getStore().getFolder(folderPath);
	}

	@Override
	public File getFile(String name) {
		Assert.hasLength(name, "Name must not be empty");
		JailedResourcePath filePath = getPath().get(name);
		return getStore().getFile(filePath);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Resource> T get(String name, Class<T> resourceType) {
		Assert.hasLength(name, "Name must not be empty");
		Assert.notNull(resourceType, "ResourceType must not be null");
		if (resourceType.equals(Folder.class)) {
			return (T) getFolder(name);
		}
		if (resourceType.equals(File.class)) {
			return (T) getFile(name);
		}
		return (T) getExisting(name);
	}

	@Override
	public Iterator<Resource> iterator() {
		return list().iterator();
	}

	@Override
	public Resources<Resource> list() {
		if (!exists()) {
			return new ResourcesCollection<Resource>(this);
		}
		return new ChildResources(new Iterable<Resource>() {

			@Override
			public Iterator<Resource> iterator() {
				return new ChildResourceIterator(StoredFolder.this);
			}
		});
	}

	@Override
	public Resources<Resource> find() {
		if (!exists()) {
			return new ResourcesCollection<Resource>(this);
		}
		return new ChildResources(new Iterable<Resource>() {

			@Override
			public Iterator<Resource> iterator() {
				return new RecursiveChildResourceIterator(StoredFolder.this);
			}
		});
	}

	@Override
	public Folder copyTo(Folder folder) {
		Assert.notNull(folder, "Folder must not be empty");
		ensureExists();
		Assert.state(getPath().getParent() != null, "Unable to copy a root folder");
		Folder destination = createDestinationFolder(folder);
		for (Resource child : list()) {
			child.copyTo(destination);
		}
		return destination;
	}

	@Override
	public Resources<Resource> copyContentsTo(Folder folder) {
		return list().copyTo(folder);
	}

	@Override
	public Folder moveTo(Folder folder) {
		Assert.notNull(folder, "Folder must not be empty");
		ensureExists();
		Assert.state(getPath().getParent() != null, "Unable to move a root folder");
		Folder destination = createDestinationFolder(folder);
		for (Resource child : list()) {
			child.moveTo(destination);
		}
		return destination;
	}

	@Override
	public Resources<Resource> moveContentsTo(Folder folder) {
		return list().moveTo(folder);
	}

	private Folder createDestinationFolder(Folder folder) {
		Folder destination = folder.getFolder(getName());
		destination.createIfMissing();
		return destination;
	}

	@Override
	public Folder rename(String name) throws ResourceExistsException {
		return (Folder) super.rename(name);
	}

	@Override
	public void delete() {
		if (exists()) {
			for (Resource child : list()) {
				child.delete();
			}
			getStore().delete();
		}
	}

	@Override
	public void createIfMissing() {
		if (!exists()) {
			createParentIfMissing();
			getStore().create();
		}
	}

	@Override
	public Folder jail() {
		JailedResourcePath jailedPath = new JailedResourcePath(getPath()
				.getUnjailedPath(), new ResourcePath());
		return getStore().getFolder(jailedPath);
	}

	@Override
	public String toString(ResourceStringFormat format) {
		return super.toString(format) + "/";
	}

	private static class ChildResourceIterator implements Iterator<Resource> {

		private final StoredFolder folder;

		private final Iterator<String> childNames;

		public ChildResourceIterator(StoredFolder folder) {
			this.folder = folder;
			Iterable<String> list = folder.getStore().list();
			this.childNames = list == null ? Collections.<String> emptyList().iterator()
					: list.iterator();
		}

		@Override
		public boolean hasNext() {
			return this.childNames.hasNext();
		}

		@Override
		public Resource next() {
			String name = this.childNames.next();
			JailedResourcePath path = this.folder.getStore().getPath().get(name);
			Resource resource = this.folder.getStore().getExisting(path);
			if (resource == null) {
				throw new ResourceDoesNotExistException(this.folder, name);
			}
			return resource;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private class RecursiveChildResourceIterator implements Iterator<Resource> {

		private final Iterator<Resource> iterator;

		private Iterator<Resource> current;

		public RecursiveChildResourceIterator(StoredFolder folder) {
			this.iterator = new ChildResourceIterator(folder);
		}

		@Override
		public boolean hasNext() {
			return this.current != null && this.current.hasNext()
					|| this.iterator.hasNext();
		}

		@Override
		public Resource next() {
			if (this.current != null && this.current.hasNext()) {
				return this.current.next();
			}
			this.current = null;
			Resource next = this.iterator.next();
			if (next instanceof StoredFolder) {
				StoredFolder folder = (StoredFolder) next;
				this.current = new RecursiveChildResourceIterator(folder);
			}
			return next;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private class ChildResources extends AbstractResources<Resource> {

		private final Iterable<Resource> iterable;

		public ChildResources(Iterable<Resource> iterable) {
			Assert.notNull(iterable, "Iterable must not be null");
			this.iterable = iterable;
		}

		@Override
		public Folder getSource() {
			return StoredFolder.this;
		}

		@Override
		public Iterator<Resource> iterator() {
			return this.iterable.iterator();
		}

	}

}
