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
	// �Ҳఴť
	protected ImageButton ClassingButton;//�Ͽ�
	protected ImageButton InternetingButton;//���ϳ���
	protected ImageButton VodingButton;//�����㲥
	protected ImageButton PlayingButton;//ý�岥��
	protected ImageButton ReadingButton;//�����Ķ�
	protected ImageButton ChatingButton;//ѧϰ����
	protected ImageButton ChatroomButton;//������
	// -------------------------------------------------------------------
	//�ײ���ť
	protected ImageButton Hand;//����
	protected boolean bHandup;//����״̬
	protected boolean bHandAllowed;//��������
	protected boolean lock = false;//���������
	//�ײ�ָʾ��
	ImageView pbLandlight;
	AnimationDrawable draw;
	//�ײ��ı�״̬��Ϣ
	TextView tvSystemState;//ϵͳ״̬
	TextView tvName;//����
	TextView tvPC;//PC���ƣ����ܲ���
	TextView tvIP;//IP��ַ
	// -------------------------------------------------------------------
	// ����
	tagCommandCode cmd;
	// -------------------------------------------------------------------
	// UDP����
	UDPSender us;
	//��ǰ�����������״̬
	boolean connected;
	//��ǰ��������ʼ��״̬
	boolean initialed;
	//��������ַ����Ҫ����
	String ServerIP;
	//��������
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

	// ��ʼ��
			public void init() {
				initData();
				initView();
				initNetwork();
				initDialog();
		}
 
	 public void initData() {
			// ��ʼ��cmd����
			UserInfo mycmd = ((UserInfo) getApplicationContext());
			if(mycmd.isEmpty()){// ����Ϊδ���ӽ�ʦ��
				connected = false;
				initialed = false;
			}else{ //��ù�����Ϣ��ʼ��
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

	//��ť������
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
	                    .getNetworkInterfaces(); en.hasMoreElements();) {  //ö������  
	                NetworkInterface intf = en.nextElement();   
	                for (Enumeration<InetAddress> enumIpAddr = intf   
	                        .getInetAddresses(); enumIpAddr.hasMoreElements();) { //ö�������ַ    
	                    InetAddress inetAddress = enumIpAddr.nextElement();   
	                    if (!inetAddress.isLoopbackAddress()) {   //�ǻ��͵�ַ
	                        return inetAddress.getHostAddress().toString();   //���ص�һ���ҵ���IP��ַ
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
//		//���η��ؼ�
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
		        .setIcon(android.R.drawable.btn_star).setTitle("ͬѧ")
		        .setMessage("����������").setCancelable(false)
		        .setNegativeButton("�ر�", new DialogInterface.OnClickListener() {  
					           public void onClick(DialogInterface dialog, int id) {  
					                dialog.cancel();  
					           }  
					       }).create(); 

		    LockDialog = new AlertDialog.Builder(this)
		    	.setIcon(android.R.drawable.btn_star).setTitle("ͬѧ")
		    	.setMessage("�����Ѿ�������")
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
