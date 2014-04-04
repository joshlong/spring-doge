
package doge;

import java.io.IOException;

/**
 * Service to manipulate a {@link Photo} in some way.
 *
 * @author Phillip Webb
 */
public interface PhotoManipulator {

	/**
	 * Manipulates a photo.
	 *
	 * @param photo the source photo
	 * @return the manipulated photo
	 * @throws IOException
	 */
	Photo manipulate(Photo photo) throws IOException;

}
