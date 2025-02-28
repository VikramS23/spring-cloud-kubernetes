/*
 * Copyright 2013-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.kubernetes.configuration.watcher;

import io.kubernetes.client.common.KubernetesObject;
import reactor.core.publisher.Mono;

import org.springframework.cloud.bus.event.PathDestinationFactory;
import org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;
import org.springframework.cloud.bus.event.ShutdownRemoteApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import static org.springframework.cloud.kubernetes.configuration.watcher.ConfigurationWatcherConfigurationProperties.RefreshStrategy.SHUTDOWN;

/**
 * An event publisher for an 'event bus' type of application.
 *
 * @author wind57
 */
final class BusRefreshTrigger implements RefreshTrigger {

	private final ApplicationEventPublisher applicationEventPublisher;

	private final String busId;

	private final ConfigurationWatcherConfigurationProperties watcherConfigurationProperties;

	BusRefreshTrigger(ApplicationEventPublisher applicationEventPublisher, String busId,
			ConfigurationWatcherConfigurationProperties watcherConfigurationProperties) {
		this.applicationEventPublisher = applicationEventPublisher;
		this.busId = busId;
		this.watcherConfigurationProperties = watcherConfigurationProperties;
	}

	@Override
	public Mono<Void> triggerRefresh(KubernetesObject configMap, String appName) {
		applicationEventPublisher.publishEvent(createRefreshApplicationEvent(configMap, appName));
		return Mono.empty();
	}

	private RemoteApplicationEvent createRefreshApplicationEvent(KubernetesObject configMap, String appName) {
		if (watcherConfigurationProperties.getRefreshStrategy() == SHUTDOWN) {
			return new ShutdownRemoteApplicationEvent(configMap, busId,
					new PathDestinationFactory().getDestination(appName));
		}
		return new RefreshRemoteApplicationEvent(configMap, busId,
				new PathDestinationFactory().getDestination(appName));
	}

}
