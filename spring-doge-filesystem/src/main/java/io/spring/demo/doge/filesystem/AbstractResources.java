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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.util.Assert;

/**
 * Abstract base for {@link Resources} implementations.
 * 
 * @author Phillip Webb
 * @param <T> The resource type
 */
public abstract class AbstractResources<T extends Resource> implements Resources<T> {

	private final ResourceFilterContext resourceFilterContext = new ResourceFilterContext() {

		@Override
		public Folder getSource() {
			return AbstractResources.this.getSource();
		}
	};

	protected final ResourceFilterContext getResourceFilterContext() {
		return this.resourceFilterContext;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Resources<File> files() {
		return (Resources) include(new ResourceFilter() {

			@Override
			public boolean match(ResourceFilterContext context, Resource resource) {
				return resource instanceof File;
			}
		});
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Resources<Folder> folders() {
		return (Resources) include(new ResourceFilter() {

			@Override
			public boolean match(ResourceFilterContext context, Resource resource) {
				return resource instanceof Folder;
			}
		});
	}

	@Override
	public Resources<T> include(ResourceFilter... filters) {
		return FilteredResources.include(this, filters);
	}

	@Override
	public Resources<T> exclude(ResourceFilter... filters) {
		return FilteredResources.exclude(this, filters);
	}

	@Override
	public void delete() {
		List<T> delete = asList();
		for (T resource : delete) {
			resource.delete();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Resources<T> moveTo(Folder folder) {
		Assert.notNull(folder, "Folder must not be null");
		List<T> movedResources = new ArrayList<T>();
		for (T resource : this) {
			movedResources.add((T) resource
					.moveTo(calculateDestination(resource, folder)));
		}
		return new ResourcesCollection<T>(folder, movedResources);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Resources<T> copyTo(Folder folder) {
		Assert.notNull(folder, "Folder must not be null");
		List<T> copiedResources = new ArrayList<T>();
		for (T resource : this) {
			copiedResources.add((T) resource
					.copyTo(calculateDestination(resource, folder)));
		}
		return new ResourcesCollection<T>(folder, copiedResources);
	}

	private Folder calculateDestination(T resource, Folder folder) {
		Folder parent = resource.getParent();
		if (parent == null) {
			return folder;
		}
		String name = parent.toStringRelativeTo(getSource());
		return name.length() == 0 ? folder : folder.getFolder(name);
	}

	@Override
	public <OPERATION extends ResourceOperation<T>> OPERATION performOperation(
			OPERATION operation) {
		for (T resource : this) {
			operation.perform(resource);
		}
		return operation;
	}

	@Override
	public List<T> asList() {
		List<T> all = new ArrayList<T>();
		for (T resource : this) {
			all.add(resource);
		}
		return Collections.unmodifiableList(all);
	}
}
