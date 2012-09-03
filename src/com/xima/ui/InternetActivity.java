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
	/*-------------------上网浏览----------------------*/
	//按钮
	private Button goButton;
	private Button backButton;
	private Button forwardButton;
	private Button stopButton;
	private Button reloadButton;
	private Button homeButton;
	private Button openButton;
	//地址栏
	private EditText EXT;	
	//网页视图
	private WebView webview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.interneting);
		init();
		
	}
	// -------------------------------------------------------------------
		/* 通过Handler来接收消息进程所传递的信息 */
		private Handler myHandler = new Handler() {
			@Override
			public synchronized void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				// ----------------------------------------------
				switch(msg.what){
				//--------------通用消息-------------------------//
				case TEACHEREXIST:// 教师端存在，如果没有被初始化，向教师端请求信息
					//设置连接状态
					if(!initialed){//未初始化，请求初始化信息
							//设置教师端IP
							if(ServerIP == null){
								ServerIP = msg.getData().getString("ServerIP");
							}
							us.SetIP(ServerIP);
							//发送学生信息请求
							tagCommandCode tcmd = new tagCommandCode(" "," "," ");//否则为null！！！
							tcmd.SetCmdID(GETSTUINFO);
							us.SendMsg(tcmd.toByteArray());
							Log.i("ActivityInfo---Login===>", "GETSTUINFO");
							tcmd.SetCmdID(LOGIN);// 在线ID
							us.SendMsg(tcmd.toByteArray());// 发送消息
							Log.i("ActivityInfo---Login===>", "Not Initialed");
					}else {
						if(!connected){//发生改变时才操作
							connected = true;
							pbLandlight.setImageResource(R.drawable.green);//设置为在线
							Log.i("LandLight========>","Online!!!");
							}
						if(ServerIP == null){
							ServerIP = msg.getData().getString("ServerIP");
						}
						cmd.SetCmdID(GETSTUINFO);
						us.SendMsg(cmd.toByteArray());
						Log.i("ActivityInfo---Login===>", "GETSTUINFO");
						us.SetIP(ServerIP);
						cmd.SetCmdID(LOGIN);// 在线ID
						us.SendMsg(cmd.toByteArray());// 发送消息
						Log.i("ActivityInfo---Login===>", "Connected & Initialed");
					}
					break;
				case ACCEPT://与教师端连接，初始化系统参数
				case GETSTUINFO_RETURN://获得学生信息
					Log.i("ActivityInfo---ACCEPT===>", "Initialed!");
					//初始化命令
					tagCommandCode tcmd = new tagCommandCode(msg.getData().getByteArray("data"));
					//初始化信息
					String StrLocalIP = getLocalIpAddress();//本地IP
					String subIP = StrLocalIP.substring(0, StrLocalIP.lastIndexOf(".")+1);//网段
					
					String StrName; 
					if(tcmd.strName!= null){
						StrName  = tcmd.strName;//学生姓名
					}else StrName ="STU"+StrLocalIP.substring(StrLocalIP.lastIndexOf("."),StrLocalIP.length());
					
					if(ServerIP==null){
						ServerIP = msg.getData().getString("ServerIP");
					}
					
					//显示到界面
					tvIP.setText(StrLocalIP);
					tvName.setText(StrName);
					String strSeat = "A1";
					
					//写入
					if(cmd == null){
						cmd = new tagCommandCode(StrLocalIP,strSeat,StrName,subIP);//座位号！！！
					}else{
						cmd.strIP = StrLocalIP;
						cmd.strName = StrName;
						cmd.subIP = subIP;
						cmd.strSeat = strSeat;
					} 
					UserInfo mycmd = ((UserInfo) getApplicationContext());
					mycmd.getInstant(cmd);
					mycmd.setIP(ServerIP);
					//标记位
					initialed = true;
					connected = true;
					pbLandlight.setImageResource(R.drawable.green);//设置为在线
					break;
				case TIMEOUTCONNECTION://掉线
					//设置连接显示
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
				case SETVOLUME://老师调节音量
					tagCommandCode t = new tagCommandCode(msg.getData().getByteArray("data"));
					seekBar.setProgress(t.iReserver[0]);
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,t.iReserver[0], 0);
					break;
				case NOTIFY:
					  byte[] Note = new byte[480];
					  System.arraycopy(msg.getData().getByteArray("data"), DATALONG-480, Note, 0, 480);
					AlertDialog.Builder NotifyDialog= new AlertDialog.Builder(InternetActivity.this);
		
					try {
						NotifyDialog.setTitle("通知").setMessage(new String(Note,"GBK"))
						.setCancelable(false)
						.setNegativeButton("关闭", new DialogInterface.OnClickListener() {  
						       public void onClick(DialogInterface dialog, int id) {  
						            dialog.cancel();  
						       }  
						   }).create().show();
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					  break;
				case CLASSRESUME://上课--跟读可用，录音等待命令，默认不允许，上课置灰
				case SELFSTUDYOFF://取消自助学习-同上课
					Intent intent = new Intent().setClass(InternetActivity.this,
							ClassTeachActivity.class);
					intent.setData(Uri.parse("0"));
					InternetActivity.this.startActivity(intent);
					break;
				//---------------自有消息----------------------//
					
				}
			}
			};
    
	 public void initView(){
	    	webview = (WebView) findViewById(R.id.WEB); 
	        //设置WebView属性，能够执行Javascript脚本 
	        webview.getSettings().setJavaScriptEnabled(true);
	        //设置flash可用
	        webview.getSettings().setPluginsEnabled(true);
	        webview.getSettings().setPluginState(PluginState.ON);
	        //加载需要显示的网页 
	        webview.loadUrl("http://www.baidu.com/"); 
	        //设置Web视图 
	        webview.setWebViewClient(new HelloWebViewClient ()); 
	        //焦点
	        webview.requestFocusFromTouch(); 
	        //使得网页中文本框能够正常输入
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
			
			//监听回车
	        EXT.setOnKeyListener(onKey);
			BaseView();
		}
	 
		BroadcastRe BR = new BroadcastRe(myHandler);
		protected void onStart(){
			super.onStart();
			//注册广播接收器
			IntentFilter iFilter = new IntentFilter();
			iFilter.addAction("android.intent.action.COMMAND");
			registerReceiver(BR,iFilter);	
		}
		protected void onStop(){
			//注销广播接收器
			super.onStop();
			unregisterReceiver(BR);
		}
		
	//Web视图 
	    private class HelloWebViewClient extends WebViewClient { 
	        @Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url) { 
	            view.loadUrl(url); 
	            return true; 
	        } 
	    }
	    
	    //回车转地址
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
	    
	    //点击转到按钮转地址
	    @Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) { 
	             webview.goBack();//goBack()表示返回WebView的上一页面 
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

			//状态灯
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
			tvSystemState.setText("上网冲浪");
			
			//音量控制
			seekBar = (SeekBar)findViewById(R.id.seekBar1);
			audioManager=(AudioManager)getSystemService(AUDIO_SERVICE);//获取音量服务
	        int MaxSound=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//获取系统音量最大值
	        seekBar.setMax(MaxSound);//音量控制Bar的最大值设置为系统音量最大值
	        int currentSount=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//获取当前音量
	        seekBar.setProgress(currentSount);//音量控制Bar的当前值设置为系统音量当前值
		    seekBar.setOnSeekBarChangeListener(osbcl);
			
		}
			
}
