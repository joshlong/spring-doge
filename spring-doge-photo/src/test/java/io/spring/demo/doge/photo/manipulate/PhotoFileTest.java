
package io.spring.demo.doge.photo.manipulate;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import io.spring.demo.doge.photo.Photo;
import io.spring.demo.doge.photo.FilePhoto;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.util.StreamUtils;

/**
 * Tests for {@link FilePhoto}.
 *
 * @author Phillip Webb
 */
public class PhotoFileTest {

	@Rule
	public ExpectedException thown = ExpectedException.none();

	@Test
	public void shouldNotBeNull() throws Exception {
		this.thown.expect(IllegalArgumentException.class);
		this.thown.expectMessage("File must not be null");
		new FilePhoto(null);
	}

	@Test
	public void shouldNotBeMissing() throws Exception {
		File file = new File("missing");
		this.thown.expect(IllegalArgumentException.class);
		this.thown.expectMessage("does not exist");
		new FilePhoto(file);
	}

	@Test
	public void shouldNotBeFolder() throws Exception {
		File file = new File("src").getAbsoluteFile();
		this.thown.expect(IllegalArgumentException.class);
		this.thown.expectMessage("is not a file");
		new FilePhoto(file);
	}

	@Test
	public void shouldGetInputStream() throws Exception {
		File file = new File("src/test/resources/thehoff.jpg");
		Photo photo = new FilePhoto(file);
		assertThat(StreamUtils.copyToByteArray(new FileInputStream(file)),
				equalTo(StreamUtils.copyToByteArray(photo.getInputStream())));
	}

}
