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

package io.spring.demo.doge.filesystem.virtual;

import io.spring.demo.doge.filesystem.File;
import io.spring.demo.doge.filesystem.Resource;
import io.spring.demo.doge.filesystem.local.LocalFolder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link VirtualFolder}.
 * 
 * @author Phillip Webb
 */
public class VirtualFolderTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private final VirtualFolder folder = new VirtualFolder();

	@Test
	public void shouldCreateNew() throws Exception {
		assertThat(this.folder.toString(), is("/"));
		assertThat(this.folder.exists(), is(false));
		assertThat(this.folder.list().asList().size(), is(0));
	}

	@Test
	public void shouldCreateRoot() throws Exception {
		this.folder.createIfMissing();
		assertThat(this.folder.exists(), is(true));
	}

	@Test
	public void shouldDeleteRoot() throws Exception {
		this.folder.createIfMissing();
		this.folder.delete();
		assertThat(this.folder.exists(), is(false));
	}

	@Test
	public void shouldCreateFile() throws Exception {
		this.folder.getFile("a/b/c.txt").getContent().write("c");
		Set<String> names = new HashSet<String>();
		for (Resource resource : this.folder.find()) {
			names.add(resource.toString());
		}
		assertThat(
				names,
				is((Set<String>) new HashSet<String>(Arrays.asList("/a/", "/a/b/",
						"/a/b/c.txt"))));
	}

	@Test
	public void shouldRenameKeepingChildren() throws Exception {
		this.folder.getFile("a/b/c.txt").getContent().write("c");
		this.folder.getFolder("a/b").rename("x");
		Set<String> names = new HashSet<String>();
		for (Resource resource : this.folder.find()) {
			names.add(resource.toString());
		}
		assertThat(
				names,
				is((Set<String>) new HashSet<String>(Arrays.asList("/a/", "/a/x/",
						"/a/x/c.txt"))));
	}

	@Test
	public void shouldNotStoreFileContents() throws Exception {
		LocalFolder temp = new LocalFolder(this.temporaryFolder.getRoot());
		File sourceFile = temp.getFile("a/b/c.txt");
		sourceFile.getContent().write("c1");
		temp.copyContentsTo(this.folder);
		File destFile = this.folder.getFile("a/b/c.txt");
		assertThat(destFile.getContent().asString(), is("c1"));
		sourceFile.getContent().write("c2");
		assertThat(destFile.getContent().asString(), is("c2"));
	}

	@Test
	public void shouldGetSizeWhenMissing() throws Exception {
		assertThat(this.folder.getFile("a.txt").getSize(), is(0L));
	}

	@Test
	public void shouldGetSizeWhenWritten() throws Exception {
		this.folder.getFile("a.txt").getContent().write("a");
		assertThat(this.folder.getFile("a.txt").getSize(), is(1L));
	}

	@Test
	public void shouldGetSizeWhenCopy() throws Exception {
		LocalFolder temp = new LocalFolder(this.temporaryFolder.getRoot());
		temp.getFile("a.txt").getContent().write("a");
		temp.copyContentsTo(this.folder);
		assertThat(this.folder.getFile("a.txt").getSize(), is(1L));
	}

	@Test
	public void shouldGetLastModifiedWhenMissing() throws Exception {
		assertThat(this.folder.getFile("a.txt").getLastModified(), is(-1L));
	}

	@Test
	public void shouldGetLastModifiedWhenWritten() throws Exception {
		this.folder.getFile("a.txt").getContent().write("a");
		assertThat(this.folder.getFile("a.txt").getLastModified(), is(not(-1L)));
	}

	@Test
	public void shouldGetLastModifiedWhenCopy() throws Exception {
		LocalFolder temp = new LocalFolder(this.temporaryFolder.getRoot());
		File tempFile = temp.getFile("a.txt");
		tempFile.getContent().write("a");
		temp.copyContentsTo(this.folder);
		assertThat(this.folder.getFile("a.txt").getLastModified(),
				is(tempFile.getLastModified()));
	}

	@Test
	public void shouldTouchFile() throws Exception {
		File file = this.folder.getFile("a.txt");
		file.createIfMissing();
		long t1 = file.getLastModified();
		Thread.sleep(100);
		file.touch();
		long t2 = file.getLastModified();
		assertThat(t2, is(greaterThan(t1)));
	}
}
