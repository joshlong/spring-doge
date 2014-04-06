package io.spring.demo.doge.photo;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Josh Long
 */
public class InputStreamPhoto  implements Photo{
    @Override
    public InputStream getInputStream() throws IOException {
         return this.inputStream;
    }

    public InputStreamPhoto(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    private final InputStream inputStream ;
}
