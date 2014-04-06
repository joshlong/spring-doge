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

import io.spring.demo.doge.filesystem.exception.ResourceTypeMismatchException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.util.ObjectUtils;

/**
 * Factory class that can be used to construct a {@link URL} for a given {@link Resource}.
 * The {@link URL}s returned from this class can be used to access file content using the
 * {@link URL#openStream()} method, however, the URL cannot be serialized. URLs can be
 * used with a {@link URLClassLoader}.
 * 
 * @author Phillip Webb
 */
public abstract class ResourceURL {

	public static final String PROTOCOL = "rfs";

	/**
	 * Get a URL for the given {@link Resource}.
	 * @param resource the resource
	 * @return a URL for the resource
	 * @throws MalformedURLException
	 */
	public static URL get(Resource resource) throws MalformedURLException {
		return get(resource, false);
	}

	/**
	 * Get a URL for the given {@link Resource}.
	 * @param resource the resource
	 * @param nonLocking if the URL should protect against file locking
	 * @return a URL for the resource
	 * @throws MalformedURLException
	 */
	public static URL get(Resource resource, boolean nonLocking)
			throws MalformedURLException {
		ResourceURLStreamHandler handler = new ResourceURLStreamHandler(resource,
				nonLocking);
		return new URL(PROTOCOL, resource.getClass().getName() + "@"
				+ ObjectUtils.getIdentityHexString(resource), 0, resource.toString(),
				handler);
	}

	/**
	 * Get a {@link List} of {@link URL}s for the given {@link Resource}s.
	 * @param resources
	 * @return a list of URLs for the resource
	 * @throws MalformedURLException
	 */
	public static List<URL> getForResources(Iterable<? extends Resource> resources)
			throws MalformedURLException {
		return getForResources(resources, false);
	}

	/**
	 * Get a {@link List} of {@link URL}s for the given {@link Resource}s.
	 * @param resources
	 * @param nonLocking if the URL should protect against file locking
	 * @return a list of URLs for the resource
	 * @throws MalformedURLException
	 */
	public static List<URL> getForResources(Iterable<? extends Resource> resources,
			boolean nonLocking) throws MalformedURLException {
		List<URL> urls = new ArrayList<URL>();
		for (Resource resource : resources) {
			urls.add(get(resource, nonLocking));
		}
		return Collections.unmodifiableList(urls);
	}

	/**
	 * Internal {@link URLStreamHandler} used with {@link Resource} URLs.
	 */
	private static class ResourceURLStreamHandler extends URLStreamHandler {

		private final Folder root;

		private final boolean nonLocking;

		public ResourceURLStreamHandler(Resource resource, boolean nonLocking) {
			this.root = findRoot(resource);
			this.nonLocking = nonLocking;
		}

		@Override
		protected void parseURL(URL u, String spec, int start, int limit) {
			super.parseURL(u, spec, start, limit);
		}

		private Folder findRoot(Resource resource) {
			Resource root = resource;
			while (root.getParent() != null) {
				root = root.getParent();
			}
			return (Folder) root;
		}

		@Override
		protected URLConnection openConnection(URL url) throws IOException {
			String path = url.getPath();
			if ("/".equals(path)) {
				throw new IOException("Unable to open root folder");
			}
			try {
				File file = this.root.getFile(path);
				if (!file.exists()) {
					throw new IOException("File '" + file + "' does not exist");
				}
				return new FileURLConnection(url, file, this.nonLocking);
			}
			catch (ResourceTypeMismatchException e) {
				throw new IOException("Unable to open URL connection to folder '" + path
						+ "'", e);
			}
		}
	}

	/**
	 * Internal {@link URLConnection} used with {@link Resource} URLs.
	 */
	private static class FileURLConnection extends URLConnection {

		private final File file;

		private final boolean nonLocking;

		public FileURLConnection(URL url, File file, boolean nonLocking) {
			super(url);
			this.file = file;
			this.nonLocking = nonLocking;
		}

		@Override
		public void connect() throws IOException {
		}

		@Override
		public InputStream getInputStream() throws IOException {
			if (this.nonLocking) {
				return new ByteArrayInputStream(this.file.getContent().asBytes());
			}
			return this.file.getContent().asInputStream();
		}
	}
}
