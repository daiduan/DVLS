package com.xima.network;

import com.xima.ui.ClassTeachActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//����Զ�����service����������Ա����
public class ServiceBootReceiver extends BroadcastReceiver {
	static final String ACTION = "android.intent.action.BOOT_COMPLETED";
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals(ACTION)){
			Intent i = new Intent(context,ClassTeachActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);//��������
			}
		}
}
