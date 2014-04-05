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

package io.spring.demo.doge.server.domain;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

/**
 * {@link TestExecutionListeners} to start and stop mongo.
 * 
 * @author Phillip Webb
 */
public class MongoDbTestExecutionListener extends AbstractTestExecutionListener {

	private MongodExecutable executable;

	private MongodProcess process;

	@Override
	public void beforeTestClass(TestContext testContext) throws Exception {
		MongodStarter starter = MongodStarter.getDefaultInstance();
		MongodConfigBuilder config = new MongodConfigBuilder();
		config.version(Version.Main.PRODUCTION);
		config.net(new Net(27017, Network.localhostIsIPv6()));
		this.executable = starter.prepare(config.build());
		this.process = this.executable.start();
	}

	@Override
	public void afterTestClass(TestContext testContext) throws Exception {
		this.process.stop();
		this.executable.stop();
	}

}
