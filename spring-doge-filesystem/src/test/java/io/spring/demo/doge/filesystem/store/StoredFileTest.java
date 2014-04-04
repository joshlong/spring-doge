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

import io.spring.demo.doge.filesystem.AbstractFileContent;
import io.spring.demo.doge.filesystem.File;
import io.spring.demo.doge.filesystem.FileContent;
import io.spring.demo.doge.filesystem.Folder;
import io.spring.demo.doge.filesystem.exception.ResourceDoesNotExistException;

import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link StoredFile}.
 * 
 * @author Phillip Webb
 */
public class StoredFileTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private StoredFile file;

	@Before
	public void setup() {
		this.file = new MockStoredFolder().getFile("file.txt");
	}

	@Test
	public void shouldGetSizeFromStore() throws Exception {
		Long size = 100L;
		given(this.file.getStore().getSize()).willReturn(size);
		assertThat(this.file.getSize(), is(equalTo(size)));
		verify(this.file.getStore()).getSize();
	}

	@Test
	public void shouldGetLastModifiedFromStore() throws Exception {
		Long lastModified = 100L;
		given(this.file.getStore().getLastModified()).willReturn(lastModified);
		assertThat(this.file.getLastModified(), is(equalTo(lastModified)));
		verify(this.file.getStore()).getLastModified();
	}

	@Test
	public void shouldThrowOnTouchNonExistingFile() throws Exception {
		given(this.file.exists()).willReturn(false);
		this.thrown.expect(ResourceDoesNotExistException.class);
		this.thrown.expectMessage("The resource '/file.txt' does not exist");
		this.file.touch();
	}

	@Test
	public void shouldTouchExistingFile() throws Exception {
		given(this.file.exists()).willReturn(true);
		this.file.touch();
		verify(this.file.getStore()).touch();
	}

	@Test
	public void shouldGetContentInputStreamFromFileSystem() throws Exception {
		InputStream inputStream = mock(InputStream.class);
		given(this.file.getStore().getInputStream()).willReturn(inputStream);
		assertThat(this.file.getContent().asInputStream(), is(inputStream));
	}

	@Test
	public void shouldGetContentOutputStreamFromFileSystem() throws Exception {
		OutputStream outputStream = mock(OutputStream.class);
		given(this.file.getStore().getOutputStream()).willReturn(outputStream);
		assertThat(this.file.getContent().asOutputStream(), is(outputStream));
	}

	@Test
	public void shouldGetContentAsAbstractContent() throws Exception {
		assertThat(this.file.getContent(), instanceOf(AbstractFileContent.class));
	}

	@Test
	public void shouldDelete() throws Exception {
		given(this.file.getStore().exists()).willReturn(true);
		this.file.delete();
		verify(this.file.getStore()).delete();
	}

	@Test
	public void shouldNotDeleteWhenDoesNotExist() throws Exception {
		given(this.file.getStore().exists()).willReturn(false);
		this.file.delete();
		verify(this.file.getStore(), never()).delete();
	}

	@Test
	public void shouldCreateIfMissingNewFile() throws Exception {
		given(this.file.getStore().exists()).willReturn(false);
		this.file.createIfMissing();
		verify(this.file.getStore()).create();
	}

	@Test
	public void shouldNotCreateIfMissingExistingFile() throws Exception {
		given(this.file.getStore().exists()).willReturn(true);
		this.file.createIfMissing();
		verify(this.file.getStore(), never()).create();
	}

	@Test
	public void shouldNotMoveIfDoesNotExist() throws Exception {
		Folder destination = mock(Folder.class);
		given(this.file.getStore().exists()).willReturn(false);
		this.thrown.expect(ResourceDoesNotExistException.class);
		this.file.moveTo(destination);
	}

	@Test
	public void shouldMove() throws Exception {
		Folder destination = mock(Folder.class);
		File destinationFile = mock(File.class);
		FileContent destinationContent = mock(FileContent.class);
		InputStream inputStream = mock(InputStream.class);
		given(destination.getFile("file.txt")).willReturn(destinationFile);
		given(destinationFile.getContent()).willReturn(destinationContent);
		given(this.file.getStore().exists()).willReturn(true);
		given(this.file.getStore().getInputStream()).willReturn(inputStream);
		this.file.moveTo(destination);
		verify(destinationContent).write(inputStream);
		verify(this.file.getStore()).delete();
	}

	@Test
	public void shouldNotCopyIfDoesNotExist() throws Exception {
		Folder destination = mock(Folder.class);
		given(this.file.getStore().exists()).willReturn(false);
		this.thrown.expect(ResourceDoesNotExistException.class);
		this.file.copyTo(destination);
	}

	@Test
	public void shouldCopy() throws Exception {
		Folder destination = mock(Folder.class);
		File destinationFile = mock(File.class);
		FileContent destinationContent = mock(FileContent.class);
		given(destination.getFile("file.txt")).willReturn(destinationFile);
		given(destinationFile.getContent()).willReturn(destinationContent);
		given(this.file.getStore().exists()).willReturn(true);
		this.file.copyTo(destination);
		verify(destinationContent).write(this.file);
		verify(this.file.getStore(), never()).delete();
	}

	@Test
	public void shouldRename() throws Exception {
		given(this.file.getStore().exists()).willReturn(true);
		this.file.rename("file.bak");
		verify(this.file.getStore()).rename("file.bak");
	}

	@Test
	public void shouldNotRenameIfDoesNotExist() throws Exception {
		given(this.file.getStore().exists()).willReturn(false);
		this.thrown.expect(ResourceDoesNotExistException.class);
		this.thrown.expectMessage("The resource '/file.txt' does not exist");
		this.file.rename("file.bak");
	}

	@Test
	public void shouldNotRenameToEmpty() throws Exception {
		given(this.file.getStore().exists()).willReturn(true);
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("Name must not be empty");
		this.file.rename("");
	}

	@Test
	public void shouldNotRenameWithPathElements() throws Exception {
		given(this.file.getStore().exists()).willReturn(true);
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("Name must not contain path elements");
		this.file.rename("file/bak");
	}

	@Test
	public void shouldHaveToString() throws Exception {
		assertThat(this.file.toString(), is("/file.txt"));
	}

}
