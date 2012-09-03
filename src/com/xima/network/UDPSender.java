package com.xima.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.util.Log;

public class UDPSender {

	private DatagramPacket dPacket;// 数据报
	private DatagramSocket dSocket;// Socket
	private String dest_ip; // IPv4地址
	static final private int SERVER_PORT = 30010; // 发送端口
	/*
	 * UDPSender步骤 
	 * 1.初始化socket
	 * 2.设置IP地址 strip
	 * 3.发送数据 msg
	 * 4.关闭socket
	 */
	public UDPSender() {
		// TODO Auto-generated constructor stub
		super();
		try {
			dSocket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("dsocket is created");
	}
	
	public UDPSender(String ServerIP) {
		// TODO Auto-generated constructor stub
		super();
		try {
			dSocket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.dest_ip = ServerIP;
		System.out.println("dsocket is created");
	}
	
	/*
	 * 设置IP地址
	 */
	
	public void SetIP(String dest_ip){
		this.dest_ip = dest_ip;
	}
	/*
	 * 发送UDP数据报
	 */
	public synchronized boolean SendMsg(byte[] msg) {
		// 数据报生成
		dPacket = new DatagramPacket(msg, msg.length);
		try {
			dPacket.setLength(msg.length);
			dPacket.setAddress(InetAddress.getByName(dest_ip));
			dPacket.setPort(SERVER_PORT);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("dPacket Error");
			return false;
		}
		// 数据报发送
		try {
			dSocket.send(dPacket);
			Log.i("UDPSender=====>", "sent!");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("send Error");
			return false;
		}
		return true;
	}

	public void destroy() {
		dSocket.close();
		System.out.println("ds is closed");
	}

}
