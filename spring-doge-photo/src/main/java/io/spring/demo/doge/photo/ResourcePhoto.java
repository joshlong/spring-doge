package io.spring.demo.doge.photo;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.Resource;

/**
 * @author Josh Long
 * @author Phillip Webb
 */
public class ResourcePhoto implements Photo {

	// FIXME check if we need this

	private final Resource resource;

	@Override
	public InputStream getInputStream() throws IOException {
		return this.resource.getInputStream();
	}

	public ResourcePhoto(Resource resource) {
		this.resource = resource;
	}
}
