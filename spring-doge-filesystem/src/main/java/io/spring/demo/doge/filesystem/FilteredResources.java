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

import java.util.Iterator;

import org.springframework.util.Assert;

/**
 * Implementation of of {@link Resources} that dynamically filters items based on a
 * {@link ResourceFilter}.
 * 
 * @author Phillip Webb
 * @param <T> The resource type
 * @see #include(Resources, ResourceFilter...)
 */
public class FilteredResources<T extends Resource> extends AbstractResources<T> {

	private static enum Type {
		INCLUDE, EXCLUDE
	}

	private final Resources<T> resources;

	private final Type type;

	private final ResourceFilter[] filters;

	private FilteredResources(Resources<T> resources, Type type,
			ResourceFilter... filters) {
		Assert.notNull(resources, "Resources must not be null");
		Assert.notNull(filters, "Filters must not be null");
		this.resources = resources;
		this.filters = filters;
		this.type = type;
	}

	@Override
	public Folder getSource() {
		return this.resources.getSource();
	}

	@Override
	public Iterator<T> iterator() {
		FilteredIterator<T> filteredIterator = new FilteredIterator<T>(
				this.resources.iterator()) {

			@Override
			protected boolean isElementFiltered(T element) {
				return FilteredResources.this.type == Type.INCLUDE ? !isMatch(element)
						: isMatch(element);
			}

			private boolean isMatch(T element) {
				for (ResourceFilter filter : FilteredResources.this.filters) {
					if (filter.match(getResourceFilterContext(), element)) {
						return true;
					}
				}
				return false;
			}

		};
		return filteredIterator;
	}

	public static <T extends Resource> Resources<T> include(Resources<T> resources,
			ResourceFilter... filters) {
		return new FilteredResources<T>(resources, Type.INCLUDE, filters);
	}

	public static <T extends Resource> Resources<T> exclude(Resources<T> resources,
			ResourceFilter... filters) {
		return new FilteredResources<T>(resources, Type.EXCLUDE, filters);
	}

}
