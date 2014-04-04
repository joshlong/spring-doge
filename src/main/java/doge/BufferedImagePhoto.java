
package doge;

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

	public InputStream getInputStream() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ImageIO.write(this.image, "jpeg", outputStream);
		return new ByteArrayInputStream(outputStream.toByteArray());
	}

}
