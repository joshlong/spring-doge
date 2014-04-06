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
import io.spring.demo.doge.filesystem.FilterOn;
import io.spring.demo.doge.filesystem.Folder;
import io.spring.demo.doge.filesystem.Resource;
import io.spring.demo.doge.filesystem.Resources;
import io.spring.demo.doge.filesystem.exception.ResourceException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link LocalFolder}.
 * 
 * @author Phillip Webb
 */
public class LocalFolderTest {

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	@Rule
	public TemporaryFolder dest = new TemporaryFolder();

	private LocalFolder root;

	@Before
	public void setup() {
		this.root = new LocalFolder(this.temp.getRoot());
		this.root.getFile("/a/b/c.txt").getContent().write("c");
		this.root.getFile("/d/e/f.txt").getContent().write("d");
		this.root.getFile("/g.txt").getContent().write("g");
	}

	@Test
	public void shouldFind() throws Exception {
		List<Resource> all = this.root.find().asList();
		Set<String> actual = getNames(all);
		Set<String> expected = new HashSet<String>();
		expected.add("/a/");
		expected.add("/a/b/");
		expected.add("/a/b/c.txt");
		expected.add("/d/");
		expected.add("/d/e/");
		expected.add("/d/e/f.txt");
		expected.add("/g.txt");
		assertThat(actual, is(expected));
	}

	@Test
	public void shouldFindSingle() throws Exception {
		List<Resource> all = this.root.getFolder("a/b").find().asList();
		Set<String> actual = getNames(all);
		Set<String> expected = new HashSet<String>();
		expected.add("/a/b/c.txt");
		assertThat(actual, is(expected));
	}

	@Test
	public void shouldFindFiles() throws Exception {
		List<File> all = this.root.find().files().asList();
		Set<String> actual = getNames(all);
		Set<String> expected = new HashSet<String>();
		expected.add("/a/b/c.txt");
		expected.add("/d/e/f.txt");
		expected.add("/g.txt");
		assertThat(actual, is(expected));
	}

	@Test
	public void shouldFindFilesTwice() throws Exception {
		// WM-4280
		Resources<File> files = this.root.find().files();
		List<File> all1 = files.asList();
		List<File> all2 = files.asList();
		assertThat(all1.size(), is(all2.size()));
	}

	@Test
	public void shouldCopy() throws Exception {
		Folder destination = new LocalFolder(this.dest.getRoot());
		this.root.find().files().exclude(FilterOn.names().starting("f"))
				.copyTo(destination);
		Set<String> actual = getNames(destination.find());
		Set<String> expected = new HashSet<String>();
		expected.add("/a/");
		expected.add("/a/b/");
		expected.add("/a/b/c.txt");
		expected.add("/g.txt");
		assertThat(actual, is(expected));
	}

	private Set<String> getNames(Iterable<? extends Resource> resources) {
		Set<String> allNames = new HashSet<String>();
		for (Resource resource : resources) {
			allNames.add(resource.toString());
		}
		return allNames;
	}

	@Test
	public void shouldUseUnderlyingResourceForEqualsAndHashCode() throws Exception {
		Folder folder1 = new LocalFolder(this.temp.getRoot()).getFolder("folder");
		folder1.createIfMissing();
		Folder folder2 = new LocalFolder(new java.io.File(this.temp.getRoot(), "folder"));
		Folder folder3 = new LocalFolder(this.temp.getRoot()).getFolder("xfolder");
		File file1 = folder1.getFile("file");
		File file2 = folder2.getFile("file");
		File file3 = folder3.getFile("file");
		file1.createIfMissing();
		file2.createIfMissing();
		file3.createIfMissing();

		assertThat(folder1, is(equalTo(folder1)));
		assertThat(folder1, is(equalTo(folder2)));
		assertThat(folder1, is(not(equalTo(folder3))));
		assertThat(file1, is(equalTo(file1)));
		assertThat(file1, is(equalTo(file2)));
		assertThat(file1, is(not(equalTo(file3))));

		assertThat(folder1.hashCode(), is(equalTo(folder1.hashCode())));
		assertThat(folder1.hashCode(), is(equalTo(folder2.hashCode())));
		assertThat(folder1.hashCode(), is(not(equalTo(folder3.hashCode()))));
		assertThat(file1.hashCode(), is(equalTo(file1.hashCode())));
		assertThat(file1.hashCode(), is(equalTo(file2.hashCode())));
		assertThat(file1.hashCode(), is(not(equalTo(file3.hashCode()))));
	}

	@Test
	public void shouldNotCreateMissingFileWhenGettingAsString() throws Exception {
		// WM-4290
		Folder folder = this.root.getFolder("test");
		File file = folder.getFile("test.txt");
		assertThat(folder.exists(), is(false));
		try {
			file.getContent().asString();
		}
		catch (ResourceException e) {
		}
		assertThat(folder.exists(), is(false));
	}
}
