package com.xima.network;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Timer;

import com.xima.datadef.CommandID;
import com.xima.ui.ClassTeachActivity;
import com.xima.utility.ConnectionTimeoutTask;
import com.xima.utility.DataConverter;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;


public class UDPService extends Service implements CommandID {
	private DatagramSocket dSocket;// Socket
	private static final int PORT = 30010;// ��Զ�Ķ˿�
	UDPReceiver URTh;// �����߳�
	// ��ʱ��
	private Timer timer;
	private ConnectionTimeoutTask TimeOutTask;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		 Log.e("TAG", "onBind onUnbind~~~");  
		return null;
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			Intent intent = new Intent();

			/*
			 * intent�д����������
			 * 1.type������UDPReceiver����������ID����
			 * 2.data��������������Ϣ
			 * 3.ServerIP�����ݱ���ԴIP
			 * ָ��action��ȷ����ͬ��activity�յ��Լ�����Ϣ
			 * 
			 */
			if(msg.getData().isEmpty()){
				if(msg.what ==  TIMEOUTCONNECTION)
					intent.setAction("android.intent.action.COMMAND");
			}
			else {
				
				byte[] data = msg.getData().getByteArray("data");
				
				if(data.length == DATALONG){//����command id����
					if(msg.what == TEACHEREXIST)
						ResetConnetcionTimer();// ���ü�ʱ��
					intent.setAction("android.intent.action.COMMAND");
				}
				//�����command id��������
				else switch (msg.what) {
					case TIMEOUTCONNECTION:// �����ʦ���ӳ�ʱ
						intent.setAction("android.intent.action.COMMAND");
						break;
					//��ʦ�Ͽ����ݱ�
					case DEMONSTRATE:
						intent.setAction("android.intent.action.DEMONSTRATE");
						break;
					//������chatroom���ݱ�
					case GETROOM:
						intent.setAction("android.intent.action.GETROOM");
						break;
					case MSGCHATROOM:
						intent.setAction("android.intent.action.MSGCHATROOM");
						break;
					//����ѧϰchatpeer���ݱ�
					case VOICEREQUEST:
					case MSGCHATPEER:
						intent.setAction("android.intent.action.MSGCHATPEER");
						break;
					case REFUSHLIST_RETURN:
						intent.setAction("android.intent.action.REFUSHLIST");
						break;
					default:
						intent.setAction("android.intent.action.DEF");
						break;
				}
				
				intent.putExtra("data", data);
				intent.putExtra("ServerIP", msg.getData().getString("ServerIP"));
			}

			intent.putExtra("type",msg.what);		
			sendBroadcast(intent);
		}
	};
	
	public void initConnectionTimer() {
		timer = new Timer();
		TimeOutTask = new ConnectionTimeoutTask(handler);
		timer.schedule(TimeOutTask, TIMELONG);
		Log.i("timer", "ok");
	}
	
	public void ResetConnetcionTimer() {
		TimeOutTask.cancel();
		TimeOutTask = new ConnectionTimeoutTask(handler);
		timer.schedule(TimeOutTask, TIMELONG);
	}
	
	@Override  
	    public void onCreate() {
	        super.onCreate();  
	     // ��ʼ��Socket
			try {
				dSocket = new DatagramSocket(PORT);
				Log.i("service", "ok");
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i("UDP Receiver", "ok");
			// ��ʼ��UDPReceiver
			URTh = new UDPReceiver(handler, dSocket);
			URTh.start();
	        Log.i("TAG", "Service onCreate~~~");  
	        //��ʼ�����Ӽ�ʱ��
	        initConnectionTimer();
	        //ע������㲥
	        IntentFilter intentFilter = new IntentFilter("com.android.ScIsDied");
	        registerReceiver( myBroadcastReceiver , intentFilter);
	    }
	 
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		dSocket.close();
		URTh.setLife(false);
		super.onDestroy();
	}
	
	private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
		 //�˳��������������
		@Override
		public void onReceive(Context context, Intent arg1) {
			// TODO Auto-generated method stub
//			Intent i = new Intent(context,ClassTeachActivity.class);
//			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			context.startActivity(i);//��������
		}
	 
	    };

}
