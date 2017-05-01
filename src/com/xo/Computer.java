package com.xo;

import java.net.InetAddress;

public class Computer {

	private InetAddress address;
	private int port;
	public Computer(InetAddress address, int port) {
		super();
		this.address = address;
		this.port = port;
	}
	public InetAddress getAddress() {
		return address;
	}
	public void setAddress(InetAddress address) {
		this.address = address;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	
	@Override
	public boolean equals(Object obj) {
		return toString().equals(obj.toString());
	}
	
	@Override
	public String toString() {
		return address.getHostAddress()+":"+port;
	}
}
