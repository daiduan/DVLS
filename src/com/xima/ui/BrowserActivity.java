package com.xima.ui;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Stack;

import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.device.DeviceChangeListener;

import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.xima.datadef.UserInfo;
import com.xima.datadef.tagCommandCode;
import com.xima.network.BroadcastRe;
import com.xima.network.UPnPService;

import com.xima.adapter.ContentListAdapter;
import com.xima.datadef.Item;

/**
 *------------------自主点播----------------------
 *
 */
public class BrowserActivity extends BaseActivity {

	//private ListView deviceListView;
	private ListView contentListView;
	private Button refresh;
	private Button backtop;
	private boolean delayedGetList = false;
	private Stack<Integer> scrollStack = new Stack<Integer>();
	private UPnPService dlnaService;
	/**
	 * ServiceConnection
	 * 服务绑定
	 */
	private ServiceConnection dlnaServiceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName name, IBinder service) {
			dlnaService = ((UPnPService.DlnaServiceBinder) service)
					.getService();
			if (delayedGetList) {
				scrollStack.clear();
				getList("0");
			}
		}

		public void onServiceDisconnected(ComponentName name) {
			dlnaService = null;
		}

	};
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser);
		init();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (dlnaService != null) {
			if (dlnaService.getStack().isEmpty()) {
				scrollStack.clear();
				getList("0");
			} else {
				getList(dlnaService.getStack().peek());
				if (!scrollStack.isEmpty()) {
					contentListView.scrollTo(0, scrollStack.peek());
				}
			}
		} else {
			//首次启动
			delayedGetList = true;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
//		if (upnpService != null) {
//			upnpService.getRegistry()
//					.removeListener(deviceListRegistryListener);
//		}
//		getApplicationContext().unbindService(serviceConnection);
	}
	
	// -------------------------------------------------------------------
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
//						//设置连接显示
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
						AlertDialog.Builder NotifyDialog= new AlertDialog.Builder(BrowserActivity.this);
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
						Intent intent = new Intent().setClass(BrowserActivity.this,
						ClassTeachActivity.class);
						intent.setData(Uri.parse("0"));
						BrowserActivity.this.startActivity(intent);
						break;
			//---------------自有消息----------------------//
				
			}
			
		}
		};
		
		// 初始化视图
		public void initView() {
			contentListView = (ListView) findViewById(R.id.contentList);
			refresh = (Button)findViewById(R.id.refreshdir);
			refresh.setOnClickListener(this);
			backtop = (Button)findViewById(R.id.backtop);
			backtop.setOnClickListener(this);
			BaseView();
		}
		
		public void initNetwork() {
			super.initNetwork();
			//启动查询服务
			startService(new Intent(UPnPService.SEARCH_DEVICES));
			Intent service = new Intent(UPnPService.BIND_SERVICE);
			this.getApplicationContext().bindService(service,
					dlnaServiceConnection, Context.BIND_AUTO_CREATE);
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
		
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.refreshdir:
			if (dlnaService != null) {
				if (dlnaService.getStack().isEmpty()) {
					scrollStack.clear();
					getList("0");
				} else {
					dlnaService.clearStack();
					scrollStack.clear();
					getList("0");
//					getList(dlnaService.getStack().peek());
//					if (!scrollStack.isEmpty()) {
//						contentListView.scrollTo(0, scrollStack.peek());
//					}
				}
			} else {
				//首次启动
				delayedGetList = true;
			}
			break;
		case R.id.backtop:
			onBackPressed();
			break;
		default:
			break;
		}
	}
	
	/**
	 * getList 获取列表并显示
	 * @param id
	 */
		private void getList(String id) {
			
			List<Item> items = dlnaService.getItems(id);
			if (items != null) {
				showItems(items);
			}

		}

	/**
	 * showItems 显示列表
	 * @param items
	 */
	private void showItems(List<Item> items) {

		contentListView.setAdapter(new ContentListAdapter(this, items));
		contentListView.setTextFilterEnabled(true);
		contentListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Item item = (Item) parent.getItemAtPosition(position);
				String title = item.getTitle();
				Toast.makeText(getApplicationContext(), title,
						Toast.LENGTH_SHORT).show();
				scrollStack.push(position);
				String obj =  item.getObjectClass();
				Log.i("ObjectName===>",obj);
				//音频或视频跳转至PlayerActivity
				if ("object.item.audioItem.musicTrack".equals(item
						.getObjectClass())) {
					Intent intent = new Intent().setClass(BrowserActivity.this,
							PlayerActivity.class);
					intent.setData(Uri.parse(item.getRes()));
					intent.putExtra("title", item.getTitle());
					BrowserActivity.this.startActivity(intent);
				} else if ("object.item.videoItem".equals(item.getObjectClass())) {
					Intent intent = new Intent().setClass(BrowserActivity.this,
							PlayerActivity.class);
					intent.setData(Uri.parse(item.getRes()));
					intent.putExtra("title", item.getTitle());
					BrowserActivity.this.startActivity(intent);
				} else if("object.item.textItem".equals(item.getObjectClass())){
					Intent intent = new Intent().setClass(BrowserActivity.this,
							ReadingActivity.class);
					intent.setData(Uri.parse(item.getRes()));
					intent.putExtra("title", item.getTitle());
					BrowserActivity.this.startActivity(intent);
				}else {
					getList(item.getId());
				}

			}
		});
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		if (!dlnaService.getStack().isEmpty()
				&& dlnaService.getStack().size() > 1) {
			String currentLevel = dlnaService.getStack().peek();
			if (currentLevel != "0") {
				dlnaService.moveUp();
				String newLevel = dlnaService.getStack().peek();
				getList(newLevel);
				Integer pop = scrollStack.pop();
				contentListView.setSelection(pop);
			}
		} else {
			super.onBackPressed();
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
		tvSystemState.setText("自主点播");
		
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
