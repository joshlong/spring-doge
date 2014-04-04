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

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * A file or folder path.
 * 
 * @author Phillip Webb
 */
public final class ResourcePath {

	private final ResourcePath parent;

	private final String name;

	/**
	 * Create a new root path instance.
	 */
	public ResourcePath() {
		this(null, "");
	}

	/**
	 * Private constructor used to create a nested path.
	 * @param parent the parent
	 * @param name the name of the path element
	 * @see #get(String)
	 */
	private ResourcePath(ResourcePath parent, String name) {
		Assert.notNull(name, "Name must not be null");
		this.parent = parent;
		this.name = name;
	}

	/**
	 * Returns the name of the path element.
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the full path value.
	 * @return the path value
	 */
	@Override
	public String toString() {
		if (this.parent != null) {
			return this.parent + "/" + this.name;
		}
		return this.name;
	}

	public String toStringRelativeTo(String source) {
		Assert.notNull(source, "Source must not be null");
		return toStringRelativeTo(new ResourcePath().get(source));
	}

	public String toStringRelativeTo(ResourcePath source) {
		Assert.notNull(source, "Source must not be null");
		if (source.equals(this)) {
			return "";
		}
		String sourcePath = source.toString() + "/";
		Assert.isTrue(toString().startsWith(sourcePath), "Source '" + source
				+ "' must be a parent of '" + this + "'");
		return toString().substring(sourcePath.length());
	}

	/**
	 * Get a new path relative to this one.
	 * @param path the path to obtain.
	 * @return a new path
	 */
	public ResourcePath get(String path) {
		Assert.hasLength(path, "Path must not be empty");
		ResourcePath rtn = this;
		if (path.startsWith("/")) {
			rtn = new ResourcePath();
			path = path.substring(1);
		}
		while (path.indexOf("/") != -1) {
			rtn = rtn.newPath(path.substring(0, path.indexOf("/")));
			path = path.substring(path.indexOf("/") + 1);
		}
		return rtn.newPath(path);
	}

	/**
	 * Internal factory method used to create a new path item.
	 * @param name the name of the path
	 * @return the new Path element
	 */
	private ResourcePath newPath(String name) {
		if ("".equals(name)) {
			return this;
		}
		if ("..".equals(name)) {
			Assert.state(this.parent != null);
			return this.parent;
		}
		return new ResourcePath(this, name);
	}

	/**
	 * Returns the parent of the path or <tt>null</tt> if this is a root path.
	 * @return the parent or <tt>null</tt>
	 */
	public ResourcePath getParent() {
		return this.parent;
	}

	/**
	 * Returns <tt>true</tt> if this path is a root path.
	 * @return if this is a root path
	 */
	public boolean isRootPath() {
		if (this.parent != null) {
			return false;
		}
		return "".equals(this.name);
	}

	/**
	 * Append the given path to this path
	 * @param path the path to append
	 * @return a new path
	 */
	public ResourcePath append(ResourcePath path) {
		Assert.notNull(path, "Path must not be null");
		if (path.isRootPath()) {
			return this;
		}
		if (path.getParent() == null) {
			return new ResourcePath(this, path.getName());
		}
		return new ResourcePath(append(path.getParent()), path.getName());
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj instanceof ResourcePath) {
			ResourcePath other = (ResourcePath) obj;
			return ObjectUtils.nullSafeEquals(getParent(), other.getParent())
					&& this.name.equals(other.name);
		}
		return false;
	}
}
