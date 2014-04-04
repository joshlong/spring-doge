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

import io.spring.demo.doge.filesystem.Folder;
import io.spring.demo.doge.filesystem.JailedResourcePath;
import io.spring.demo.doge.filesystem.Resource;
import io.spring.demo.doge.filesystem.ResourceOperation;
import io.spring.demo.doge.filesystem.ResourceStringFormat;
import io.spring.demo.doge.filesystem.exception.ResourceDoesNotExistException;
import io.spring.demo.doge.filesystem.exception.ResourceExistsException;

import org.springframework.core.GenericTypeResolver;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Base for {@link StoredFile} and {@link StoredFolder}.
 * 
 * @author Phillip Webb
 * @see StoredFile
 * @see StoredFolder
 */
public abstract class StoredResource implements Resource {

	protected abstract ResourceStore getStore();

	protected final JailedResourcePath getPath() {
		return getStore().getPath();
	}

	protected final void ensureExists() {
		if (!exists()) {
			throw new ResourceDoesNotExistException(this);
		}
	}

	protected final void createParentIfMissing() {
		if (getParent() != null) {
			getParent(true).createIfMissing();
		}
	}

	@Override
	public Folder getParent() {
		return getParent(false);
	}

	private Folder getParent(boolean unjailed) {
		JailedResourcePath path = getPath();
		JailedResourcePath parentPath = (unjailed ? path.unjail() : path).getParent();
		if (parentPath == null) {
			return null;
		}
		return getStore().getFolder(parentPath);
	}

	@Override
	public boolean exists() {
		return getStore().exists();
	}

	@Override
	public Resource rename(String name) throws ResourceExistsException {
		Assert.hasLength(name, "Name must not be empty");
		Assert.isTrue(!name.contains("/"), "Name must not contain path elements");
		ensureExists();
		Assert.state(getPath().getPath().getParent() != null,
				"Root folders cannot be renamed");
		return getStore().rename(name);
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
	public String getName() {
		return getPath().getPath().getName();
	}

	@Override
	public String toString() {
		return toString(ResourceStringFormat.FULL);
	}

	@Override
	public String toString(ResourceStringFormat format) {
		return getPath().toString(format);
	}

	@Override
	public String toStringRelativeTo(Folder source) {
		Assert.notNull(source, "Source must not be null");
		return getPath().getPath().toStringRelativeTo(source.toString());
	}

	@Override
	public int hashCode() {
		return getStore().hashCode();
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
		StoredResource other = (StoredResource) obj;
		return ObjectUtils.nullSafeEquals(getStore(), other.getStore());
	}
}
