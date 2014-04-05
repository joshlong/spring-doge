
package io.spring.demo.doge.photo.manipulate;

import io.spring.demo.doge.photo.Photo;
import io.spring.demo.doge.photo.FilePhoto;
import io.spring.demo.doge.photo.manipulate.DogePhotoManipulator;
import io.spring.demo.doge.photo.manipulate.PhotoManipulator;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

/**
 * Tests for {@link DogePhotoManipulator}.
 *
 * @author Phillip Webb
 */
public class DogePhotoManipulatorTest {

	private PhotoManipulator manipulator = new DogePhotoManipulator();

	@Test
	public void testDogeService() throws Exception {

        Resource resource  = new ClassPathResource("thehoff.jpg") ;

		Photo photo = new FilePhoto(
                new File("src/test/resources/thehoff.jpg"));
		Photo manipulatedPhoto = manipulator.manipulate(photo);
		File target = new File("target/test-image-output");
		target.mkdirs();
		FileCopyUtils.copy(manipulatedPhoto.getInputStream(), new FileOutputStream(
				new File(target, "manipulatedhoff.jpg")));
	}

}
