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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.springframework.util.Assert;

/**
 * Implementation of {@link Resources} backed by a {@link Collection}.
 * 
 * @author Phillip Webb
 * @param <T> the resource type
 */
public class ResourcesCollection<T extends Resource> extends AbstractResources<T> {

	private final Folder source;

	private final Collection<T> resources;

	public ResourcesCollection(Folder source, Collection<T> resources) {
		Assert.notNull(source, "Source must not be null");
		Assert.notNull(resources, "Resources must not be null");
		this.source = source;
		this.resources = resources;
	}

	@SafeVarargs
	public ResourcesCollection(Folder source, T... resources) {
		Assert.notNull(source, "Source must not be null");
		Assert.notNull(resources, "Resources must not be null");
		this.source = source;
		this.resources = Arrays.asList(resources);
	}

	@Override
	public Folder getSource() {
		return this.source;
	}

	@Override
	public Iterator<T> iterator() {
		return this.resources.iterator();
	}
}
