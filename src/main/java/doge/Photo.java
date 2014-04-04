
package doge;

import java.io.IOException;
import java.io.InputStream;

/**
 * Encapsulation of a photo.
 *
 * @author Phillip Webb
 */
public interface Photo {

	/**
	 * @return a new {@link InputStream} containing photo data. The caller is responsible
	 *         for closing the stream.
	 * @throws IOException
	 */
	public InputStream getInputStream() throws IOException;

}
