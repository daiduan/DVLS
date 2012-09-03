package com.xima.ui;

import java.io.UnsupportedEncodingException;

import com.xima.datadef.UserInfo;
import com.xima.datadef.tagCommandCode;
import com.xima.datadef.tagCommandStream;
import com.xima.network.BroadcastRe;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.PluginState;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class InternetActivity extends BaseActivity {
	/*-------------------�������----------------------*/
	//��ť
	private Button goButton;
	private Button backButton;
	private Button forwardButton;
	private Button stopButton;
	private Button reloadButton;
	private Button homeButton;
	private Button openButton;
	//��ַ��
	private EditText EXT;	
	//��ҳ��ͼ
	private WebView webview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.interneting);
		init();
		
	}
	// -------------------------------------------------------------------
		/* ͨ��Handler��������Ϣ���������ݵ���Ϣ */
		private Handler myHandler = new Handler() {
			@Override
			public synchronized void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				// ----------------------------------------------
				switch(msg.what){
				//--------------ͨ����Ϣ-------------------------//
				case TEACHEREXIST:// ��ʦ�˴��ڣ����û�б���ʼ�������ʦ��������Ϣ
					//��������״̬
					if(!initialed){//δ��ʼ���������ʼ����Ϣ
							//���ý�ʦ��IP
							if(ServerIP == null){
								ServerIP = msg.getData().getString("ServerIP");
							}
							us.SetIP(ServerIP);
							//����ѧ����Ϣ����
							tagCommandCode tcmd = new tagCommandCode(" "," "," ");//����Ϊnull������
							tcmd.SetCmdID(GETSTUINFO);
							us.SendMsg(tcmd.toByteArray());
							Log.i("ActivityInfo---Login===>", "GETSTUINFO");
							tcmd.SetCmdID(LOGIN);// ����ID
							us.SendMsg(tcmd.toByteArray());// ������Ϣ
							Log.i("ActivityInfo---Login===>", "Not Initialed");
					}else {
						if(!connected){//�����ı�ʱ�Ų���
							connected = true;
							pbLandlight.setImageResource(R.drawable.green);//����Ϊ����
							Log.i("LandLight========>","Online!!!");
							}
						if(ServerIP == null){
							ServerIP = msg.getData().getString("ServerIP");
						}
						cmd.SetCmdID(GETSTUINFO);
						us.SendMsg(cmd.toByteArray());
						Log.i("ActivityInfo---Login===>", "GETSTUINFO");
						us.SetIP(ServerIP);
						cmd.SetCmdID(LOGIN);// ����ID
						us.SendMsg(cmd.toByteArray());// ������Ϣ
						Log.i("ActivityInfo---Login===>", "Connected & Initialed");
					}
					break;
				case ACCEPT://���ʦ�����ӣ���ʼ��ϵͳ����
				case GETSTUINFO_RETURN://���ѧ����Ϣ
					Log.i("ActivityInfo---ACCEPT===>", "Initialed!");
					//��ʼ������
					tagCommandCode tcmd = new tagCommandCode(msg.getData().getByteArray("data"));
					//��ʼ����Ϣ
					String StrLocalIP = getLocalIpAddress();//����IP
					String subIP = StrLocalIP.substring(0, StrLocalIP.lastIndexOf(".")+1);//����
					
					String StrName; 
					if(tcmd.strName!= null){
						StrName  = tcmd.strName;//ѧ������
					}else StrName ="STU"+StrLocalIP.substring(StrLocalIP.lastIndexOf("."),StrLocalIP.length());
					
					if(ServerIP==null){
						ServerIP = msg.getData().getString("ServerIP");
					}
					
					//��ʾ������
					tvIP.setText(StrLocalIP);
					tvName.setText(StrName);
					String strSeat = "A1";
					
					//д��
					if(cmd == null){
						cmd = new tagCommandCode(StrLocalIP,strSeat,StrName,subIP);//��λ�ţ�����
					}else{
						cmd.strIP = StrLocalIP;
						cmd.strName = StrName;
						cmd.subIP = subIP;
						cmd.strSeat = strSeat;
					} 
					UserInfo mycmd = ((UserInfo) getApplicationContext());
					mycmd.getInstant(cmd);
					mycmd.setIP(ServerIP);
					//���λ
					initialed = true;
					connected = true;
					pbLandlight.setImageResource(R.drawable.green);//����Ϊ����
					break;
				case TIMEOUTCONNECTION://����
					//����������ʾ
					if (!initialed || connected) {
						unconnected();
						connected = false;
						}
					break;
				case CLEARRAISEHAND:
					Hand.setImageResource(R.drawable.hand_on);
					bHandup = false;
					break;
				case ENABLERAISEHAND:
					Hand.setEnabled(true);
					Hand.setImageResource(R.drawable.hand_on);
					bHandup = false;
					break;
				case DISRAISEHAND:
					Hand.setEnabled(false);
					Hand.setImageResource(R.drawable.hand_disable);
					bHandup = false;
					break;
				case SETVOLUME://��ʦ��������
					tagCommandCode t = new tagCommandCode(msg.getData().getByteArray("data"));
					seekBar.setProgress(t.iReserver[0]);
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,t.iReserver[0], 0);
					break;
				case NOTIFY:
					  byte[] Note = new byte[480];
					  System.arraycopy(msg.getData().getByteArray("data"), DATALONG-480, Note, 0, 480);
					AlertDialog.Builder NotifyDialog= new AlertDialog.Builder(InternetActivity.this);
		
					try {
						NotifyDialog.setTitle("֪ͨ").setMessage(new String(Note,"GBK"))
						.setCancelable(false)
						.setNegativeButton("�ر�", new DialogInterface.OnClickListener() {  
						       public void onClick(DialogInterface dialog, int id) {  
						            dialog.cancel();  
						       }  
						   }).create().show();
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					  break;
				case CLASSRESUME://�Ͽ�--�������ã�¼���ȴ����Ĭ�ϲ������Ͽ��û�
				case SELFSTUDYOFF://ȡ������ѧϰ-ͬ�Ͽ�
					Intent intent = new Intent().setClass(InternetActivity.this,
							ClassTeachActivity.class);
					intent.setData(Uri.parse("0"));
					InternetActivity.this.startActivity(intent);
					break;
				//---------------������Ϣ----------------------//
					
				}
			}
			};
    
	 public void initView(){
	    	webview = (WebView) findViewById(R.id.WEB); 
	        //����WebView���ԣ��ܹ�ִ��Javascript�ű� 
	        webview.getSettings().setJavaScriptEnabled(true);
	        //����flash����
	        webview.getSettings().setPluginsEnabled(true);
	        webview.getSettings().setPluginState(PluginState.ON);
	        //������Ҫ��ʾ����ҳ 
	        webview.loadUrl("http://www.baidu.com/"); 
	        //����Web��ͼ 
	        webview.setWebViewClient(new HelloWebViewClient ()); 
	        //����
	        webview.requestFocusFromTouch(); 
	        //ʹ����ҳ���ı����ܹ���������
	        webview.setOnTouchListener(new OnTouchListener(){
				public boolean onTouch(View v, MotionEvent event) 
				{
					// TODO Auto-generated method stub
					webview.requestFocus();
					return false;
				}
	        });
	        
			goButton=(Button)findViewById(R.id.gobutton);
			backButton=(Button)findViewById(R.id.backbutton);
			forwardButton=(Button)findViewById(R.id.forwardbutton);
			stopButton=(Button)findViewById(R.id.stopbutton);
			reloadButton=(Button)findViewById(R.id.reloadbutton);
			homeButton=(Button)findViewById(R.id.homebutton);
			openButton=(Button)findViewById(R.id.openbutton);
			
			goButton.setOnClickListener(this);
			backButton.setOnClickListener(this);
			forwardButton.setOnClickListener(this);
			stopButton.setOnClickListener(this);
			reloadButton.setOnClickListener(this);
			homeButton.setOnClickListener(this);
			openButton.setOnClickListener(this);				
			Log.i("widget", "ok");
			EXT=(EditText)findViewById(R.id.url);
			
			//�����س�
	        EXT.setOnKeyListener(onKey);
			BaseView();
		}
	 
		BroadcastRe BR = new BroadcastRe(myHandler);
		protected void onStart(){
			super.onStart();
			//ע��㲥������
			IntentFilter iFilter = new IntentFilter();
			iFilter.addAction("android.intent.action.COMMAND");
			registerReceiver(BR,iFilter);	
		}
		protected void onStop(){
			//ע���㲥������
			super.onStop();
			unregisterReceiver(BR);
		}
		
	//Web��ͼ 
	    private class HelloWebViewClient extends WebViewClient { 
	        @Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url) { 
	            view.loadUrl(url); 
	            return true; 
	        } 
	    }
	    
	    //�س�ת��ַ
	    OnKeyListener onKey=new OnKeyListener() {  
	   	 
			public boolean onKey(View v, int keyCode, KeyEvent event) {  
			  
			// TODO Auto-generated method stub  
			 System.out.println(keyCode); 
			if(keyCode == KeyEvent.KEYCODE_ENTER){  
			  
			InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			webview.loadUrl("http://"+EXT.getText().toString());
			  
				if(imm.isActive()){  
			  
					imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0 );  
			  
					}  
			  
				return true;  			  
				}  
			  
			return false;  
			  
			}  
			  
			};  
	    
	    //���ת����ťת��ַ
	    @Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) { 
	             webview.goBack();//goBack()��ʾ����WebView����һҳ�� 
	            return true; 
	        } 
	        return false;
		}
	    
	 public void onClick(View v) {
			// TODO Auto-generated method stub		
						if (v == goButton) {
							webview.loadUrl("http://"+EXT.getText().toString());					
						}
						else if (v == backButton) {
							webview.goBack();
						}
						else if (v == forwardButton) {
							webview.goForward();
						}
						else if (v == stopButton) {
							webview.stopLoading();
						}
						else if (v == reloadButton) {
							webview.reload();
						}
						else if (v == homeButton) {
							webview.loadUrl("http://www.baidu.com/");
						}
						else if (v == openButton) {
							 Intent intent= new Intent();        
							 intent.setAction("android.intent.action.VIEW");    
							 Uri content_url = Uri.parse("http://"+EXT.getText().toString());   
							 intent.setData(content_url);  
							 startActivity(intent);
						}			
					}
	 
	 public void BaseView(){
			Hand = (ImageButton)findViewById(R.id.hand);
			ClassingButton =(ImageButton)findViewById(R.id.classing);
			InternetingButton=(ImageButton)findViewById(R.id.interneting);
			VodingButton=(ImageButton)findViewById(R.id.voding);
			PlayingButton=(ImageButton)findViewById(R.id.playing);
			ReadingButton=(ImageButton)findViewById(R.id.reading);
			ChatingButton=(ImageButton)findViewById(R.id.chating);
			ChatroomButton=(ImageButton)findViewById(R.id.chating1);
			
			Hand.setOnClickListener(OnClickListener);
			ClassingButton.setOnClickListener(OnClickListener);
			InternetingButton.setOnClickListener(OnClickListener);
			VodingButton.setOnClickListener(OnClickListener);
			PlayingButton.setOnClickListener(OnClickListener);
			ReadingButton.setOnClickListener(OnClickListener);
			ChatingButton.setOnClickListener(OnClickListener);
			ChatroomButton.setOnClickListener(OnClickListener);

			//״̬��
			pbLandlight = (ImageView)findViewById(R.id.loginlight);
				if(connected){
					pbLandlight.setImageResource(R.drawable.green);
					}else {
						unconnected();
					}
			
			tvIP = (TextView)findViewById(R.id.ip);
			tvName = (TextView)findViewById(R.id.name);
			if(initialed){
				tvIP.setText(cmd.strIP);
				tvName.setText(cmd.strName);
			}
			tvSystemState=(TextView)findViewById(R.id.systemstate);
			tvSystemState.setText("��������");
			
			//��������
			seekBar = (SeekBar)findViewById(R.id.seekBar1);
			audioManager=(AudioManager)getSystemService(AUDIO_SERVICE);//��ȡ��������
	        int MaxSound=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//��ȡϵͳ�������ֵ
	        seekBar.setMax(MaxSound);//��������Bar�����ֵ����Ϊϵͳ�������ֵ
	        int currentSount=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//��ȡ��ǰ����
	        seekBar.setProgress(currentSount);//��������Bar�ĵ�ǰֵ����Ϊϵͳ������ǰֵ
		    seekBar.setOnSeekBarChangeListener(osbcl);
			
		}
			
}
