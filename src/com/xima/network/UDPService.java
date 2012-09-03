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
	private static final int PORT = 30010;// 永远的端口
	UDPReceiver URTh;// 监听线程
	// 计时器
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
			 * intent中存放三个数据
			 * 1.type：根据UDPReceiver发来的命令ID得来
			 * 2.data：完整的数据信息
			 * 3.ServerIP：数据报来源IP
			 * 指定action，确保不同的activity收到自己的消息
			 * 
			 */
			if(msg.getData().isEmpty()){
				if(msg.what ==  TIMEOUTCONNECTION)
					intent.setAction("android.intent.action.COMMAND");
			}
			else {
				
				byte[] data = msg.getData().getByteArray("data");
				
				if(data.length == DATALONG){//处理command id数据
					if(msg.what == TEACHEREXIST)
						ResetConnetcionTimer();// 重置计时器
					intent.setAction("android.intent.action.COMMAND");
				}
				//处理非command id类型数据
				else switch (msg.what) {
					case TIMEOUTCONNECTION:// 处理教师连接超时
						intent.setAction("android.intent.action.COMMAND");
						break;
					//教师上课数据报
					case DEMONSTRATE:
						intent.setAction("android.intent.action.DEMONSTRATE");
						break;
					//聊天室chatroom数据报
					case GETROOM:
						intent.setAction("android.intent.action.GETROOM");
						break;
					case MSGCHATROOM:
						intent.setAction("android.intent.action.MSGCHATROOM");
						break;
					//自主学习chatpeer数据报
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
	     // 初始化Socket
			try {
				dSocket = new DatagramSocket(PORT);
				Log.i("service", "ok");
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i("UDP Receiver", "ok");
			// 初始化UDPReceiver
			URTh = new UDPReceiver(handler, dSocket);
			URTh.start();
	        Log.i("TAG", "Service onCreate~~~");  
	        //初始化连接计时器
	        initConnectionTimer();
	        //注册监听广播
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
		 //退出程序后重新启动
		@Override
		public void onReceive(Context context, Intent arg1) {
			// TODO Auto-generated method stub
//			Intent i = new Intent(context,ClassTeachActivity.class);
//			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			context.startActivity(i);//启动服务
		}
	 
	    };

}
