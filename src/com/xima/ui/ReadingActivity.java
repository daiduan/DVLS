package com.xima.ui;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.xima.datadef.UserInfo;
import com.xima.datadef.tagCommandCode;
import com.xima.network.BroadcastRe;

import android.R.integer;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.PluginState;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ReadingActivity extends BaseActivity{
	/*-------------------电子阅读----------------------*/
	//网页视图
	private WebView webview;

	
	//显示列表
	protected PopupWindow popupReadingWindow;
	protected ListView lvPopupWindow;
	

	//按钮
	private Button DisplayButton;
	private Button OpenfileButton;
	private Button PreviousButton;
	private Button NextButton;
	
	//文件列表
	List<String> urllist;  
    ArrayAdapter<String> adapter;//适配器
    ArrayList<String> titleList;
	private String txturl;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		Uri mUrl = this.getIntent().getData();
		//UPnPService服务未启动
//		if (dlnaService == null) {
//		//	dlnaServiceDelayedStart = true;
//		}
		//获得文件地址
		if (mUrl != null) {
			//设置当前文件播
			txturl = mUrl.toString();
			//验证是否存在相同的文件
			boolean bFileIn = false;
			if(!urllist.isEmpty()){
				bFileIn = urllist.contains(txturl);
			}
				
			if(!bFileIn){
				//添加至播放列表
				urllist.add(txturl);
				titleList.add(this.getIntent().getExtras().getString("title"));
				adapter.notifyDataSetChanged();
				UserInfo lds = ((UserInfo) getApplicationContext());
				lds.setTxtUrl(urllist);
				lds.setTxtTitle(titleList);
			}
			
			webview.loadUrl(txturl);
			
		}
		else {
			//非点播跳转
//			shortcutResume = true;
		}

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
//				//设置连接显示
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
				AlertDialog.Builder NotifyDialog= new AlertDialog.Builder(ReadingActivity.this);
	
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
					Intent intent = new Intent().setClass(ReadingActivity.this,
							ClassTeachActivity.class);
					intent.setData(Uri.parse("0"));
					ReadingActivity.this.startActivity(intent);
					break;
				//---------------自有消息----------------------//
					
				}
				
			}
			};
	// 初始化
		public void init() {
			super.init();
			initPopupReadingWnd();
		}
	public void initView(){
			setContentView(R.layout.reading);
//			Bundle bundle = this.getIntent().getExtras();
//	        txturl = this.getIntent().getData();;//当前播放文件的路径
	    	DisplayButton=(Button)findViewById(R.id.displaybutton);
	    	OpenfileButton=(Button)findViewById(R.id.openfilebutton);
	    	PreviousButton=(Button)findViewById(R.id.previousbutton);
	    	NextButton=(Button)findViewById(R.id.nextbutton);
	    	webview = (WebView) findViewById(R.id.readingWEB);
			
	    	DisplayButton.setOnClickListener(this);
	    	OpenfileButton.setOnClickListener(this);
	    	PreviousButton.setOnClickListener(this);
	    	NextButton.setOnClickListener(this);
	    	
	    	//通过WebView得到WebSettings对象 
	    	WebSettings mWebSettings = webview.getSettings(); 
	    	//设置网页默认编码 
	    	webview.getSettings().setDefaultTextEncodingName("GBK");   
	    	
	    	BaseView();
	}
	
	BroadcastRe BR = new BroadcastRe(myHandler);
	protected void onStart(){
		//注册广播接收器
		super.onStart();
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction("android.intent.action.COMMAND");
		registerReceiver(BR,iFilter);	
	}
	protected void onStop(){
		//注销广播接收器
		super.onStop();
		unregisterReceiver(BR);
	}
	    	//按钮监听器
	  public void onClick(View v) {
	    			// TODO Auto-generated method stub			
	        		System.out.println("bool!!!");
	        		if (v == DisplayButton) {
	    				//显示列表	
	        			if(popupReadingWindow.isShowing()){
	        				DisplayButton.setText("隐藏列表");
	        				popupReadingWindow.dismiss();
	    				}else
	    					//popupReadingWindow.showAsDropDown(DisplayButton,DisplayButton.getWidth(),-lvPopupWindow.getHeight());
	    			       popupReadingWindow.showAsDropDown(DisplayButton,0,0);     
	    			}
	    			else if (v == OpenfileButton) {
	    				//跳转至自主点播
	    				Intent intent = new Intent();
	    				intent.setClass(v.getContext(),BrowserActivity.class);
	    				startActivity(intent);
	    			}
	    			else if (v == PreviousButton) {
	    				int index = urllist.indexOf(txturl);
	    				if(index!=-1 && index!= 0){
	    					txturl = urllist.get(index-1);
	    					webview.loadUrl(txturl);
	    					Toast.makeText(getApplicationContext(), titleList.get(index),
	    							Toast.LENGTH_SHORT).show();
	    				}else {
	    					Toast.makeText(getApplicationContext(), "已经是第一篇",
	    							Toast.LENGTH_SHORT).show();
						}
	    			}
	    			else if (v == NextButton) {
	    				int index = urllist.indexOf(txturl);
	    				if(index!=-1 && index!= urllist.size()-1){
	    					txturl = urllist.get(index+1);
	    					webview.loadUrl(txturl);
	    					Toast.makeText(getApplicationContext(), titleList.get(index),
	    							Toast.LENGTH_SHORT).show();
	    					}else {
		    					Toast.makeText(getApplicationContext(), "已经是最后一篇",
		    							Toast.LENGTH_SHORT).show();
							}
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
			tvSystemState.setText("文本阅读");
			
			//音量控制
			seekBar = (SeekBar)findViewById(R.id.seekBar1);
			audioManager=(AudioManager)getSystemService(AUDIO_SERVICE);//获取音量服务
	        int MaxSound=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//获取系统音量最大值
	        seekBar.setMax(MaxSound);//音量控制Bar的最大值设置为系统音量最大值
	        int currentSount=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//获取当前音量
	        seekBar.setProgress(currentSount);//音量控制Bar的当前值设置为系统音量当前值
		    seekBar.setOnSeekBarChangeListener(osbcl);
		}
	 
	 public void initPopupReadingWnd(){
			// 创建用于显示阅读列表菜单的PopupWindow对象。
						View popReadingView = View.inflate(this, R.layout.item_play_list, null);
						popupReadingWindow = new PopupWindow(popReadingView, LayoutParams.WRAP_CONTENT,
								LayoutParams.WRAP_CONTENT);
						// 使PopupWindow可以获得焦点，以便可以通过轨迹球或上、下、左、右键来控制菜单项,设置透明背景色
						popupReadingWindow.setFocusable(true);
						popupReadingWindow.setOutsideTouchable(true);
						ColorDrawable dw = new ColorDrawable(0xb0000000);
						popupReadingWindow.setBackgroundDrawable(dw);
						
			//初始化阅读菜单中的列表ListView
						lvPopupWindow = (ListView) popReadingView.findViewById(R.id.playlistview);						
			//初始化数据
			UserInfo lds = ((UserInfo) getApplicationContext());
			urllist = lds.getTxtUrl();
			titleList = lds.getTxtTitle();			
			if(titleList == null){
					urllist = new ArrayList<String>();
					titleList=new ArrayList<String>();
			    	
			    }
			if(adapter== null)
			adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,titleList);
			//添加并且显示
			lvPopupWindow.setAdapter(adapter);
//	        //设置监听器，对点击进行监听
			lvPopupWindow.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position,
						long id){ 
					// TODO Auto-generated method stub
					popupReadingWindow.dismiss();
					//打开文件
					txturl = urllist.get(position);
					//显示文本
					webview.loadUrl(txturl);
					}
	        }
	        );
	        
		
	}

}
	
	
