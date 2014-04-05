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

package io.spring.demo.doge.server.tosort;


//@RestController
//@RequestMapping(value = PhotoUploadRestController.PHOTO_URI)
class PhotoUploadRestController {
	//
	// public static final String PHOTO_URI = "/users/{user}/photo";
	//
	// private final PhotoService photoService;
	//
	// @Autowired
	// public PhotoUploadRestController(PhotoService photoService) {
	// this.photoService = photoService;
	// }
	//
	// @RequestMapping(method = RequestMethod.GET)
	// public ResponseEntity<byte[]> read(@PathVariable long user) throws IOException {
	// Photo photo = this.photoService.readPhoto(user);
	// HttpHeaders httpHeaders = new HttpHeaders();
	// httpHeaders.setContentType(MediaType.parseMediaType(photo.getContentType()));
	// return new ResponseEntity<>(photo.getPhoto(), httpHeaders, HttpStatus.OK);
	// }
	//
	// @RequestMapping(method = RequestMethod.POST)
	// public HttpEntity<Void> write(@PathVariable long user,
	// @RequestParam MultipartFile file) throws Throwable {
	// byte bytesForProfilePhoto[] = FileCopyUtils
	// .copyToByteArray(file.getInputStream());
	//
	// this.photoService.writePhoto(user,
	// MediaType.parseMediaType(file.getContentType()), bytesForProfilePhoto);
	//
	// HttpHeaders httpHeaders = new HttpHeaders();
	// URI uriOfPhoto = ServletUriComponentsBuilder.fromCurrentContextPath()
	// .pathSegment(PhotoUploadRestController.PHOTO_URI.substring(1))
	// .buildAndExpand(Collections.singletonMap("user", user)).toUri();
	// httpHeaders.setLocation(uriOfPhoto);
	//
	// return new ResponseEntity<Void>(httpHeaders, HttpStatus.CREATED);
	// }
	//
}
