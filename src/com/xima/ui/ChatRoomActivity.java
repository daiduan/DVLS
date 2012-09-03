package com.xima.ui;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import android.R.integer;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.xima.datadef.UserInfo;
import com.xima.datadef.tagCommandCode;
import com.xima.datadef.tagStuList;
import com.xima.network.BroadcastRe;

/**
 *-------------------聊天室----------------------
 *
 */
public class ChatRoomActivity extends BaseChat{
	
	byte[] msg_data = new byte[DATALONG];// 命令
	// 聊天	
	protected int CurrentRoomNum = -1;// 当前房间号
	protected String chat_msg;// 新消息
	//IP地址列表
	ArrayList<String> IPs = new ArrayList<String>();

	protected TextView tvMember;
	int preRoom = -1;
	protected GridView RoomList;
	//语音
	Button btVoiceOpen;//语音开关
	Button btVoiceCls;
	boolean VoiceOn = false;//语音开关
	//退出房间
	Button quitBT;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chatroom);
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
				AlertDialog.Builder NotifyDialog= new AlertDialog.Builder(ChatRoomActivity.this);
	
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
				Intent intent = new Intent().setClass(ChatRoomActivity.this,
						ClassTeachActivity.class);
				intent.setData(Uri.parse("0"));
				ChatRoomActivity.this.startActivity(intent);
				break;
			//---------------自有消息----------------------//
			case GETROOM:
				getRoomList(msg.getData().getByteArray("data"));
				break;
			// ------------------------------------------------------------
			//聊天消息处理
			case MSGCHATROOM:
//				int r = msg.getData().getInt("roomnum");
//				if(r == CurrentRoomNum){
				byte[] bytes = msg.getData().getByteArray("data");
				byte[] chats = new byte[bytes.length-DATALONG];
				System.arraycopy(bytes,DATALONG,chats,0, bytes.length-DATALONG);
				String data =new String(chats);
				add2InputWindow(data,false);
//				}
				break;
			}
			
		}
	};
	
	// 初始化视图
	public void initView() {
		//Button
		super.initView();
		chat_send = (Button)findViewById(R.id.sendbt);
		chat_send.setOnClickListener(this);
		chat_emotion = (Button)findViewById(R.id.embt);
		chat_emotion.setOnClickListener(this);
		btVoiceOpen = (Button)findViewById(R.id.BtvoiceOpen);
		btVoiceOpen.setOnClickListener(this);
		btVoiceCls = (Button)findViewById(R.id.BtvoiceCls);
		btVoiceCls.setOnClickListener(this);
		btVoiceCls.setEnabled(false);
		quitBT=(Button)findViewById(R.id.quitBT);
		quitBT.setOnClickListener(this);
		tvMember = (TextView)findViewById(R.id.tvInfo);
		//Input
		ChatWindow = (TextView)findViewById(R.id.ChatWindow);
		ChatInput = (EditText)findViewById(R.id.ChatInput);
		sv = (ScrollView)findViewById(R.id.SV);//滚动条
		//监听回车键 
        ChatInput.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if(keyCode == KeyEvent.KEYCODE_ENTER){
					send();
					return true;
				}else return false;
			}
        });
        
        BaseView();
		initRoomList();
	}
	
	//左侧房间列表
	private void initRoomList(){
		//初始化房间列表
		RoomList = (GridView)findViewById(R.id.RoomListView);
		//生成动态数组，初始化数据  
	    ArrayList<HashMap<String, Object>> mylist = new ArrayList<HashMap<String, Object>>();  
	    
	    for(int i=0;i<10;i++)  //因为GridView的child有限制为12，所以不能超过这个值，待以后解决
	    {  
	        HashMap<String, Object> map = new HashMap<String, Object>();  
	        map.put("ItemImage",R.drawable.homegreen);  
	        map.put("ItemText", "房间"+(i+1));  //1-10，木有0哈
	        mylist.add(map);  
	    }
	    
	    //生成适配器，数组===》ListItem  
	   SimpleAdapter mSchedule = new SimpleAdapter(this, //没什么解释  
	                                                mylist,//数据来源   
	                                                R.layout.item_room_list,//ListItem的XML实现  
	                                                //动态数组与ListItem对应的子项          
	                                                new String[] {"ItemImage", "ItemText"},   
	                                                //ListItem的XML文件里面的ID  
	                                                new int[] {R.id.ivRoom,R.id.tvRoom});  
	    //添加并且显示
        RoomList.setAdapter(mSchedule);
        //设置监听器，对点击进行监听
        RoomList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id){ 
				// TODO Auto-generated method stub
				if(connected)//只有在网有IP才可以进入房间
				if(CurrentRoomNum != position){
					//新进入房间
					/*
					 * Reserve[0]代表消息类型：
					 * 1:发出加入请求 2：发出退出请求  3:刷新   4:开始说话  5:停止说话
					 * 
					 * Reserve[1]代表房间号:1~15 Reserve[119]代表学生性别
					 * 
					 */	
					//改变选中项图片
					ImageView iv;
					//-----------退出旧房间----------------
					if(preRoom != -1){
						//改变旧房间颜色
						iv = (ImageView)RoomList.getChildAt(CurrentRoomNum).findViewById(R.id.ivRoom);
						iv.setImageResource(R.drawable.homegreen);
						//设置退出命令
						cmd.iReserver[0] = 2;
						//设置退出房间号
						cmd.iReserver[1]=CurrentRoomNum;
						//发送至服务器
						cmd.SetCmdID(SENDROOM);
						us.SetIP(ServerIP);
						us.SendMsg(cmd.toByteArray());
					}
					
					//-----------加入新房间-------------------
					//改变新房间颜色
					iv = (ImageView)view.findViewById(R.id.ivRoom);
					iv.setImageResource(R.drawable.homered);
					//保存旧有房间
					if(CurrentRoomNum == -1){
						preRoom = position;
					}else preRoom = CurrentRoomNum;
					//保存当前房间号
					CurrentRoomNum = position;
					//设置加入命令
					cmd.iReserver[0]= 1;
					//设置选中房间号
					cmd.iReserver[1]= position;
					//发送至服务器
					cmd.SetCmdID(SENDROOM);
					us.SetIP(ServerIP);
					us.SendMsg(cmd.toByteArray());
				}
				
			}
        }
        );
	}
	
	BroadcastRe BR = new BroadcastRe(myHandler);
	protected void onStart(){
		//注册广播接收器
		super.onStart();
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction("android.intent.action.COMMAND");
		iFilter.addAction("android.intent.action.GETROOM");
		iFilter.addAction("android.intent.action.MSGCHATROOM");
		registerReceiver(BR,iFilter);	
	}
	
	protected void onStop(){
		//注销广播接收器
		super.onStop();
		unregisterReceiver(BR);
	}	
	
	//重写sendout！！
	public void sendout(Editable etb){
		//加入本机信息
		etb.insert(0,cmd.strName+"说：");
		String StrChat = FilterHtml((Html.toHtml(etb)));
		int StrLen = StrChat.length();//内容长度
		//命令
		cmd.SetCmdID(MSGCHATROOM);
		//将命令与聊天信息合并
		byte[] msg = new byte[DATALONG+StrLen];
		System.arraycopy(cmd.toByteArray(), 0, msg, 0, DATALONG);//命令
		System.arraycopy(StrChat.getBytes(), 0, msg, DATALONG, StrLen);//内容
		
		for(int i=0;i!=CurrentIPNum;i++){
			if(!cmd.strIP.equals(IPs.get(i))){//不给自己发送消息
				String IP = IPs.get(i);
				us.SetIP(IP);
				us.SendMsg(msg);
			}	
		}
	}
	
	//--------------------------------------------------------------------------------
	//GetRoom
	public void getRoomList(byte[] data){
		byte[] cmd_data = new byte[DATALONG];// 命令
		System.arraycopy(data, 0, cmd_data, 0, DATALONG);
		tagCommandCode TCmdData = new tagCommandCode(cmd_data);
		
		SpannableString ss;//提醒信息
		/*
		 * command.Reserve[3];//命令类型返回(1加入 、2退出  、3说话  、4不说话)
		 * command.Reserve[2];//成员数
		 * command.Reserve[1];//房间号
		 * command.Reserve[0]=3;//刷新
		 * command.Reserve[0]=6;//成功
		 */
		
		if(TCmdData.iReserver[0] == 3 && TCmdData.iReserver[1] == CurrentRoomNum){
			boolean ListRe =true; 
			//服务器刷新房间列表
			if(!TCmdData.strIP.equals(cmd.strIP))
				switch(TCmdData.iReserver[3]){
				case 2://某同学退出房间
					ss = new SpannableString("系统提示："+TCmdData.strName+"退出了本房间\n");
					EnterRoomSS(ss);
					break;
				case 1://某同学加入房间
					ss = new SpannableString("系统提示："+TCmdData.strName+"进入了本房间\n");
					EnterRoomSS(ss);
					break;
				case 4://某同学开始说话
					ss = new SpannableString("系统提示："+TCmdData.strName+"开始发言\n");
					EnterRoomSS(ss);
					ListRe = false;
					break;
				case 5://某同学停止说话
					ss = new SpannableString("系统提示："+TCmdData.strName+"停止发言\n");
					EnterRoomSS(ss);
					ListRe = false;
					break;
			}
	
			if(ListRe){
				CurrentIPNum = TCmdData.iReserver[2];
				IPs.clear();
				tvMember.setText("");
				for (int i = 0; i != CurrentIPNum ; i++) {
					byte[] demob = new byte[STULISTLONG];
					System.arraycopy(data, DATALONG + i * STULISTLONG,
							demob, 0, STULISTLONG);
					IPs.add(cmd.subIP+new tagStuList(demob).IP);//IP地址
					SpannableString s = new SpannableString(new tagStuList(demob).StrName+"  ");
					tvMember.append(s);
				}
			}	
		}else if(TCmdData.iReserver[0] == 6) {//成功命令
			if(cmd.strIP.equals(TCmdData.strIP)){
				switch(TCmdData.iReserver[3]){
			case 0:
				break;
			case 1://服务器同意自己加入某个房间
				//保存当前房间号
				//CurrentRoomNum = TCmdData.iReserver[1];
				//提示进入房间
				ss = new SpannableString("系统提示：你进入了房间"+String.valueOf(CurrentRoomNum+1)+"\n");
				EnterRoomSS(ss);
				break;
			case 3://服务器同意自己退出房间
				//保存旧房间号
				//preRoom = CurrentRoomNum;
				ss = new SpannableString("系统提示：你退出了房间"+String.valueOf(TCmdData.iReserver[1]+1)+"\n");
				EnterRoomSS(ss);
				break;
			case 4://同意说话
				ss = new SpannableString("系统提示：你可以开始发言了"+"\n");
				EnterRoomSS(ss);
				break;
			case 5://停止说话
				ss = new SpannableString("系统提示：你已经退出发言了"+"\n");
				EnterRoomSS(ss);
				break;}
			}else{
				switch(TCmdData.iReserver[3]){
			case 0:
				break;
			case 4://同意说话
				ss = new SpannableString("系统提示："+TCmdData.strName+"开始发言了\n");
				EnterRoomSS(ss);
				break;
			case 5://停止说话
				ss = new SpannableString("系统提示："+TCmdData.strName+"退出发言了\n");
				EnterRoomSS(ss);
				break;
			}
			}
		}			
	}
	
	public void onClick(View v) {
		// TODO Auto-generated method stub
			if (v == chat_send) {
				send();//发送消息
			}else if(v == chat_emotion){
				if(popup.isShowing()){
					popup.dismiss();
				}else
					popup.showAsDropDown(chat_emotion,chat_emotion.getWidth(),-gvPopupWindow.getHeight());
			}else if(v.equals(btVoiceOpen)){
				if (connected) {
				//开始说话命令
				cmd.iReserver[0] = 4;
				//设置房间号
				cmd.iReserver[1]=CurrentRoomNum;
				//发送至服务器
				cmd.SetCmdID(SENDROOM);
				us.SetIP(ServerIP);
				us.SendMsg(cmd.toByteArray());
				btVoiceCls.setEnabled(true);
				btVoiceOpen.setEnabled(false);				
				}
			}else if(v.equals(btVoiceCls)){
				if (CurrentRoomNum!= -1) {
					//设置停止命令
					cmd.iReserver[0] = 5;
					//设置退出房间号
					cmd.iReserver[1]=CurrentRoomNum;
					//发送至服务器
					cmd.SetCmdID(SENDROOM);
					us.SetIP(ServerIP);
					us.SendMsg(cmd.toByteArray());
					btVoiceCls.setEnabled(false);
					btVoiceOpen.setEnabled(true);
				}
			}else if(v.equals(quitBT)){
				//设置退出命令
				cmd.iReserver[0] = 2;
				//设置退出房间号
				cmd.iReserver[1]=CurrentRoomNum;
				//发送至服务器
				cmd.SetCmdID(SENDROOM);
				us.SetIP(ServerIP);
				us.SendMsg(cmd.toByteArray());
				CurrentRoomNum = -1;
				preRoom = -1;
				CurrentIPNum = -1;
				IPs.clear();
				
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
		tvSystemState.setText("聊天室");
		
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
