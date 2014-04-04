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

import io.spring.demo.doge.filesystem.exception.ResourceDoesNotExistException;

import java.util.List;

/**
 * An {@link Iterable} collections of {@link Resource}s that also support various
 * {@link ResourceOperation operations}.
 * 
 * @author Phillip Webb
 * @param <T> The resource type
 */
public interface Resources<T extends Resource> extends Iterable<T> {

	/**
	 * Returns the source of the resources. Items in this collection will be
	 * {@link #moveTo(Folder) moved} or {@link #copyTo(Folder) copied} relative to the
	 * source. For example, if this object contains the files '/a/b/c.txt' and
	 * '/d/e/f.txt' and the source is '/d' copy to '/x/ will result in '/x/b/c.txt' and
	 * 'x/e/f.txt'.
	 * @return the source for the resources
	 */
	Folder getSource();

	/**
	 * Return a new {@link Resources} instance containing only {@link File}s from this
	 * collection.
	 * @return the files
	 */
	Resources<File> files();

	/**
	 * Return a new {@link Resources} instance containing only {@link Folder}s from this
	 * collection.
	 * @return the folders
	 */
	Resources<Folder> folders();

	/**
	 * Return a new {@link Resources} instance containing items that match any of the
	 * specified filters.
	 * @param filters the include filters
	 * @return filtered {@link Resources}
	 */
	Resources<T> include(ResourceFilter... filters);

	/**
	 * Return a new {@link Resources} instance removing items that match any of the
	 * specified filters.
	 * @param filters the exclude filters
	 * @return filtered {@link Resources}
	 */
	Resources<T> exclude(ResourceFilter... filters);

	/**
	 * Delete the current resource (and any children). If this resource does not exist
	 * then no operation is performed.
	 */
	void delete();

	/**
	 * Move this resource to the specified folder. Any duplicate {@link File}s will be
	 * replaced (existing {@link Folder} resources will be merged). If the resource does
	 * not exist no operation is performed.
	 * @param folder the folder to move the resource to
	 * @return a resource collection containing the new destination resources
	 * @throws ResourceDoesNotExistException if this resource no longer exists
	 */
	Resources<T> moveTo(Folder folder);

	/**
	 * Recursively copy this resource to the specified folder. Any duplicate {@link File}s
	 * will be replaced (existing {@link Folder} resources will be merged). If the
	 * resource does not exist no operation is performed.
	 * @param folder the folder to copy the resource to
	 * @return a resource collection containing the new destination resources
	 * @throws ResourceDoesNotExistException if this resource no longer exists
	 */
	Resources<T> copyTo(Folder folder);

	/**
	 * Perform the given operation with each {@link Resource} in this collection.
	 * @param operation the operation to perform
	 * @return the operation that was performed
	 */
	<O extends ResourceOperation<T>> O performOperation(O operation);

	/**
	 * Fetch all {@link Resource}s from this collection and return the result as a
	 * {@link List}. This will trigger {@link #iterator() iteration} over each element.
	 * @return a {@link List} of all {@link Resource}s in this collection.
	 */
	List<T> asList();
}
