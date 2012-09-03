package com.xima.ui;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import com.xima.datadef.CommandID;
import com.xima.datadef.UserInfo;
import com.xima.datadef.tagCommandCode;
import com.xima.network.UDPSender;
import com.xima.ui.PlayerActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public abstract class BaseActivity extends Activity implements CommandID,OnClickListener{
	
	// -------------------------------------------------------------------
	// 右侧按钮
	protected ImageButton ClassingButton;//上课
	protected ImageButton InternetingButton;//网上冲浪
	protected ImageButton VodingButton;//自主点播
	protected ImageButton PlayingButton;//媒体播放
	protected ImageButton ReadingButton;//电子阅读
	protected ImageButton ChatingButton;//学习交流
	protected ImageButton ChatroomButton;//聊天室
	// -------------------------------------------------------------------
	//底部按钮
	protected ImageButton Hand;//举手
	protected boolean bHandup;//举手状态
	protected boolean bHandAllowed;//举手屏蔽
	protected boolean lock = false;//锁键盘鼠标
	//底部指示灯
	ImageView pbLandlight;
	AnimationDrawable draw;
	//底部文本状态信息
	TextView tvSystemState;//系统状态
	TextView tvName;//名字
	TextView tvPC;//PC名称，可能不用
	TextView tvIP;//IP地址
	// -------------------------------------------------------------------
	// 命令
	tagCommandCode cmd;
	// -------------------------------------------------------------------
	// UDP发送
	UDPSender us;
	//当前与服务器连接状态
	boolean connected;
	//当前服务器初始化状态
	boolean initialed;
	//服务器地址，需要处理
	String ServerIP;
	//音量调节
	protected SeekBar seekBar;
    protected AudioManager audioManager;
    protected Dialog WarnDialog,LockDialog;

	@Override  
	    protected void onResume() {  
		super.onResume();  
			if(ServerIP == null)
				us = new UDPSender();
			else 
				us = new UDPSender(ServerIP);
	 }

	 @Override  
	    protected void onPause() {  
	   super.onPause();  
	   us.destroy();
	 }
	 
	 @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		 Intent intent = new Intent("com.android.ScIsDied");
	        sendBroadcast(intent);
		super.onDestroy();
	}

	// 初始化
			public void init() {
				initData();
				initView();
				initNetwork();
				initDialog();
		}
 
	 public void initData() {
			// 初始化cmd数据
			UserInfo mycmd = ((UserInfo) getApplicationContext());
			if(mycmd.isEmpty()){// 设置为未连接教师端
				connected = false;
				initialed = false;
			}else{ //获得公共信息初始化
				cmd = mycmd.getData();
				ServerIP = mycmd.getIP();
				connected = true;
				initialed = true;
			}
	 }

	 public void initView() {
		 
	 }
	 
	 public void initNetwork(){ 
	 }

	//按钮监听器
	protected OnClickListener OnClickListener =new OnClickListener(){
	public void onClick(View v){
		if (v.equals(Hand)) {
			if(connected)
			if (bHandup) {
				cmd.SetCmdID(DROPEHAND);
				us.SendMsg(cmd.toByteArray());
				Hand.setImageResource(R.drawable.hand_on);
				bHandup = false;
			} else {
				cmd.SetCmdID(RAISEHAND);
				us.SendMsg(cmd.toByteArray());
				Hand.setImageResource(R.drawable.hand_pressed);
				bHandup = true;
			}
		}else if(v.equals(ClassingButton)){
			Intent intent = new Intent();
			intent.setClass(v.getContext(),ClassTeachActivity.class);
			startActivity(intent);
		}
		else if (v.equals(InternetingButton)) {
			Intent intent = new Intent();
			intent.setClass(v.getContext(),InternetActivity.class);
			startActivity(intent);
		}
		else if (v.equals(VodingButton)) {
			Intent intent = new Intent();
			intent.setClass(v.getContext(),BrowserActivity.class);
			startActivity(intent);
		}
		else if (v.equals(PlayingButton)) {
			Intent intent = new Intent();
			intent.setClass(v.getContext(),PlayerActivity.class);
			startActivity(intent);
		}
		else if (v.equals(ReadingButton)) {
			Intent intent = new Intent();
			intent.setClass(v.getContext(),ReadingActivity.class);
			startActivity(intent);
		}
		else if (v.equals(ChatingButton)) {
			Intent intent = new Intent();
			intent.setClass(v.getContext(),ChatPeerActivity.class);
			startActivity(intent);
		}
		else if (v.equals(ChatroomButton)) {
			Intent intent = new Intent();
			intent.setClass(v.getContext(),ChatRoomActivity.class);
			startActivity(intent);
		}
	}
	
	};
	
	public void unconnected(){
		pbLandlight.setImageResource(R.anim.un_connected);
		pbLandlight.post(new Runnable() {
		    @Override
		    public void run() {
		        AnimationDrawable frameAnimation = (AnimationDrawable) pbLandlight.getDrawable();
		        frameAnimation.start();
		    }
		});
	}

	 public String getLocalIpAddress() {   
	        try {   
	            for (Enumeration<NetworkInterface> en = NetworkInterface   
	                    .getNetworkInterfaces(); en.hasMoreElements();) {  //枚举网卡  
	                NetworkInterface intf = en.nextElement();   
	                for (Enumeration<InetAddress> enumIpAddr = intf   
	                        .getInetAddresses(); enumIpAddr.hasMoreElements();) { //枚举网络地址    
	                    InetAddress inetAddress = enumIpAddr.nextElement();   
	                    if (!inetAddress.isLoopbackAddress()) {   //非回送地址
	                        return inetAddress.getHostAddress().toString();   //返回第一个找到的IP地址
	                    }   
	                }   
	            }   
	        } catch (SocketException ex) {   
	            Log.e("WifiPreference IpAddress", ex.toString());   
	        }   
	        return null;   
	    }
	 
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		//屏蔽返回键
////	if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
////		return true;
//		if (lock) {
//			return true;
//		}else 
//		return false;
//	}
	
	 @Override
	 public boolean onKeyDown(int keyCode, KeyEvent event)
	{ // TODO Auto-generated method stub
	  if(KeyEvent.KEYCODE_HOME==keyCode){
	  }
	    //android.os.Process.killProcess(android.os.Process.myPid());
	   return super.onKeyDown(keyCode, event);
	  }

	@Override
	 public void onAttachedToWindow()
	 { // TODO Auto-generated method stub
	    this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
	    super.onAttachedToWindow();
	 }
	
	 @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	         // TODO Auto-generated method stub
	         if(requestCode == 0 && resultCode == RESULT_OK) {
	            
	         }
     }
	 
	 OnSeekBarChangeListener osbcl = new OnSeekBarChangeListener() {

		 
	        @Override
	        public void onProgressChanged(SeekBar seekBar, int progress,
	                boolean fromUser) {
	            // TODO Auto-generated method stub
	            if (fromUser) {
	                int SeekPosition=seekBar.getProgress();
	                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, SeekPosition, 0);
	                if(connected){
	    				cmd.SetCmdID(STUSETVOLUME);
	    				cmd.iReserver[0]=seekBar.getProgress();
	    				us.SetIP(ServerIP);
	    				us.SendMsg(cmd.toByteArray());
	    			}
	            }
	          //  volumeView.setText(String.valueOf(progress));
	        }
	        @Override
	        public void onStartTrackingTouch(SeekBar seekBar) {
	            // TODO Auto-generated method stub	            
	        }
	        @Override
	        public void onStopTrackingTouch(SeekBar seekBar) {
	            // TODO Auto-generated method stub
	            
	        }
	
		};
		
 
		public void initDialog() {
			// TODO Auto-generated method stub
			 WarnDialog = new AlertDialog.Builder(this)
		        .setIcon(android.R.drawable.btn_star).setTitle("同学")
		        .setMessage("请认真听讲").setCancelable(false)
		        .setNegativeButton("关闭", new DialogInterface.OnClickListener() {  
					           public void onClick(DialogInterface dialog, int id) {  
					                dialog.cancel();  
					           }  
					       }).create(); 

		    LockDialog = new AlertDialog.Builder(this)
		    	.setIcon(android.R.drawable.btn_star).setTitle("同学")
		    	.setMessage("机器已经被锁定")
		    	.setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode,
							KeyEvent event) {
						// TODO Auto-generated method stub
						if(KeyEvent.KEYCODE_HOME==keyCode){
						  return true;}
						return true;
					}
				}).create();

		}


}
