
package doge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.util.Assert;

/**
 * {@link Photo} backed by a {@link java.io.File}.
 *
 * @author Phillip Webb
 */
public class PhotoFile implements Photo {

	private final File file;

	public PhotoFile(File file) {
		Assert.notNull(file, "File must not be null");
		Assert.isTrue(file.exists(), "File '" + file + "' does not exist");
		Assert.isTrue(file.isFile(), "File '" + file + "' is not a file");
		this.file = file;
	}

	public InputStream getInputStream() throws IOException {
		return new FileInputStream(file);
	}

}
