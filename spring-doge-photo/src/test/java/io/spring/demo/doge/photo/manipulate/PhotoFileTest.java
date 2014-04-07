package io.spring.demo.doge.photo.manipulate;

import io.spring.demo.doge.photo.FilePhoto;
import io.spring.demo.doge.photo.Photo;
import io.spring.demo.doge.photo.ResourcePhoto;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.io.File;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link FilePhoto}.
 *
 * @author Phillip Webb
 * @author Josh Long
 */
public class PhotoFileTest {

	private File tmpDirectory = new File(System.getProperty("java.io.tmpdir"));

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
		File file = tmpDirectory.getAbsoluteFile();
		this.thown.expect(IllegalArgumentException.class);
		this.thown.expectMessage("is not a file");
		new FilePhoto(file);
	}

	@Test
	public void shouldGetInputStream() throws Exception {
		Resource file = new ClassPathResource("thehoff.jpg");
		Photo photo = new ResourcePhoto(file);
		byte[] originalResource = StreamUtils.copyToByteArray(file.getInputStream());
		byte[] photoResource = StreamUtils.copyToByteArray(photo.getInputStream());
		assertThat(originalResource, equalTo(photoResource));
	}

}
