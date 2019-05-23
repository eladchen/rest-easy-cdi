package com.example.boot;

import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.apache.logging.log4j.LogManager.getLogger;

public class BootManager implements BootService {
	private static final Logger logger = getLogger(BootManager.class);

	private final List<NamedBootService> bootServiceList = new ArrayList<>();

	private BootManager() {
		Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
	}

	public BootManager addBootService(final NamedBootService namedBootService) {
		if (namedBootService == null) {
			throw new IllegalArgumentException("namedBootService can not be null");
		}

		bootServiceList.add(namedBootService);

		return this;
	}

	public BootManager addBootService(final BootService bootService) {
		if (bootService == null) {
			throw new IllegalArgumentException("bootService can not be null");
		}

		return addBootService(new NamedBootService(bootService, "AnonymousService"));
	}

	@Override
	public void boot() {
		bootServiceList.forEach(namedBootService -> {
			final String serviceName = namedBootService.getName();
			final BootService bootService = namedBootService.getNamedObject();

			logger.debug("Service {} is about to boot", serviceName);

			bootService.boot();

			logger.debug("Service {} booted successfully", serviceName);
		});
	}

	@Override
	public void shutdown() {
		bootServiceList.forEach(namedBootService -> {
			logger.debug("{} Service is about to shutdown", namedBootService.getName());

			namedBootService.getNamedObject().shutdown();
		});
	}

	public static BootManager getInstance() {
		return BootManagerInstance.getInstance();
	}

	// Lazy Single Instance (Bill Pugh's thread-safe)
	private static class BootManagerInstance {
		private static BootManager INSTANCE = null;

		private static BootManager getInstance() {
			if (INSTANCE == null) {
				INSTANCE = new BootManager();
			}

			return INSTANCE;
		}
	}
}
