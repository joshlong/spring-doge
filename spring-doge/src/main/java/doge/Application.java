/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package doge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import doge.photo.DogePhotoManipulator;

/**
 * Application configuration and main method.
 *
 * @author Josh Long
 * @author Phillip Webb
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {

	@Bean
	DogePhotoManipulator dogePhotoManipulator() {
		DogePhotoManipulator dogePhotoManipulator = new DogePhotoManipulator();
		dogePhotoManipulator.addTextOverlay("pivotal", "abstractfactorybean", "java");
		dogePhotoManipulator.addTextOverlay("spring", "annotations", "boot");
		dogePhotoManipulator.addTextOverlay("code", "semicolonfree", "groovy");
		dogePhotoManipulator.addTextOverlay("clean", "juergenized", "spring");
		dogePhotoManipulator.addTextOverlay("js", "nonblocking", "wat");
		return dogePhotoManipulator;
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
