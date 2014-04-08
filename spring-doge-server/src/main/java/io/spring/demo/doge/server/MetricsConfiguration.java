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

package io.spring.demo.doge.server;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;

@Configuration
public class MetricsConfiguration {

	private static final InetSocketAddress ADDRESS = new InetSocketAddress("localhost",
			2003);

	@Bean
	@Conditional(GraphiteCondition.class)
	public GraphiteReporter graphiteReporter(MetricRegistry registry) {
		Graphite graphite = new Graphite(ADDRESS);
		GraphiteReporter reporter = GraphiteReporter.forRegistry(registry)
				.prefixedWith("doge.spring.io").build(graphite);
		reporter.start(2, TimeUnit.SECONDS);
		return reporter;
	}

	public static class GraphiteCondition implements Condition {

		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			Socket socket = new Socket();
			try {
				socket.connect(ADDRESS);
				socket.close();
				return true;
			}
			catch (Exception ex) {
				return false;
			}
		}
	}

}
