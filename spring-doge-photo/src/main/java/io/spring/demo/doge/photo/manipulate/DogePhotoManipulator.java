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

package io.spring.demo.doge.photo.manipulate;

import io.spring.demo.doge.photo.BufferedImagePhoto;
import io.spring.demo.doge.photo.Photo;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.springframework.core.io.ClassPathResource;

/**
 * A {@link PhotoManipulator} to add Doge images.
 *
 * @author Josh Long
 * @author Phillip Webb
 */
public class DogePhotoManipulator implements PhotoManipulator {

	private static final int IMAGE_WIDTH = 300;

	private final BufferedImage overlayTop;

	private final BufferedImage overlayBottom;

	public DogePhotoManipulator() {
		this.overlayTop = readClassImage("/doge-top.png");
		this.overlayBottom = readClassImage("/doge-bottom.png");
	}

	private BufferedImage readClassImage(String name) {

		try (InputStream imgInputStream = new ClassPathResource(name).getInputStream()) {
			return ImageIO.read(imgInputStream);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public Photo manipulate(Photo photo) throws IOException {
		BufferedImage sourceImage = readImage(photo);
		BufferedImage destinationImage = manipulate(sourceImage);
		return new BufferedImagePhoto(destinationImage);
	}

	private BufferedImage readImage(Photo photo) throws IOException {
		try (InputStream inputStream = photo.getInputStream()) {
			return ImageIO.read(inputStream);
		}
	}

	private BufferedImage manipulate(BufferedImage sourceImage) {
		double aspectRatio = sourceImage.getHeight() / (double) sourceImage.getWidth();
		int height = (int) Math.floor(IMAGE_WIDTH * aspectRatio);
		BufferedImage destinationImage = new BufferedImage(IMAGE_WIDTH, height,
				BufferedImage.TYPE_INT_RGB);
		render(sourceImage, destinationImage);
		return destinationImage;
	}

	private void render(BufferedImage sourceImage, BufferedImage destinationImage) {
		Graphics2D destinationGraphics = destinationImage.createGraphics();
		try {
			setGraphicsHints(destinationGraphics);
			renderBackground(sourceImage, destinationImage, destinationGraphics);
			renderOverlay(destinationImage, destinationGraphics);
		}
		finally {
			destinationGraphics.dispose();
		}
	}

	private void renderBackground(BufferedImage sourceImage,
			BufferedImage destinationImage, Graphics2D destinationGraphics) {
		destinationGraphics.drawImage(sourceImage, 0, 0, IMAGE_WIDTH,
				destinationImage.getHeight(), null);
	}

	private void renderOverlay(BufferedImage destinationImage,
			Graphics2D destinationGraphics) {
		destinationGraphics.drawImage(this.overlayTop, 0, 0, null);
		int y = destinationImage.getHeight() - this.overlayBottom.getHeight();
		destinationGraphics.drawImage(this.overlayBottom, 0, y, null);
	}

	private void setGraphicsHints(Graphics2D graphics) {
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
	}
}
