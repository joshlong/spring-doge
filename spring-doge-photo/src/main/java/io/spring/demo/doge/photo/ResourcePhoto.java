package io.spring.demo.doge.photo;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Josh Long
 */
public class ResourcePhoto implements Photo {
    private final Resource resource;

    @Override
    public InputStream getInputStream() throws IOException {
        return this.resource.getInputStream();
    }

    public ResourcePhoto(Resource resource) {
        this.resource = resource;
    }
}
