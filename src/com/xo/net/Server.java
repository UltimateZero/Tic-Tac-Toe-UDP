package com.xo.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;

import com.xo.Computer;
import com.xo.PacketListener;
import com.xo.core.GameCore.Player;

public class Server {
	private static final String DISCOVERY_REQUEST = "DISCOVER_XO";
	private static final String DISCOVERY_RESPONSE = "RUN_XO";
	
	private static final String PLAY_REQUEST = "PLAY_REQUEST";
	private static final String PLAY_ACCEPT = "PLAY_ACCEPT";
	private static final String PLAY_REJECT = "PLAY_REJECT";
	
	private static final String MOVE_PREFIX = "MOVE_";
	
	private int PORT = 4445;
    private DatagramSocket serverSocket;
    private boolean running;
    private byte[] receiveBuffer = new byte[256];
    private PacketListener listener;
    private Computer myComputer;
 
    public Server(PacketListener listener) {
        try {
        	myComputer = new Computer(InetAddress.getLocalHost(), PORT);
        	this.listener = listener;
        	serverSocket = new DatagramSocket(PORT);
        	serverSocket.setBroadcast(true);
        	Arrays.fill(receiveBuffer, (byte)0);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
    }
 
    public void startListening() {
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
		        running = true;
		        
		        while (running) {
		        	System.out.println(getClass().getName() + ">>>Ready to receive packets!");

		        	 
		            DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
		            
		            try {
		            	serverSocket.receive(packet);

		            	Computer computer = new Computer(packet.getAddress(), packet.getPort());

		            	String message = new String(packet.getData()).trim().split("\0")[0]; //Consider null is end of message

		            	System.out.println(getClass().getName() + ">>>Packet received from: " + packet.getAddress().getHostAddress());
		            	System.out.println(getClass().getName() + ">>>Packet received; data: " + message);
		            	Arrays.fill(receiveBuffer, (byte)0);
		            	
		            	if(message.startsWith(DISCOVERY_REQUEST) && !(myComputer.equals(computer))) {
		            		listener.discoveryRequestReceived(computer);
		            	}
		            	else if(message.startsWith(DISCOVERY_RESPONSE)) {
		            		listener.discoveryResponseReceived(computer);
		            		System.out.println(getClass().getName() + ">>>Found player: " + packet.getAddress().getHostAddress());
		            	}
		            	
		            	else if(message.startsWith(PLAY_REQUEST)) {
		            		//Request game
		            		listener.playRequestReceived(computer);
		            	}
		            	else if(message.startsWith(PLAY_ACCEPT)){
		            		//Player accepted game
		            		listener.playAcceptReceived(computer);
		            	}
		            	else if(message.startsWith(PLAY_REJECT)) {
		            		//Player rejected game
		            		listener.playRejectReceived(computer);
		            	}
		            	else if(message.startsWith(MOVE_PREFIX)) {
		            		//Player made a move
		            		
		            		String suffix = message.split(MOVE_PREFIX)[1];
		            		Player player = Player.valueOf(suffix.split("_")[0]);
		            		int row = Integer.parseInt(suffix.split("_")[1]);
		            		int column = Integer.parseInt(suffix.split("_")[2]);
		            		System.out.println("Player: " + player + ", Row: " + row +", Column: " + column);
		            		listener.moveReceived(computer, row, column, player);
		            	}
					} catch (IOException e) {
						e.printStackTrace();
					}
		             
		        }
		        serverSocket.close();
			}
		}).start();

    }
    
    public void sendDiscoveryResponse(Computer computer) throws IOException {
    	DatagramPacket sendPacket = new DatagramPacket(DISCOVERY_RESPONSE.getBytes(), DISCOVERY_RESPONSE.getBytes().length,
    			computer.getAddress(), computer.getPort());
		serverSocket.send(sendPacket);
    }
    
    public void sendPlayAccept(Computer computer) throws IOException {
    	byte[] sendData = (PLAY_ACCEPT).getBytes();
    	DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, computer.getAddress(), computer.getPort());
    	serverSocket.send(sendPacket);
    }
    
    public void sendPlayReject(Computer computer) throws IOException {
    	byte[] sendData = (PLAY_REJECT).getBytes();
    	DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, computer.getAddress(), computer.getPort());
    	serverSocket.send(sendPacket);
    }
    
    public void sendPlayRequest(Computer computer) throws IOException {
    	byte[] sendData = (PLAY_REQUEST).getBytes();
    	DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, computer.getAddress(), computer.getPort());
    	serverSocket.send(sendPacket);
    }
    
    public void sendMove(int row, int column, Player player, Computer computer) throws IOException {
    	byte[] sendData = (MOVE_PREFIX  + player + "_" + row + "_" + column).getBytes();
    	DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, computer.getAddress(), computer.getPort());
    	serverSocket.send(sendPacket);
    }
    
    public void sendDiscoveryRequest() throws SocketException {
    	byte[] sendData = DISCOVERY_REQUEST.getBytes();
    	
    	  // Get all network interfaces
    	  Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
    	  while (interfaces.hasMoreElements()) {
    	    NetworkInterface networkInterface = interfaces.nextElement();

    	    
    	    if (!networkInterface.isUp() || networkInterface.isLoopback()
    	    		|| networkInterface.getDisplayName().contains("VMware") || networkInterface.getDisplayName().contains("Npcap")) {
    	      continue; // Don't send to loopback interfaces
    	    }

    	    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
    	      InetAddress broadcast = interfaceAddress.getBroadcast();
    	      if (broadcast == null) {
    	        continue;
    	      }

    	      // Send the broadcast
    	      try {
    	        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, PORT);
    	        serverSocket.send(sendPacket);
    	      } catch (Exception e) {
    	    	  e.printStackTrace();
    	      }

    	      System.out.println(getClass().getName() + ">>> Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
    	    }
    	  }

    	  System.out.println(getClass().getName() + ">>> Done broadcasting");

    }
}
