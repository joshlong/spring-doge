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

import io.spring.demo.doge.filesystem.exception.ReadOnlyResourceException;
import io.spring.demo.doge.filesystem.exception.ResourceDoesNotExistException;
import io.spring.demo.doge.filesystem.exception.ResourceException;
import io.spring.demo.doge.filesystem.exception.ResourceExistsException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.core.GenericTypeResolver;
import org.springframework.util.Assert;

/**
 * Abstract base class for read-only {@link File} implementations that are not contained
 * in any {@link #getParent() parent} {@link Folder}.
 * 
 * @author Phillip Webb
 */
public abstract class AbstractReadOnlyFile implements File {

	private final FileContent content = new AbstractFileContent() {

		@Override
		public OutputStream asOutputStream() {
			throw newReadOnlyResourceException();
		}

		@Override
		public InputStream asInputStream() {
			InputStream inputStream = getInputStream();
			if (inputStream == null) {
				throw new ResourceDoesNotExistException(AbstractReadOnlyFile.this);
			}
			return inputStream;
		}
	};

	@Override
	public Folder getParent() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete() {
		throw newReadOnlyResourceException();
	}

	@Override
	public boolean exists() {
		InputStream inputStream = getInputStream();
		try {
			return inputStream != null;
		}
		finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			}
			catch (IOException e) {
				throw new ResourceException(e);
			}
		}
	}

	@Override
	public void createIfMissing() {
		throw newReadOnlyResourceException();
	}

	@Override
	public String toString() {
		return toString(ResourceStringFormat.FULL);
	}

	@Override
	public File moveTo(Folder folder) {
		throw newReadOnlyResourceException();
	}

	@Override
	public File copyTo(Folder folder) {
		File file = folder.getFile(getName());
		file.getContent().write(this);
		return file;
	}

	@Override
	public File rename(String name) throws ResourceExistsException {
		throw newReadOnlyResourceException();
	}

	@Override
	public void touch() throws ResourceDoesNotExistException {
		throw newReadOnlyResourceException();
	}

	@Override
	public long getSize() {
		return 0;
	}

	@Override
	public long getLastModified() {
		return 0;
	}

	@Override
	public FileContent getContent() {
		return this.content;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R extends Resource, O extends ResourceOperation<R>> O performOperation(
			O operation) {
		Class<?> typeArgument = GenericTypeResolver.resolveTypeArgument(
				operation.getClass(), ResourceOperation.class);
		Assert.isInstanceOf(typeArgument, this);
		operation.perform((R) this);
		return operation;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		return toString().equals(obj.toString());
	}

	/**
	 * Return the input stream for the contents of the File or <tt>null</tt> if the file
	 * does not exist.
	 * @return the {@link InputStream} or <tt>null</tt>
	 */
	protected abstract InputStream getInputStream();

	/**
	 * Return the {@link ReadOnlyResourceException} that should be thrown on error.
	 * @return the exception
	 */
	protected ReadOnlyResourceException newReadOnlyResourceException() {
		return new ReadOnlyResourceException("The resource " + toString()
				+ " is read-only");
	}
}
