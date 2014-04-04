
package doge;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.Test;
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
		Photo photo = new PhotoFile(new File("src/test/resources/thehoff.jpg"));
		Photo manipulatedPhoto = manipulator.manipulate(photo);
		File target = new File("target/test-image-output");
		target.mkdirs();
		FileCopyUtils.copy(manipulatedPhoto.getInputStream(), new FileOutputStream(
				new File(target, "manipulatedhoff.jpg")));
	}

}
