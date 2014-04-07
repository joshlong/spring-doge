package io.spring.demo.doge.photo;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Josh Long
 * @author Phillip Webb
 */
public class InputStreamPhoto implements Photo {

	// FIXME should delete this as cannot call getInputStream multiple times

	private final InputStream inputStream;

	@Override
	public InputStream getInputStream() throws IOException {
		return this.inputStream;
	}

	public InputStreamPhoto(InputStream inputStream) {
		this.inputStream = inputStream;
	}

}
