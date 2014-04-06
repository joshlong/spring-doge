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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.WritableResource;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link AbstractFileContent}.
 * 
 * @author Phillip Webb
 */
public class FileContentTest {

	private final String CONTENT = "Test";

	private ByteArrayOutputStream outputStream;

	private ByteArrayInputStream inputStream;

	private AbstractFileContent content;

	@Before
	public void setup() {
		this.outputStream = spy(new ByteArrayOutputStream());
		this.inputStream = spy(new ByteArrayInputStream(this.CONTENT.getBytes()));
		this.content = new AbstractFileContent() {

			@Override
			protected String getDescription() {
				return "test";
			}

			@Override
			public OutputStream asOutputStream() {
				return FileContentTest.this.outputStream;
			}

			@Override
			public InputStream asInputStream() {
				return FileContentTest.this.inputStream;
			}

		};
	}

	@Test
	public void shouldGetAsReader() throws Exception {
		char[] cbuf = new char[4];
		Reader reader = this.content.asReader();
		reader.read(cbuf);
		assertThat(cbuf, is(equalTo(this.CONTENT.toCharArray())));
		reader.close();
		verify(this.inputStream).close();
	}

	@Test
	public void shouldGetAsString() throws Exception {
		String string = this.content.asString();
		assertThat(string, is(equalTo(this.CONTENT)));
		verify(this.inputStream).close();
	}

	@Test
	public void shouldGetAsBytes() throws Exception {
		byte[] bytes = this.content.asBytes();
		assertThat(bytes, is(equalTo(this.CONTENT.getBytes())));
		verify(this.inputStream).close();
	}

	@Test
	public void shouldGetAsReadable() throws Exception {
		WritableResource resource = this.content.asResource();
		assertThat(resource.getDescription(), equalTo("test"));
		assertThat(resource.isReadable(), equalTo(true));
		assertThat(resource.getInputStream(),
				sameInstance((InputStream) this.inputStream));
		assertThat(resource.getOutputStream(),
				sameInstance((OutputStream) this.outputStream));
	}

	@Test
	public void shouldCopyToOutputStream() throws Exception {
		ByteArrayOutputStream copyStream = spy(new ByteArrayOutputStream());
		this.content.copyTo(copyStream);
		assertThat(copyStream.toByteArray(), is(equalTo(this.CONTENT.getBytes())));
		verify(this.inputStream).close();
		verify(copyStream).close();
	}

	@Test
	public void shouldCopyToWriter() throws Exception {
		StringWriter writer = spy(new StringWriter());
		this.content.copyTo(writer);
		assertThat(writer.toString(), is(equalTo(this.CONTENT)));
		verify(this.inputStream).close();
		verify(writer).close();
	}

	@Test
	public void shouldGetAsWriter() throws Exception {
		Writer writer = this.content.asWriter();
		writer.write(this.CONTENT.toCharArray());
		writer.close();
		assertThat(this.outputStream.toByteArray(), is(this.CONTENT.getBytes()));
		verify(this.outputStream).close();
	}

	@Test
	public void shouldWriteOutputStream() throws Exception {
		OutputStream outputStream = this.content.asOutputStream();
		outputStream.write(this.CONTENT.getBytes());
		outputStream.close();
		assertThat(this.outputStream.toByteArray(), is(equalTo(this.CONTENT.getBytes())));
		verify(this.outputStream).close();
	}

	@Test
	public void shouldWriteReader() throws Exception {
		StringReader reader = spy(new StringReader(this.CONTENT));
		this.content.write(reader);
		assertThat(this.outputStream.toByteArray(), is(equalTo(this.CONTENT.getBytes())));
		verify(reader).close();
		verify(this.outputStream).close();
	}

	@Test
	public void shouldWriteString() throws Exception {
		this.content.write(this.CONTENT);
		assertThat(this.outputStream.toByteArray(), is(equalTo(this.CONTENT.getBytes())));
		verify(this.outputStream).close();
	}
}
