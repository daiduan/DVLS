package com.xima.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.util.Log;

public class UDPSender {

	private DatagramPacket dPacket;// ���ݱ�
	private DatagramSocket dSocket;// Socket
	private String dest_ip; // IPv4��ַ
	static final private int SERVER_PORT = 30010; // ���Ͷ˿�
	/*
	 * UDPSender���� 
	 * 1.��ʼ��socket
	 * 2.����IP��ַ strip
	 * 3.�������� msg
	 * 4.�ر�socket
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
	 * ����IP��ַ
	 */
	
	public void SetIP(String dest_ip){
		this.dest_ip = dest_ip;
	}
	/*
	 * ����UDP���ݱ�
	 */
	public synchronized boolean SendMsg(byte[] msg) {
		// ���ݱ�����
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
		// ���ݱ�����
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
