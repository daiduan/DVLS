package com.xima.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import com.xima.datadef.CommandID;
import com.xima.utility.DataConverter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class UDPReceiver extends Thread implements CommandID {

	private boolean life = true;
	private Handler handler;
	DatagramSocket dSocket;

	public UDPReceiver(Handler handler, DatagramSocket dSocket) {
		this.handler = handler;
		this.dSocket = dSocket;
	}

	/**
	 * @return the life
	 */
	public boolean isLife() {
		return life;
	}

	/**
	 * @param life
	 *            the life to set
	 */
	public void setLife(boolean life) {
		this.life = life;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		// ׼�����ܿռ�
		byte[] recv_buf = new byte[10240];
		DatagramPacket dPacket = new DatagramPacket(recv_buf, recv_buf.length);
		while (life) {// һֱ��ѯ��ֱ���ر�
			// ��������
			try {
				dSocket.receive(dPacket);
				
				if (dPacket.getLength() > 0) {
					
					byte[] bytes = new byte[dPacket.getLength()];
					System.arraycopy(dPacket.getData(), 0, bytes, 0,
							bytes.length);
					
					Message msg = handler.obtainMessage();
					Bundle b = new Bundle();
					//�����Ϣͷ-command id
					byte[] header = new byte[4];
					System.arraycopy(bytes,0,header, 0, 4);
					msg.what = DataConverter.bytes2int(header);
					//��������
					b.putByteArray("data", bytes);
					//�����ϢԴIP
					b.putString("ServerIP",dPacket.getAddress().getHostAddress());					
					msg.setData(b);
					handler.sendMessage(msg);
					Log.i("dPackt--HEAD!====>", ""+new String(header));
				}
			} catch (IOException e) {
				// ��ʱ
				System.out.println("UDPReceiver catch out");
				e.printStackTrace();
			}
		} // end of while
	} // end of run

}
