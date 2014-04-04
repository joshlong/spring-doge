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

package io.spring.demo.doge.filesystem.store;

import io.spring.demo.doge.filesystem.File;
import io.spring.demo.doge.filesystem.Folder;
import io.spring.demo.doge.filesystem.Resource;
import io.spring.demo.doge.filesystem.ResourceFilter;
import io.spring.demo.doge.filesystem.ResourceFilterContext;
import io.spring.demo.doge.filesystem.ResourceStringFormat;
import io.spring.demo.doge.filesystem.Resources;
import io.spring.demo.doge.filesystem.exception.ResourceDoesNotExistException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link StoredFolder}.
 * 
 * @author Phillip Webb
 */
public class StoredFolderTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private MockStoredFolder folder;

	@Before
	public void setup() {
		this.folder = new MockStoredFolder();
	}

	@Test
	public void shouldCreateWithNoParent() throws Exception {
		assertThat(this.folder.getParent(), is(nullValue()));
	}

	@Test
	public void shouldDelete() throws Exception {
		given(this.folder.getStore().exists()).willReturn(true);
		this.folder.delete();
		verify(this.folder.getStore()).delete();
	}

	@Test
	public void shouldNotDeleteWhenDoesNotExist() throws Exception {
		given(this.folder.getStore().exists()).willReturn(false);
		this.folder.delete();
		verify(this.folder.getStore(), never()).delete();
	}

	@Test
	public void shouldDeleteChildren() throws Exception {
		MockStoredFolder child = this.folder.getFolder("a", true);
		given(this.folder.getStore().exists()).willReturn(true);
		given(this.folder.getStore().list()).willReturn(Collections.singleton("a"));
		this.folder.delete();
		verify(child.getStore()).delete();
		verify(this.folder.getStore()).delete();
	}

	@Test
	public void shouldGetParent() throws Exception {
		Folder parent = this.folder.getFolder("a/b").getParent();
		assertThat(parent.getName(), is("a"));
		assertThat(parent.toString(), is("/a/"));
	}

	@Test
	public void shouldCreateWithNoName() throws Exception {
		assertThat(this.folder.getName(), is(""));
	}

	@Test
	public void shouldCreateWithPathName() throws Exception {
		assertThat(this.folder.toString(), is("/"));
	}

	@Test
	public void shouldDelegateToFileSystemForExists() throws Exception {
		given(this.folder.getStore().exists()).willReturn(true);
		assertThat(this.folder.exists(), is(true));
		verify(this.folder.getStore()).exists();
	}

	@Test
	public void shouldGetChildFolder() throws Exception {
		Folder child = this.folder.getFolder("a");
		assertThat(child.getName(), is("a"));
		assertThat(child.toString(), is("/a/"));
	}

	@Test
	public void shouldGetNestedChildFolder() throws Exception {
		Folder child = this.folder.getFolder("a/b");
		assertThat(child.getName(), is("b"));
		assertThat(child.toString(), is("/a/b/"));
	}

	@Test
	public void shouldGetRelativeChildFolder() throws Exception {
		Folder child = this.folder.getFolder("a/b/../c");
		assertThat(child.getName(), is("c"));
		assertThat(child.toString(), is("/a/c/"));
	}

	@Test
	public void shouldGetNestedChildFile() throws Exception {
		File child = this.folder.getFile("a/b");
		assertThat(child.getName(), is("b"));
		assertThat(child.toString(), is("/a/b"));
	}

	@Test
	public void shouldGetRelativeChildFile() throws Exception {
		File child = this.folder.getFile("a/b/../c");
		assertThat(child.getName(), is("c"));
		assertThat(child.toString(), is("/a/c"));
	}

	@Test
	public void shouldGetExistingFile() throws Exception {
		this.folder.getFile("a");
		Resource child = this.folder.getExisting("a");
		assertThat(child, instanceOf(File.class));
		assertThat(child.getName(), is("a"));
		assertThat(child.toString(), is("/a"));
	}

	@Test
	public void shouldGetExistingFolder() throws Exception {
		this.folder.getFolder("a");
		Resource child = this.folder.getExisting("a");
		assertThat(child, instanceOf(Folder.class));
		assertThat(child.getName(), is("a"));
		assertThat(child.toString(), is("/a/"));
	}

	@Test
	public void shouldNotGetExistingIfDoesNotExist() throws Exception {
		this.thrown.expect(ResourceDoesNotExistException.class);
		this.thrown.expectMessage("The resource 'a' does not exist in the folder '/'");
		this.folder.getExisting("a");
	}

	@Test
	public void shouldNotHaveExistingFileIfDoesNotExist() throws Exception {
		boolean actual = this.folder.hasExisting("a");
		assertThat(actual, is(false));
	}

	@Test
	public void shouldHaveExistingFile() throws Exception {
		this.folder.getFolder("a");
		boolean actual = this.folder.hasExisting("a");
		assertThat(actual, is(true));
	}

	@Test
	public void shouldCreateMissingDirectory() throws Exception {
		MockStoredFolder child = this.folder.getFolder("a");
		child.createIfMissing();
		verify(child.getStore()).create();
	}

	@Test
	public void shouldNotCreateExistingDirectory() throws Exception {
		MockStoredFolder child = this.folder.getFolder("a", true);
		child.createIfMissing();
		verify(child.getStore(), never()).create();
	}

	@Test
	public void shouldCreateParent() throws Exception {
		MockStoredFolder child = this.folder.getFolder("a");
		MockStoredFolder grandChild = child.getFolder("b");
		grandChild.createIfMissing();
		InOrder inOrder = inOrder(grandChild.getStore(), child.getStore());
		inOrder.verify(child.getStore()).create();
		inOrder.verify(grandChild.getStore()).create();
	}

	@Test
	public void shouldListResources() throws Exception {
		this.folder.getFolder("a", true);
		this.folder.getFile("b", true);
		given(this.folder.getStore().exists()).willReturn(true);
		given(this.folder.getStore().list()).willReturn(Arrays.asList("a", "b"));
		Resources<Resource> resources = this.folder.list();
		Iterator<Resource> iterator = resources.iterator();
		Resource resourceA = iterator.next();
		Resource resourceB = iterator.next();
		assertThat(iterator.hasNext(), is(false));
		assertThat(resourceA, instanceOf(Folder.class));
		assertThat(resourceB, instanceOf(File.class));
		assertThat(resourceA.toString(), is("/a/"));
		assertThat(resourceB.toString(), is("/b"));
	}

	@Test
	public void shouldListFilteredResources() throws Exception {
		this.folder.getFolder("a", true);
		this.folder.getFile("b", true);
		given(this.folder.getStore().exists()).willReturn(true);
		given(this.folder.getStore().list()).willReturn(Arrays.asList("a", "b"));
		Resources<Resource> resources = this.folder.list().include(new ResourceFilter() {

			@Override
			public boolean match(ResourceFilterContext context, Resource resource) {
				return resource instanceof File;
			}
		});
		Iterator<Resource> iterator = resources.iterator();
		Resource file = iterator.next();
		assertThat(iterator.hasNext(), is(false));
		assertThat(file.toString(), is("/b"));
	}

	@Test
	public void shouldListFilteredResourcesWithAnonymousClass() throws Exception {
		this.folder.getFolder("a", true);
		this.folder.getFile("b", true);
		given(this.folder.getStore().exists()).willReturn(true);
		given(this.folder.getStore().list()).willReturn(Arrays.asList("a", "b"));
		Resources<File> resources = this.folder.list().files()
				.include(new ResourceFilter() {

					@Override
					public boolean match(ResourceFilterContext context, Resource resource) {
						return true;
					}
				});
		Iterator<File> iterator = resources.iterator();
		Resource file = iterator.next();
		assertThat(iterator.hasNext(), is(false));
		assertThat(file.toString(), is("/b"));

	}

	@Test
	public void shouldNeedListResourcesFilters() throws Exception {
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("Filters must not be null");
		this.folder.list().include((ResourceFilter[]) null);
	}

	@Test
	public void shouldIterateUsingList() throws Exception {
		this.folder.getFolder("a", true);
		this.folder.getFile("b", true);
		given(this.folder.getStore().exists()).willReturn(true);
		given(this.folder.getStore().list()).willReturn(Arrays.asList("a", "b"));
		Iterator<Resource> iterator = this.folder.iterator();
		Resource resourceA = iterator.next();
		Resource resourceB = iterator.next();
		assertThat(iterator.hasNext(), is(false));
		assertThat(resourceA, instanceOf(Folder.class));
		assertThat(resourceB, instanceOf(File.class));
		assertThat(resourceA.toString(), is("/a/"));
		assertThat(resourceB.toString(), is("/b"));
	}

	@Test
	public void shouldMoveWithoutChildren() throws Exception {
		Folder destination = mock(Folder.class);
		Folder destinationChild = mock(Folder.class);
		MockStoredFolder child = this.folder.getFolder("a", true);
		given(this.folder.getStore().exists()).willReturn(true);
		given(destination.getFolder("a")).willReturn(destinationChild);
		child.moveTo(destination);
		verify(destination).getFolder("a");
		verify(destinationChild).createIfMissing();
	}

	@Test
	public void shouldNotMoveIfDoesNotExist() throws Exception {
		Folder destination = mock(Folder.class);
		MockStoredFolder child = this.folder.getFolder("a", false);
		given(this.folder.getStore().exists()).willReturn(true);
		this.thrown.expect(ResourceDoesNotExistException.class);
		child.moveTo(destination);
	}

	@Test
	public void shouldNotMoveRoot() throws Exception {
		Folder destination = mock(Folder.class);
		given(this.folder.getStore().exists()).willReturn(true);
		this.thrown.expect(IllegalStateException.class);
		this.thrown.expectMessage("Unable to move a root folder");
		this.folder.moveTo(destination);
	}

	@Test
	public void shouldMoveChildren() throws Exception {
		Folder destination = mock(Folder.class);
		Folder destinationChild = mock(Folder.class);
		Folder destinationGrandchild = mock(Folder.class);
		MockStoredFolder child = this.folder.getFolder("a", true);
		child.getFolder("b", true);
		given(child.getStore().list()).willReturn(Collections.singleton("b"));
		given(destination.getFolder("a")).willReturn(destinationChild);
		given(destinationChild.getFolder("b")).willReturn(destinationGrandchild);
		child.moveTo(destination);
		verify(destinationGrandchild).createIfMissing();
	}

	@Test
	public void shouldCopyWithoutChildren() throws Exception {
		Folder destination = mock(Folder.class);
		Folder destinationChild = mock(Folder.class);
		MockStoredFolder child = this.folder.getFolder("a", true);
		given(this.folder.getStore().exists()).willReturn(true);
		given(destination.getFolder("a")).willReturn(destinationChild);
		child.copyTo(destination);
		verify(destination).getFolder("a");
		verify(destinationChild).createIfMissing();
	}

	@Test
	public void shouldNotCopyIfDoesNotExist() throws Exception {
		Folder destination = mock(Folder.class);
		MockStoredFolder child = this.folder.getFolder("a");
		given(this.folder.getStore().exists()).willReturn(false);
		this.thrown.expect(ResourceDoesNotExistException.class);
		child.copyTo(destination);
	}

	@Test
	public void shouldNotCopyRoot() throws Exception {
		given(this.folder.getStore().exists()).willReturn(true);
		Folder destination = mock(Folder.class);
		this.thrown.expect(IllegalStateException.class);
		this.thrown.expectMessage("Unable to copy a root folder");
		this.folder.copyTo(destination);
	}

	@Test
	public void shouldCopyChildren() throws Exception {
		MockStoredFolder child = this.folder.getFolder("a", true);
		child.getFolder("b", true);
		given(this.folder.getStore().exists()).willReturn(true);
		given(child.getStore().list()).willReturn(Collections.singleton("b"));
		Folder destination = mock(Folder.class);
		Folder destinationChild = mock(Folder.class);
		Folder destinationGrandchild = mock(Folder.class);
		given(destination.getFolder("a")).willReturn(destinationChild);
		given(destinationChild.getFolder("b")).willReturn(destinationGrandchild);
		child.copyTo(destination);
		verify(destinationGrandchild).createIfMissing();
	}

	@Test
	public void shouldRename() throws Exception {
		MockStoredFolder subFolder = this.folder.getFolder("subfolder");
		given(subFolder.getStore().exists()).willReturn(true);
		subFolder.rename("folder.bak");
		verify(subFolder.getStore()).rename("folder.bak");
	}

	@Test
	public void shouldNotRenameRootFolder() throws Exception {
		given(this.folder.getStore().exists()).willReturn(true);
		this.thrown.expect(IllegalStateException.class);
		this.thrown.expectMessage("Root folders cannot be renamed");
		this.folder.rename("folder.bak");
	}

	@Test
	public void shouldNotRenameIfDoesNotExist() throws Exception {
		MockStoredFolder subFolder = this.folder.getFolder("subfolder");
		given(subFolder.getStore().exists()).willReturn(false);
		this.thrown.expect(ResourceDoesNotExistException.class);
		this.thrown.expectMessage("The resource '/subfolder/' does not exist");
		subFolder.rename("file.bak");
	}

	@Test
	public void shouldNotRenameToEmpty() throws Exception {
		MockStoredFolder subFolder = this.folder.getFolder("subfolder");
		given(subFolder.getStore().exists()).willReturn(true);
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("Name must not be empty");
		subFolder.rename("");
	}

	@Test
	public void shouldNotRenameWithPathElements() throws Exception {
		given(this.folder.getStore().exists()).willReturn(true);
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("Name must not contain path elements");
		this.folder.rename("file/bak");
	}

	@Test
	public void shouldAppendPathToToString() throws Exception {
		MockStoredFolder child = this.folder.getFolder("a/b/c");
		assertThat(child.toString(), is("/a/b/c/"));
	}

	@Test
	public void shouldFormatToString() throws Exception {
		Folder child = this.folder.getFolder("a", true).jail().getFolder("b/c");
		assertThat(child.toString(), is("/b/c/"));
		assertThat(child.toString(ResourceStringFormat.FULL), is("/b/c/"));
		assertThat(child.toString(ResourceStringFormat.UNJAILED), is("/a/b/c/"));
	}

	@Test
	public void shouldNeedNameForGet() throws Exception {
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("Name must not be empty");
		this.folder.get("", File.class);
	}

	@Test
	public void shouldNeedTypeForGet() throws Exception {
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("ResourceType must not be null");
		this.folder.get("name", null);
	}

	@Test
	public void shouldGetFile() throws Exception {
		this.folder = spy(this.folder);
		this.folder.get("name", File.class);
		verify(this.folder).getFile("name");
	}

	@Test
	public void shouldGetFolder() throws Exception {
		this.folder = spy(this.folder);
		this.folder.get("name", Folder.class);
		verify(this.folder).getFolder("name");
	}

	@Test
	public void shouldGetResource() throws Exception {
		this.folder.getFile("name", true);
		this.folder = spy(this.folder);
		this.folder.get("name", Resource.class);
		verify(this.folder).getExisting("name");
	}

	@Test
	public void shouldJail() throws Exception {
		Folder jailed = this.folder.getFolder("a").jail();
		Folder sub = jailed.getFolder("/b");
		assertThat(sub.toString(), is("/b/"));
		assertThat(sub.toString(ResourceStringFormat.UNJAILED), is("/a/b/"));
	}

	@Test
	public void shouldDoubleJail() throws Exception {
		Folder jailed = this.folder.getFolder("a").jail().jail();
		Folder sub = jailed.getFolder("/b");
		assertThat(sub.toString(), is("/b/"));
		assertThat(sub.toString(ResourceStringFormat.UNJAILED), is("/a/b/"));
	}
}
