package com.xima.network;

import com.xima.datadef.CommandID;
import com.xima.datadef.tagCommandCode;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class BroadcastRe extends android.content.BroadcastReceiver implements CommandID {
	Handler myHandler;
	public BroadcastRe(){
	}
	
	public BroadcastRe(Handler myHandler){
		this.myHandler = myHandler;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		///���MSG����
		//��Ϣ����
		Bundle b = new Bundle();
		
		int type = intent.getIntExtra("type", 0);
		if(type != TIMEOUTCONNECTION){
			byte[] data = intent.getByteArrayExtra("data");
			String ServerIP = intent.getStringExtra("ServerIP");
			System.out.println("DEBUG--Broadcast Infomation:"+new String(data));
			//��Ϣ
			b.putByteArray("data", data);
			b.putString("ServerIP",ServerIP);
		}
		
		Message msg = myHandler.obtainMessage();
		//����������handler
		msg.what = type;
		msg.setData(b);
		myHandler.sendMessage(msg);
	}
}
