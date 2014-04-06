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

import io.spring.demo.doge.filesystem.exception.ResourceException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.WritableResource;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

/**
 * Abstract base class for {@link FileContent}.
 * 
 * @author Phillip Webb
 */
public abstract class AbstractFileContent implements FileContent {

	protected abstract String getDescription();

	protected boolean isWritable() {
		return true;
	}

	@Override
	public Reader asReader() {
		return asReader(DEFAULT_ENCODING);
	}

	@Override
	public Reader asReader(String encoding) {
		Assert.notNull(encoding, "Encoding must not be null");
		try {
			return new InputStreamReader(asInputStream(), encoding);
		}
		catch (UnsupportedEncodingException e) {
			throw new ResourceException(e);
		}
	}

	@Override
	public String asString() throws ResourceException {
		return asString(DEFAULT_ENCODING);
	}

	@Override
	public String asString(String encoding) throws ResourceException {
		Assert.notNull(encoding, "Encoding must not be null");
		try {
			return FileCopyUtils.copyToString(asReader(encoding));
		}
		catch (IOException e) {
			throw new ResourceException(e);
		}
	}

	@Override
	public byte[] asBytes() throws ResourceException {
		try {
			return FileCopyUtils.copyToByteArray(asInputStream());
		}
		catch (IOException e) {
			throw new ResourceException(e);
		}
	}

	@Override
	public WritableResource asResource() {
		return new FileContentResource();
	}

	@Override
	public void copyTo(OutputStream outputStream) throws ResourceException {
		Assert.notNull(outputStream, "OutputStream must not be null");
		try {
			FileCopyUtils.copy(asInputStream(), outputStream);
		}
		catch (IOException e) {
			throw new ResourceException(e);
		}
	}

	@Override
	public void copyTo(Writer writer) throws ResourceException {
		Assert.notNull(writer, "Writer must not be null");
		try {
			FileCopyUtils.copy(asReader(), writer);
		}
		catch (IOException e) {
			throw new ResourceException(e);
		}
	}

	@Override
	public Writer asWriter() throws ResourceException {
		return asWriter(DEFAULT_ENCODING);
	}

	@Override
	public Writer asWriter(String encoding) throws ResourceException {
		Assert.notNull(encoding, "Encoding must not be null");
		try {
			return new OutputStreamWriter(asOutputStream(), encoding);
		}
		catch (UnsupportedEncodingException e) {
			throw new ResourceException(e);
		}
	}

	@Override
	public void write(File file) {
		Assert.notNull(file, "File must not be null");
		write(file.getContent().asInputStream());
	}

	@Override
	public void write(InputStream inputStream) throws ResourceException {
		Assert.notNull(inputStream, "InputStream must not be null");
		try {
			FileCopyUtils.copy(inputStream, asOutputStream());
		}
		catch (IOException e) {
			throw new ResourceException(e);
		}
	}

	@Override
	public void write(Reader reader) throws ResourceException {
		Assert.notNull(reader, "Reader must not be null");
		write(DEFAULT_ENCODING, reader);
	}

	@Override
	public void write(String encoding, Reader reader) throws ResourceException {
		Assert.notNull(encoding, "Encoding must not be null");
		Assert.notNull(reader, "Reader must not be null");
		try {
			FileCopyUtils.copy(reader, asWriter(encoding));
		}
		catch (IOException e) {
			throw new ResourceException(e);
		}
	}

	@Override
	public void write(String string) throws ResourceException {
		Assert.notNull(string, "String must not be null");
		write(DEFAULT_ENCODING, string);
	}

	@Override
	public void write(String encoding, String string) throws ResourceException {
		Assert.notNull(encoding, "Encoding must not be null");
		Assert.notNull(string, "String must not be null");
		try {
			FileCopyUtils.copy(string, asWriter(encoding));
		}
		catch (IOException e) {
			throw new ResourceException(e);
		}
	}

	@Override
	public abstract InputStream asInputStream();

	@Override
	public abstract OutputStream asOutputStream();

	/**
	 * Adapter class to present {@link AbstractFileContent} as a {@link WritableResource}.
	 */
	private class FileContentResource extends AbstractResource implements
			WritableResource {

		@Override
		public String getDescription() {
			return AbstractFileContent.this.getDescription();
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return AbstractFileContent.this.asInputStream();
		}

		@Override
		public boolean isWritable() {
			return AbstractFileContent.this.isWritable();
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return AbstractFileContent.this.asOutputStream();
		}

	}
}
