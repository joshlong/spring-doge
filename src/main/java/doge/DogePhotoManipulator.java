
package doge;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * A {@link PhotoManipulator} to add Dogeness. Wow. Such Spring. Much
 * AbstractSingletonProxyFactoryBean.
 *
 * @author Phillip Webb
 */
public class DogePhotoManipulator implements PhotoManipulator {

	private static final int IMAGE_WIDTH = 300;

	private final BufferedImage overlayTop;

	private final BufferedImage overlayBottom;

	public DogePhotoManipulator() {
		this.overlayTop = readClassImage("doge-top.png");
		this.overlayBottom = readClassImage("doge-bottom.png");
	}

	private BufferedImage readClassImage(String name) {
		try {
			return ImageIO.read(getClass().getResourceAsStream(name));
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	public Photo manipulate(Photo photo) throws IOException {
		BufferedImage sourceImage = readImage(photo);
		BufferedImage destinationImage = manipulate(sourceImage);
		return new BufferedImagePhoto(destinationImage);
	}

	private BufferedImage readImage(Photo photo) throws IOException {
		InputStream inputStream = photo.getInputStream();
		try {
			return ImageIO.read(inputStream);
		} finally {
			inputStream.close();
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
		} finally {
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
