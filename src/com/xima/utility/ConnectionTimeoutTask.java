package com.xima.utility;

import java.util.TimerTask;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class ConnectionTimeoutTask extends TimerTask {

	private Handler handler;
	static final int TIMEOUTCONNECTION = 6000;

	public ConnectionTimeoutTask() {
		super();
	}

	public ConnectionTimeoutTask(Handler handler) {
		this.handler = handler;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		handler.sendEmptyMessage(TIMEOUTCONNECTION);
	}

}
