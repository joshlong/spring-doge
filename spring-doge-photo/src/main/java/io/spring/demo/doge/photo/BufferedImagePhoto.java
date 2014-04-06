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

package io.spring.demo.doge.photo;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.springframework.util.Assert;

/**
 * {@link Photo} backed by a {@link BufferedImage}.
 * 
 * @author Phillip Webb
 */
public class BufferedImagePhoto implements Photo {

	private final BufferedImage image;

	public BufferedImagePhoto(BufferedImage image) {
		Assert.notNull(image, "Image must not be null");
		this.image = image;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ImageIO.write(this.image, "jpeg", outputStream);
		return new ByteArrayInputStream(outputStream.toByteArray());
	}

}
