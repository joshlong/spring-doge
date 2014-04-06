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

package io.spring.demo.doge.filesystem.local;

import io.spring.demo.doge.filesystem.File;
import io.spring.demo.doge.filesystem.Folder;
import io.spring.demo.doge.filesystem.JailedResourcePath;
import io.spring.demo.doge.filesystem.Resource;
import io.spring.demo.doge.filesystem.ResourcePath;
import io.spring.demo.doge.filesystem.exception.ResourceTypeMismatchException;
import io.spring.demo.doge.filesystem.local.LocalResourceStore.LocalFileStore;
import io.spring.demo.doge.filesystem.local.LocalResourceStore.LocalFolderStore;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.springframework.util.FileCopyUtils;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link LocalResourceStore} and subclasses.
 * 
 * @author Phillip Webb
 */
public class LocalResourceStoreTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	private LocalFolderStore store;

	@Before
	public void setup() {
		Folder root = new LocalFolder(this.temp.getRoot());
		root.getFile("/a/b/c.txt").getContent().write("c");
		root.getFile("/d/e/f.txt").getContent().write("d");
		root.getFile("/g.txt").getContent().write("g");
		this.store = new LocalFolderStore(this.temp.getRoot(), new JailedResourcePath());
	}

	@Test
	public void shouldNeedRoot() throws Exception {
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("Root must not be null");
		new LocalFolderStore(null, new JailedResourcePath());
	}

	@Test
	public void shouldNeedPath() throws Exception {
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("Path must not be null");
		new LocalFolderStore(this.temp.getRoot(), null);
	}

	@Test
	public void shouldNeedRootThatExists() throws Exception {
		java.io.File folderDoesNotExist = new java.io.File(this.temp.getRoot(),
				"doesnotexist");
		this.thrown.expect(IllegalStateException.class);
		this.thrown.expectMessage(endsWith("does not exist"));
		new LocalFolderStore(folderDoesNotExist, new JailedResourcePath());
	}

	@Test
	public void shouldNeedRootThatIsFolder() throws Exception {
		java.io.File notAFolder = new java.io.File(this.temp.getRoot(), "g.txt");
		this.thrown.expect(IllegalStateException.class);
		this.thrown.expectMessage(endsWith("is not a folder"));
		new LocalFolderStore(notAFolder, new JailedResourcePath());
	}

	@Test
	public void shouldGetPath() throws Exception {
		JailedResourcePath path = new JailedResourcePath(new ResourcePath(),
				new ResourcePath().get("a/b/c"));
		LocalFolderStore store = new LocalFolderStore(this.temp.getRoot(), path);
		assertThat(store.getPath(), is(equalTo(path)));
	}

	@Test
	public void shouldReturnNullForMissingGetExisting() throws Exception {
		Resource actual = this.store.getExisting(new JailedResourcePath()
				.get("doesnotexist"));
		assertThat(actual, is(nullValue()));
	}

	@Test
	public void shouldGetExistingFile() throws Exception {
		Resource actual = this.store.getExisting(new JailedResourcePath().get("g.txt"));
		assertThat(actual, instanceOf(File.class));
		assertThat(actual.toString(), is("/g.txt"));
	}

	@Test
	public void shouldGetExistingFolder() throws Exception {
		Resource actual = this.store.getExisting(new JailedResourcePath().get("a"));
		assertThat(actual, instanceOf(Folder.class));
		assertThat(actual.toString(), is("/a/"));
	}

	@Test
	public void shouldGetFolder() throws Exception {
		Folder folder = this.store.getFolder(new JailedResourcePath().get("x"));
		assertThat(folder.toString(), is("/x/"));
	}

	@Test
	public void shouldNotGetExistingFileAsFolder() throws Exception {
		this.thrown.expect(ResourceTypeMismatchException.class);
		this.thrown
				.expectMessage("Unable to access resource '/g.txt' as folder due to existing resource");
		this.store.getFolder(new JailedResourcePath().get("g.txt"));
	}

	@Test
	public void shouldGetFile() throws Exception {
		File file = this.store.getFile(new JailedResourcePath().get("x"));
		assertThat(file.toString(), is("/x"));
	}

	@Test
	public void shouldNotGetExistingFolderAsFile() throws Exception {
		this.thrown.expect(ResourceTypeMismatchException.class);
		this.thrown
				.expectMessage("Unable to access resource '/a' as file due to existing resource");
		this.store.getFile(new JailedResourcePath().get("a"));
	}

	@Test
	public void shouldRename() throws Exception {
		LocalFileStore store = new LocalFileStore(this.temp.getRoot(),
				new JailedResourcePath().get("g.txt"));
		store.rename("g.bak");
		assertThat(new java.io.File(this.temp.getRoot(), "g.txt").exists(), is(false));
		assertThat(new java.io.File(this.temp.getRoot(), "g.bak").exists(), is(true));
	}

	@Test
	public void shouldExist() throws Exception {
		LocalFileStore store = new LocalFileStore(this.temp.getRoot(),
				new JailedResourcePath().get("g.txt"));
		assertThat(store.exists(), is(true));
	}

	@Test
	public void shouldNotExist() throws Exception {
		LocalFileStore store = new LocalFileStore(this.temp.getRoot(),
				new JailedResourcePath().get("doesnotexist"));
		assertThat(store.exists(), is(false));
	}

	@Test
	public void shouldDelete() throws Exception {
		LocalFileStore store = new LocalFileStore(this.temp.getRoot(),
				new JailedResourcePath().get("g.txt"));
		store.delete();
		assertThat(new java.io.File(this.temp.getRoot(), "g.txt").exists(), is(false));
	}

	@Test
	public void shouldCreateFile() throws Exception {
		LocalFileStore store = new LocalFileStore(this.temp.getRoot(),
				new JailedResourcePath().get("new"));
		store.create();
		assertThat(new java.io.File(this.temp.getRoot(), "new").exists(), is(true));
	}

	@Test
	public void shouldGetInputStream() throws Exception {
		LocalFileStore store = new LocalFileStore(this.temp.getRoot(),
				new JailedResourcePath().get("g.txt"));
		String actual = FileCopyUtils.copyToString(new InputStreamReader(store
				.getInputStream()));
		assertThat(actual, is("g"));
	}

	@Test
	public void shouldGetOutputStream() throws Exception {
		LocalFileStore store = new LocalFileStore(this.temp.getRoot(),
				new JailedResourcePath().get("g.txt"));
		OutputStream stream = store.getOutputStream();
		try {
			stream.write("x".getBytes());
		}
		finally {
			stream.close();
		}
		String actual = FileCopyUtils.copyToString(new InputStreamReader(
				new FileInputStream(new java.io.File(this.temp.getRoot(), "g.txt"))));
		assertThat(actual, is("x"));

	}

	@Test
	public void shouldGetSize() throws Exception {
		LocalFileStore store = new LocalFileStore(this.temp.getRoot(),
				new JailedResourcePath().get("g.txt"));
		long actual = store.getSize();
		assertThat(actual, is(1L));
	}

	@Test
	public void shouldGetLastModified() throws Exception {
		LocalFileStore store = new LocalFileStore(this.temp.getRoot(),
				new JailedResourcePath().get("g.txt"));
		long actual = store.getLastModified();
		assertThat(actual,
				is(new java.io.File(this.temp.getRoot(), "g.txt").lastModified()));
	}

	@Test
	public void shouldTouchFile() throws Exception {
		LocalFileStore store = new LocalFileStore(this.temp.getRoot(),
				new JailedResourcePath().get("g.txt"));
		java.io.File f = new java.io.File(this.temp.getRoot(), "g.txt");
		f.setLastModified(f.lastModified() - TimeUnit.MINUTES.toMillis(10));
		long beforeTouch = f.lastModified();
		store.touch();
		assertThat(f.lastModified(), is(greaterThan(beforeTouch)));
	}

	@Test
	public void shouldCreateFolder() throws Exception {
		LocalFolderStore store = new LocalFolderStore(this.temp.getRoot(),
				new JailedResourcePath().get("new"));
		store.create();
		assertThat(new java.io.File(this.temp.getRoot(), "new").exists(), is(true));
	}

	@Test
	public void shouldList() throws Exception {
		Set<String> expected = new HashSet<String>(Arrays.asList("a", "d", "g.txt"));
		Set<String> actual = new HashSet<String>(Arrays.asList("a", "d", "g.txt"));
		for (String name : this.store.list()) {
			actual.add(name);
		}
		assertThat(actual, is(equalTo(expected)));
	}
}
