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

package io.spring.demo.doge.filesystem;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link JailedResourcePath}.
 * 
 * @author Phillip Webb
 */
public class JailedResourcePathTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private final ResourcePath pathA = new ResourcePath().get("a");

	private final ResourcePath pathB = new ResourcePath().get("b");

	@Test
	public void shouldNeedJail() throws Exception {
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("JailPath must not be null");
		new JailedResourcePath(null, new ResourcePath());
	}

	@Test
	public void shouldNeedPath() throws Exception {
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("Path must not be null");
		new JailedResourcePath(new ResourcePath(), null);
	}

	@Test
	public void shouldCreateFromJailAndPath() throws Exception {
		JailedResourcePath jailedResourcePath = new JailedResourcePath(this.pathA,
				this.pathB);
		assertThat(jailedResourcePath.getJailPath(), is(this.pathA));
		assertThat(jailedResourcePath.getPath(), is(this.pathB));
	}

	@Test
	public void shouldCreateRoot() throws Exception {
		JailedResourcePath jailedResourcePath = new JailedResourcePath();
		assertThat(jailedResourcePath.getJailPath().isRootPath(), is(true));
		assertThat(jailedResourcePath.getPath().isRootPath(), is(true));
	}

	@Test
	public void shouldGetNullParent() throws Exception {
		JailedResourcePath jailedResourcePath = new JailedResourcePath(this.pathA,
				new ResourcePath());
		assertThat(jailedResourcePath.getParent(), is(nullValue()));
	}

	@Test
	public void shouldGetParent() throws Exception {
		JailedResourcePath jailedResourcePath = new JailedResourcePath(this.pathA,
				new ResourcePath().get("b/c"));
		JailedResourcePath expected = new JailedResourcePath(this.pathA, this.pathB);
		assertThat(jailedResourcePath.getParent(), is(equalTo(expected)));
	}

	@Test
	public void shouldGet() throws Exception {
		JailedResourcePath jailedResourcePath = new JailedResourcePath(this.pathA,
				new ResourcePath());
		jailedResourcePath = jailedResourcePath.get("b/c");
		JailedResourcePath expected = new JailedResourcePath(this.pathA,
				new ResourcePath().get("b/c"));
		assertThat(jailedResourcePath, is(equalTo(expected)));

	}

	@Test
	public void shouldGetUnjailedRootPath() throws Exception {
		JailedResourcePath jailedResourcePath = new JailedResourcePath(
				new ResourcePath(), this.pathA);
		assertThat(jailedResourcePath.getUnjailedPath(),
				is(equalTo(new ResourcePath().get("a"))));
	}

	@Test
	public void shouldGetUnjailedPath() throws Exception {
		JailedResourcePath jailedResourcePath = new JailedResourcePath(this.pathA,
				this.pathB);
		assertThat(jailedResourcePath.getUnjailedPath(),
				is(equalTo(new ResourcePath().get("a/b"))));
	}

	@Test
	public void shouldUsePathToString() throws Exception {
		JailedResourcePath jailedResourcePath = new JailedResourcePath(this.pathA,
				this.pathB);
		assertThat(jailedResourcePath.toString(), is(equalTo("/b")));
	}
}
