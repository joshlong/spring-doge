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

package io.spring.demo.doge.filesystem.mongo;

import io.spring.demo.doge.filesystem.File;
import io.spring.demo.doge.filesystem.Folder;
import io.spring.demo.doge.filesystem.JailedResourcePath;
import io.spring.demo.doge.filesystem.Resource;
import io.spring.demo.doge.filesystem.ResourcePath;
import io.spring.demo.doge.filesystem.exception.ResourceTypeMismatchException;
import io.spring.demo.doge.filesystem.mongo.MongoResourceStore.MongoFileStore;
import io.spring.demo.doge.filesystem.mongo.MongoResourceStore.MongoFolderStore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.util.FileCopyUtils;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link MongoResourceStore} and subclasses.
 * 
 * @author Phillip Webb
 */
public class MongoResourceStoreTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Mock
	private GridFS fs;

	private MongoFolderStore store;

	private Map<String, GridFSDBFile> files;

	@Captor
	private ArgumentCaptor<DBObject> queryCaptor;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.store = new MongoFolderStore(this.fs, new JailedResourcePath());
		createFileStructure();
	}

	private void createFileStructure() {
		List<GridFSDBFile> files = new ArrayList<GridFSDBFile>();
		files.add(newGridFSDBFile("/a", "", "FOLDER"));
		files.add(newGridFSDBFile("/a/b", "/a", "FOLDER"));
		files.add(newGridFSDBFile("/a/b/c.txt", "/a/b", "FILE"));
		files.add(newGridFSDBFile("/d", "", "FOLDER"));
		files.add(newGridFSDBFile("/d/e", "/d", "FOLDER"));
		files.add(newGridFSDBFile("/d/e/f.txt", "/d/e", "FILE"));
		files.add(newGridFSDBFile("/g.txt", "", "FILE"));
		this.files = new LinkedHashMap<String, GridFSDBFile>();
		for (GridFSDBFile file : files) {
			this.files.put(file.getFilename(), file);
			given(this.fs.findOne(file.getFilename())).willReturn(file);
		}
	}

	private GridFSDBFile newGridFSDBFile(String filename, String parent, String type) {
		GridFSDBFile file = spy(new GridFSDBFile());
		willDoNothing().given(file).save();
		file.put("filename", filename);
		file.put("parent", parent);
		file.put("resourceType", type);
		return file;
	}

	@Test
	public void shouldNeedGridFS() throws Exception {
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("FS must not be null");
		new MongoFolderStore(null, new JailedResourcePath());
	}

	@Test
	public void shouldNeedPath() throws Exception {
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("Path must not be null");
		new MongoFolderStore(this.fs, null);
	}

	@Test
	public void shouldGetPath() throws Exception {
		JailedResourcePath path = new JailedResourcePath(new ResourcePath(),
				new ResourcePath().get("a/b/c"));
		MongoFolderStore store = new MongoFolderStore(this.fs, path);
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
		GridFSDBFile gridFSDBFile = this.files.get("/g.txt");
		MongoFileStore store = new MongoFileStore(this.fs,
				new JailedResourcePath().get("g.txt"));
		store.rename("g.bak");
		assertThat(gridFSDBFile.getFilename(), is("/g.bak"));
		verify(gridFSDBFile).save();
	}

	@Test
	public void shouldExist() throws Exception {
		MongoFileStore store = new MongoFileStore(this.fs,
				new JailedResourcePath().get("g.txt"));
		assertThat(store.exists(), is(true));
	}

	@Test
	public void shouldNotExist() throws Exception {
		MongoFileStore store = new MongoFileStore(this.fs,
				new JailedResourcePath().get("doesnotexist"));
		assertThat(store.exists(), is(false));
	}

	@Test
	public void shouldDelete() throws Exception {
		MongoFileStore store = new MongoFileStore(this.fs,
				new JailedResourcePath().get("g.txt"));
		store.delete();
		verify(this.fs).remove("/g.txt");
	}

	@Test
	public void shouldCreateFile() throws Exception {
		GridFSInputFile gridFSInputFile = mock(GridFSInputFile.class);
		OutputStream outputStream = mock(OutputStream.class);
		given(gridFSInputFile.getOutputStream()).willReturn(outputStream);
		given(this.fs.createFile("/new")).willReturn(gridFSInputFile);
		MongoFileStore store = new MongoFileStore(this.fs,
				new JailedResourcePath().get("new"));
		store.create();
		verify(this.fs).createFile("/new");
		verify(gridFSInputFile).put("parent", "");
		verify(gridFSInputFile).put("resourceType", "FILE");
		verify(outputStream).close();
	}

	@Test
	public void shouldGetInputStream() throws Exception {
		GridFSDBFile gridFSDBFile = this.files.get("/g.txt");
		given(gridFSDBFile.getInputStream()).willReturn(
				new ByteArrayInputStream("g".getBytes()));
		MongoFileStore store = new MongoFileStore(this.fs,
				new JailedResourcePath().get("g.txt"));
		String actual = FileCopyUtils.copyToString(new InputStreamReader(store
				.getInputStream()));
		assertThat(actual, is("g"));
	}

	@Test
	public void shouldGetOutputStream() throws Exception {
		GridFSInputFile gridFSInputFile = mock(GridFSInputFile.class);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		given(gridFSInputFile.getOutputStream()).willReturn(outputStream);
		given(this.fs.createFile("/g.txt")).willReturn(gridFSInputFile);
		MongoFileStore store = new MongoFileStore(this.fs,
				new JailedResourcePath().get("g.txt"));
		OutputStream stream = store.getOutputStream();
		try {
			stream.write("x".getBytes());
		}
		finally {
			stream.close();
		}
		assertThat(outputStream.toByteArray(), is("x".getBytes()));
	}

	@Test
	public void shouldGetSize() throws Exception {
		GridFSDBFile gridFSDBFile = this.files.get("/g.txt");
		given(gridFSDBFile.getLength()).willReturn(1L);
		MongoFileStore store = new MongoFileStore(this.fs,
				new JailedResourcePath().get("g.txt"));
		long actual = store.getSize();
		assertThat(actual, is(1L));
	}

	@Test
	public void shouldGetLastModified() throws Exception {
		Date date = new Date();
		GridFSDBFile gridFSDBFile = this.files.get("/g.txt");
		given(gridFSDBFile.getUploadDate()).willReturn(date);
		MongoFileStore store = new MongoFileStore(this.fs,
				new JailedResourcePath().get("g.txt"));
		long actual = store.getLastModified();
		assertThat(actual, is(equalTo(date.getTime())));
	}

	@Test
	public void shouldTouchFile() throws Exception {
		GridFSDBFile gridFSDBFile = this.files.get("/g.txt");
		MongoFileStore store = new MongoFileStore(this.fs,
				new JailedResourcePath().get("g.txt"));
		store.touch();
		verify(gridFSDBFile).put(eq("uploadDate"), any(Date.class));
		verify(gridFSDBFile).save();
	}

	@Test
	public void shouldCreateFolder() throws Exception {
		GridFSInputFile gridFSInputFile = mock(GridFSInputFile.class);
		OutputStream outputStream = mock(OutputStream.class);
		given(gridFSInputFile.getOutputStream()).willReturn(outputStream);
		given(this.fs.createFile("/new")).willReturn(gridFSInputFile);
		MongoFolderStore store = new MongoFolderStore(this.fs,
				new JailedResourcePath().get("new"));
		store.create();
		verify(this.fs).createFile("/new");
		verify(gridFSInputFile).put("parent", "");
		verify(gridFSInputFile).put("resourceType", "FOLDER");
		verify(outputStream).close();
	}

	@Test
	public void shouldList() throws Exception {
		DBCursor cursor = mock(DBCursor.class);
		given(this.fs.getFileList(this.queryCaptor.capture())).willReturn(cursor);
		List<DBObject> foundFiles = new ArrayList<DBObject>();
		foundFiles.add(this.files.get("/a"));
		foundFiles.add(this.files.get("/d"));
		foundFiles.add(this.files.get("/g.txt"));
		given(cursor.iterator()).willReturn(foundFiles.iterator());
		Set<String> expected = new HashSet<String>(Arrays.asList("a", "d", "g.txt"));
		Set<String> actual = new HashSet<String>(Arrays.asList("a", "d", "g.txt"));
		for (String name : this.store.list()) {
			actual.add(name);
		}
		assertThat((String) this.queryCaptor.getValue().get("parent"), is(""));
		assertThat(actual, is(equalTo(expected)));
	}
}
