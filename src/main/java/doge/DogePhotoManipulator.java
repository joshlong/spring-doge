
package doge;

import java.awt.Graphics;
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

	public Photo manipulate(Photo photo) throws IOException {
		BufferedImage image = readImage(photo);
		manipulate(image);
		return new BufferedImagePhoto(image);
	}

	private BufferedImage readImage(Photo photo) throws IOException {
		InputStream inputStream = photo.getInputStream();
		try {
			return ImageIO.read(inputStream);
		} finally {
			inputStream.close();
		}
	}

	private void manipulate(BufferedImage image) {
		Graphics g = image.getGraphics();
		g.setFont(g.getFont().deriveFont(30f));
		g.drawString("Hello World!", 100, 100);
		g.dispose();
	}
}
