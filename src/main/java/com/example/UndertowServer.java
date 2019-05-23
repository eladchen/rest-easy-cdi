package com.example;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.InstanceHandle;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.ServletInfo;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class UndertowServer {
	private Weld weld;
	private Undertow undertow;
	private WeldContainer weldContainer;
	private DeploymentInfo deploymentInfo = Servlets.deployment();
	private List<ServletInfo> servletInfoList = new ArrayList<>();
	private Function<HttpHandler, HttpHandler> handlerMiddleware = Function.identity();

	public void addServlet(ServletInfo servletInfo) {
		servletInfoList.add(servletInfo);
	}

	public void setWeld(Weld weld) {
		this.weld = weld;
	}

	public void setDeploymentInfo(DeploymentInfo deploymentInfo) {
		this.deploymentInfo = deploymentInfo;
	}

	public void setWeldContainer(WeldContainer weldContainer) {
		this.weldContainer = weldContainer;
	}

	public void setHttpMiddleware(Function<HttpHandler, HttpHandler> func) {
		this.handlerMiddleware = func;
	}

	public synchronized Undertow start(Undertow.Builder undertowBuilder) throws RuntimeException {
		try {
			if (undertow == null) {
				final WeldContainer container = getWeldContainer();

				if (container != null) {
					weldContainer = container;

					useWeldContainer(container, deploymentInfo);
				}

				for (ServletInfo servletInfo : servletInfoList) {
					deploymentInfo.addServlet(servletInfo);
				}

				HttpHandler httpHandler = servletsHandler(deploymentInfo);

				if (handlerMiddleware != null) {
					httpHandler = handlerMiddleware.apply(httpHandler);

					if (httpHandler == null) {
						throw new NullPointerException("httpHandler can not be null");
					}
				}

				undertow = undertowBuilder.setHandler(httpHandler).build();

				undertow.start();

				Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		return undertow;
	}

	// Starts undertow using an http listener on localhost with a random port.
	public synchronized Undertow start() throws RuntimeException {
		return start(Undertow.builder().addHttpListener(getEphemeralPort(), "127.0.0.1"));
	}

	public void stop() {
		if (undertow != null) {
			if (weldContainer != null) {
				weld.shutdown();
			}

			undertow.stop();

			undertow = null;
			weldContainer = null;
		}
	}

	private WeldContainer getWeldContainer() {
		WeldContainer container = null;

		if (weldContainer != null) {
			container = weldContainer;
		} else if (weld != null) {
			container = weld.initialize();
		}

		return container;
	}

	static int getEphemeralPort() {
		try (ServerSocket socket = new ServerSocket(0)) {
			socket.setReuseAddress(true);

			return socket.getLocalPort();
		}
		catch (IOException e) {
			throw new IllegalStateException("Could not find a free TCP/IP port", e);
		}
	}

	static HttpHandler servletsHandler(DeploymentInfo deploymentInfo) throws ServletException {
		DeploymentManager deploymentManager = Servlets.defaultContainer().addDeployment(deploymentInfo);

		deploymentManager.deploy();

		return deploymentManager.start();
	}

	static DeploymentInfo useWeldContainer(final WeldContainer weldContainer, final DeploymentInfo deploymentInfo) {
		// Use the explicit signature – programmatic: true (spelling error)
		final ServletContextListener listener = new WeldListener(weldContainer);

		final InstanceFactory<ServletContextListener> listenerFactory = () -> new InstanceHandle<ServletContextListener>() {
			@Override
			public ServletContextListener getInstance() {
				return listener;
			}

			@Override
			public void release() {
				weldContainer.shutdown();
			}
		};

		deploymentInfo.addListener(new ListenerInfo(listener.getClass(), listenerFactory, true));

		return deploymentInfo;
	}

	public static class EphemeralHttpServer extends UndertowServer {
		private int port = getEphemeralPort();

		public int getPort() {
			return port;
		}

		public String getHost() {
			return "127.0.0.1";
		}

		@Override
		public Undertow start() {
			return start(Undertow.builder().addHttpListener(getPort(), getHost()));
		}
	}
}