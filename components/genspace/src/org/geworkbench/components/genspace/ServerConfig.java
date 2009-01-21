package org.geworkbench.components.genspace;

public class ServerConfig {
	private String host;
	private int port;

	public ServerConfig(String h, int p) {
		host = h;
		port = p;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
}