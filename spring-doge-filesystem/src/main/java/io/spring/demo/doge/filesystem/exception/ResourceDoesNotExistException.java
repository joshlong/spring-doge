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

package io.spring.demo.doge.filesystem.exception;

import io.spring.demo.doge.filesystem.Folder;
import io.spring.demo.doge.filesystem.Resource;

/**
 * {@link ResourceException} thrown when a requested resource does not exist.
 * 
 * @author Phillip Webb
 */
public class ResourceDoesNotExistException extends ResourceException {

	private static final long serialVersionUID = 1L;

	public ResourceDoesNotExistException(Folder folder, String missingResourceName) {
		super("The resource '" + missingResourceName + "' does not exist in the folder '"
				+ folder + "'");
	}

	public ResourceDoesNotExistException(Resource resource) {
		super("The resource '" + resource.toString() + "' does not exist");
	}
}
