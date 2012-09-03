package com.xima.ui;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import com.xima.datadef.UserInfo;
import com.xima.datadef.tagCommandCode;
import com.xima.datadef.tagStuList;
import com.xima.network.BroadcastRe;
import com.xima.utility.DataConverter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
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
import android.widget.AdapterView.OnItemClickListener;

/**
 *-------------------学习交流----------------------
 *
 */
public class ChatPeerActivity extends BaseChat {

	//语音
	Button btVoiceRequest;//请求语音
	Button btVoiceClear;//请求停止
	Button btVoiceOK;//同意请求
	Button btVoiceReject;//拒绝请求
	Button btVoiceCancle;//挂断
	/*
	 * 语音请求对象
	 *     状态                                             请求  停止  同意  拒绝  挂断
	 * 0 自己无请求，他人无请求0     1  0   0  0   0
	 *  1自己请求                             1     0  1   0  0   0
	 *  2他人请求                             2     0  0   1  1   0
	 *  3通话                                     3     0  0   0  0   1
	 */
	int VoiceState;//语音请求状态
	ArrayList<HashMap<String, Object>> mylist;  
	String CurrentReqMM = null;//当前请求对象，包含：1.自己请求对象，2.对方请求对象
	String CurrentReqIP = null;//当前请求IP，包含：1.自己请求对象，2.对方请求对象
	
	//IP地址列表
	ArrayList<String> IPs = new ArrayList<String>();
	ArrayList<String> Users = new ArrayList<String>();
	//对话对象
	String CurrentMM;//聊天对象
	String CurrentIP = null;//聊天IP
	boolean toPublic = false;//向全体广播
	Button PublicSwitch;//是否向全体说话
	TextView tvChatName;
	SimpleAdapter mSchedule;//适配器
	//文本提示
	SpannableString ss;
	protected GridView List;
	Button btRefresh;//刷新
	boolean NotReq = true;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chatpeer);//ERROR!需要修改！
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
				AlertDialog.Builder NotifyDialog= new AlertDialog.Builder(ChatPeerActivity.this);
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
				Intent intent = new Intent().setClass(ChatPeerActivity.this,
						ClassTeachActivity.class);
				intent.setData(Uri.parse("0"));
				ChatPeerActivity.this.startActivity(intent);
				break;
			//---------------自有消息----------------------//
				//聊天消息处理
			case MSGCHATPEER:
					byte[] bytes = msg.getData().getByteArray("data");
					byte[] chats = new byte[bytes.length-DATALONG];
					System.arraycopy(bytes,DATALONG,chats,0, bytes.length-DATALONG);
					String data =new String(chats);
					//data +="\n";
					add2InputWindow(data,false);
					break;
			case VOICEREQUEST:		
					VoiceRequest(msg.getData().getByteArray("data"));
				break;
			case REFUSHLIST_RETURN:
				LoadList(msg.getData().getByteArray("data"));
				break;
			}
		}
		};
		
		// 初始化视图
		public void initView() {
			super.initView();
			
			chat_send = (Button)findViewById(R.id.sendbt_p);
			chat_send.setOnClickListener(this);
			chat_emotion = (Button)findViewById(R.id.embt_p);
			chat_emotion.setOnClickListener(this);
			
			btVoiceRequest = (Button)findViewById(R.id.BtvoiceRequest_p);
			btVoiceRequest.setOnClickListener(this);
			
			btVoiceClear = (Button)findViewById(R.id.BtvoiceClear_p);
			btVoiceClear.setOnClickListener(this);
			
			btVoiceOK = (Button)findViewById(R.id.BtvoiceOK_p);
			btVoiceOK.setOnClickListener(this);
			
			btVoiceReject = (Button)findViewById(R.id.BtvoiceReject_p);
			btVoiceReject.setOnClickListener(this);
			
			btVoiceCancle = (Button)findViewById(R.id.BtvoiceCancle_p);
			btVoiceCancle.setOnClickListener(this);
			
			btRefresh = (Button)findViewById(R.id.refresh_p);
			btRefresh.setOnClickListener(this);
			
			PublicSwitch = (Button)findViewById(R.id.PublicSwitch);
			PublicSwitch.setOnClickListener(this);
			PublicSwitch.setText("对聊天对象说话");
			
			tvChatName = (TextView)findViewById(R.id.tvChatName);
			//初始化
			btVoiceRequest.setEnabled(true);
			btVoiceClear.setEnabled(false);
			btVoiceOK.setEnabled(false);
			btVoiceReject.setEnabled(false);
			btVoiceCancle.setEnabled(false);
			
			//Input
			ChatWindow = (TextView)findViewById(R.id.ChatWindow_p);
			ChatInput = (EditText)findViewById(R.id.ChatInput_p);
			sv = (ScrollView)findViewById(R.id.SV_p);//滚动条
			//监听回车键 
	        ChatInput.setOnKeyListener(new OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					if(keyCode == KeyEvent.KEYCODE_ENTER){
						if((CurrentIP == null) && (!toPublic)){
							ChatInput.setError("请选择聊天对象");
						}else{
							send();
						}
						
						return true;
					}else return false;
				}
	        });
			
	     			
			BaseView();
			initList();
		}
		
		BroadcastRe BR = new BroadcastRe(myHandler);
		protected void onStart(){
			//注册广播接收器
			super.onStart();
			IntentFilter iFilter = new IntentFilter();
			iFilter.addAction("android.intent.action.COMMAND");
			iFilter.addAction("android.intent.action.MSGCHATPEER");
			iFilter.addAction("android.intent.action.REFUSHLIST");
			registerReceiver(BR,iFilter);	
		}
		
		protected void onStop(){
			//注销广播接收器
			super.onStop();
			unregisterReceiver(BR);
		}	
	public void onClick(View v) {
		// TODO Auto-generated method stub
		/*请求信息自定义
		 * VOICEREQUEST
		 * 1.Reserve[0]=0  请求通话
		 * 2.Reserve[0]=1 拒绝通话
		 * 3.Reserve[0]=2 清除请求
		 * 3.Reserve[0]=3 挂断
		 * 4.Reserve[0]=4 同意通话
		 * cmd.iReserver[1]
		 * 告知他人自己的状态
		 */
		if(v.equals(btVoiceRequest)){//请求语音
			if(VoiceState == 0){
				if(CurrentMM == null){
					btVoiceRequest.setError("请选择一个聊天对象！");
				}else{//向当前聊天者请求语音
			    	VoiceState = 1;
			    	CurrentReqMM = CurrentMM;
					CurrentReqIP = CurrentIP;
					
					btVoiceRequest.setEnabled(false);
					btVoiceClear.setEnabled(true);
					btVoiceOK.setEnabled(false);
					btVoiceReject.setEnabled(false);
					btVoiceCancle.setEnabled(false);
					
					cmd.iwID = VOICEREQUEST;
					cmd.iReserver[0]=0;						
					us.SetIP(CurrentReqIP);
					us.SendMsg(cmd.toByteArray());
			    	
					ss = new SpannableString("系统提示：你正在请求与"+CurrentMM+"进行语音通话\n");
			    	EnterRoomSS(ss);
				}
			}
		}else if(v.equals(btVoiceClear)){//请求取消
			if(VoiceState ==1 && (CurrentReqMM!=null)){
				//发送给通话对象
				cmd.iwID = VOICEREQUEST;
				cmd.iReserver[0]=2;						
				us.SetIP(CurrentReqIP);
				us.SendMsg(cmd.toByteArray());
				
				ss = new SpannableString("系统提示：你已经停止与"+CurrentReqMM+"的通话请求\n");
		    	EnterRoomSS(ss);
				
		    	VoiceState = 0;
		    	CurrentReqMM = null;
				CurrentReqIP = null;
				
				btVoiceRequest.setEnabled(true);
				btVoiceClear.setEnabled(false);
				btVoiceOK.setEnabled(false);
				btVoiceReject.setEnabled(false);
				btVoiceCancle.setEnabled(false);
				
			}
		}else if(v.equals(btVoiceOK)){          //同意请求         ----开始通话
			if(VoiceState == 2 && CurrentReqMM != null){
					//向服务器请求语音
					cmd.iwID = SELFTALK;
					cmd.iReserver[0]=1;
					String cmdIP = cmd.strIP.substring(cmd.strIP.lastIndexOf(".")+1,cmd.strIP.length());
					cmd.iReserver[1] = Integer.parseInt(cmdIP);
					cmd.iReserver[2] =  Integer.parseInt(CurrentReqIP.substring(CurrentReqIP.lastIndexOf(".")+1,CurrentReqIP.codePointCount(0, CurrentReqIP.length())));
					us.SetIP(ServerIP);
					us.SendMsg(cmd.toByteArray());
					//发送给通话对象
					cmd.iwID = VOICEREQUEST;
					cmd.iReserver[0]=4;						
					us.SetIP(CurrentReqIP);
					us.SendMsg(cmd.toByteArray());
					//提示
					ss = new SpannableString("系统提示：你可以开始与"+CurrentReqMM+"的通话请求\n");
			    	EnterRoomSS(ss);
					//设置当前状态
					VoiceState = 3;//通话
					//设置按钮
					btVoiceReject.setEnabled(false);
					btVoiceRequest.setEnabled(false);
					btVoiceOK.setEnabled(false);
					btVoiceClear.setEnabled(false);
					btVoiceCancle.setEnabled(true);

			}
		}else if(v.equals(btVoiceReject)){//拒绝请求
			if(CurrentReqMM !=null && VoiceState == 2){
				//向请求者拒绝
				cmd.iwID = VOICEREQUEST;
				cmd.iReserver[0]=1;
				us.SetIP(CurrentReqIP);
				us.SendMsg(cmd.toByteArray());
				//提示
				ss = new SpannableString("系统提示：你已经拒绝了"+CurrentReqMM+"的通话请求\n");
		    	EnterRoomSS(ss);
				//设置当前状态
				VoiceState = 0;
		    	CurrentReqMM = null;
				CurrentReqIP = null;
				btVoiceRequest.setEnabled(true);
				btVoiceClear.setEnabled(false);
				btVoiceOK.setEnabled(false);
				btVoiceReject.setEnabled(false);
				btVoiceCancle.setEnabled(false);
				
			}
		}else if(v.equals(btVoiceCancle)){//挂断语音        ----停止通话
			if(VoiceState == 3 && CurrentReqMM!= null){
				//发送挂断至服务器
				cmd.iwID = SELFTALK;
				cmd.iReserver[0]=0;
				cmd.iReserver[1] = Integer.parseInt(cmd.strIP.substring(cmd.strIP.lastIndexOf(".")+1, cmd.strIP.length()));
				cmd.iReserver[2] =  Integer.parseInt(CurrentReqIP.substring(CurrentReqIP.lastIndexOf(".")+1,CurrentReqIP.length()));
				us.SetIP(ServerIP);
				us.SendMsg(cmd.toByteArray());
				//发送挂断至对象
				cmd.iwID = VOICEREQUEST;
				cmd.iReserver[0]=3;
				us.SetIP(CurrentReqIP);
				us.SendMsg(cmd.toByteArray());
				//提示
				ss = new SpannableString("系统提示：你已经挂断了与"+CurrentReqMM+"的通话\n");
		    	EnterRoomSS(ss);
				//设置当前状态
				VoiceState = 0;
		    	CurrentReqMM = null;
				CurrentReqIP = null;
				
				btVoiceRequest.setEnabled(true);
				btVoiceClear.setEnabled(false);
				btVoiceOK.setEnabled(false);
				btVoiceReject.setEnabled(false);
				btVoiceCancle.setEnabled(false);
			}
		}else if(v.equals(btRefresh)){
			//向服务器请求刷新列表
			if(connected){
				cmd.iwID =REFUSHLIST;
				us.SetIP(ServerIP);
				us.SendMsg(cmd.toByteArray());	
			}
		}else if(v.equals(chat_send)){
			//发送消息
			send();
		}else if(v.equals(chat_emotion)){
			//表情列表
			if(popup.isShowing()){
				popup.dismiss();
			}else
				popup.showAsDropDown(chat_emotion,chat_emotion.getWidth(),-gvPopupWindow.getHeight());
		}else if(v.equals(PublicSwitch)){
			if(toPublic){
				PublicSwitch.setText("对聊天对象说话");
				toPublic = false;
			}else {
				PublicSwitch.setText("对全体说话");
				toPublic = true;
			}
		}
	}
		
	
	//重写sendout！！
		public void sendout(Editable etb){
			//加入本机信息
			etb.insert(0, cmd.strName+"说：");
			String StrChat = FilterHtml((Html.toHtml(etb)));
			int StrLen = StrChat.length();//内容长度
			//命令
			cmd.SetCmdID(MSGCHATPEER);
			//将命令与聊天信息合并
			byte[] msg = new byte[DATALONG+StrLen];
			System.arraycopy(cmd.toByteArray(), 0, msg, 0, DATALONG);//命令
			System.arraycopy(StrChat.getBytes(), 0, msg, DATALONG, StrLen);//内容
			
			if(toPublic)
				for(int i=0;i!=CurrentIPNum;i++){
					if(!cmd.strIP.equals(IPs.get(i))){//不给自己发送消息
						us.SetIP((String) IPs.get(i));
						us.SendMsg(msg);
					}				
				}
			else {
				us.SetIP(CurrentIP);
				us.SendMsg(msg);}
		}
		
	public void initList(){
		
		 //添加并且显示
		mylist = new ArrayList<HashMap<String, Object>>();
		 //生成适配器，数组===》ListItem  使用item_room_list的布局
		mSchedule = new SimpleAdapter(this, //没什么解释  
		                              mylist,//数据来源   
		                              R.layout.item_room_list,//ListItem的XML实现  
		                              //动态数组与ListItem对应的子项          
		                              new String[] {"ItemImage", "ItemText"},   
		                              //ListItem的XML文件里面的ID  
		                              new int[] {R.id.ivRoom,R.id.tvRoom}); 
		 //初始化成员列表
		List = (GridView)findViewById(R.id.LV_p);
		List.setAdapter(mSchedule);
        //设置监听器，对点击进行监听
        List.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id){ 
				// TODO Auto-generated method stub
				if(!IPs.isEmpty()){
					//设置聊天对象
					CurrentMM = Users.get(position);
					CurrentIP = IPs.get(position);
					//系统提示
					ss = new SpannableString("系统提示：你选择了与"+CurrentMM +"聊天\n");
			    	EnterRoomSS(ss);
			    	//设置当前显示对话人姓名
			    	tvChatName.setText(CurrentMM);
					}
				}
        }
        );
	}
	public void LoadList(byte[] data){
		
		byte[] cmd_data = new byte[DATALONG];// 命令
		System.arraycopy(data, 0, cmd_data, 0, DATALONG);
		tagCommandCode TCmdData = new tagCommandCode(cmd_data);
		CurrentIPNum = TCmdData.iReserver[0];
		boolean MMNotIn = true;//当前对象是否在聊天室内
		
	if(TCmdData.idwCmdSize == CurrentIPNum*STULISTLONG)//验证数据
	{
		//清除数据
		mylist.clear();
		mSchedule.notifyDataSetChanged();
		IPs.clear();
		Users.clear();
		//生成动态数组，初始化数据  	   
	    for(int i=0;i<CurrentIPNum;i++)//根据学生数量添加
	    {
	    	byte[] demob = new byte[STULISTLONG];
			System.arraycopy(data, DATALONG + i * STULISTLONG,
					demob, 0, STULISTLONG);
			tagStuList stuinfo = new tagStuList(demob);//成员信息
			if( (cmd.subIP+stuinfo.IP).equals(CurrentIP)|| CurrentIP == null) MMNotIn = false;//列表中人尚在 
			if(!cmd.strIP.equals(cmd.subIP+stuinfo.IP)){//排除自己进入列表
				 HashMap<String, Object> map = new HashMap<String, Object>();
			     if(stuinfo.Sex == 0)map.put("ItemImage",R.drawable.homegreen);  //头像图片，判断男女,此时为男
			        else map.put("ItemImage",R.drawable.green); //女
			    	IPs.add(cmd.subIP+stuinfo.IP);//IP地址
					Users.add(stuinfo.StrName);
			        map.put("ItemText", stuinfo.StrName);  //学生姓名
			        mylist.add(map);  	
			}
	    }
	    CurrentIPNum = CurrentIPNum-1;
	    mSchedule.notifyDataSetChanged();

	}
	 if(MMNotIn){//房间聊天对象离开
	    	ss = new SpannableString("系统提示："+CurrentMM +"已经不在房间了！\n");
	    	EnterRoomSS(ss);
	    	CurrentMM = null;
	    	CurrentIP = null;
	    	tvChatName.setText(" ");
	    }
}
	
	public void VoiceRequest(byte[] data){
		/*请求信息自定义
		 * VOICEREQUEST
		 * 1.Reserve[0]=0  请求通话
		 * 2.Reserve[0]=1 拒绝通话
		 * 3.Reserve[0]=2 清除请求
		 * 3.Reserve[0]=3 挂断
		 * 4.Reserve[0]=4 同意通话
		 * cmd.iReserver[1]
		 * 告知他人自己的状态
		 */

		byte[] cmd_data = new byte[DATALONG];// 命令
		System.arraycopy(data, 0, cmd_data, 0, DATALONG);
		tagCommandCode TCmdData = new tagCommandCode(cmd_data);
		
		switch(TCmdData.iReserver[0]){
		case 0://请求通话
			if(VoiceState == 0){//等待状态
				VoiceState = 2;//change to 被请求状态
				CurrentReqMM = TCmdData.strName;
				CurrentReqIP = TCmdData.strIP;;
				
				btVoiceRequest.setEnabled(false);
				btVoiceClear.setEnabled(false);
				btVoiceOK.setEnabled(true);
				btVoiceReject.setEnabled(true);
				btVoiceCancle.setEnabled(false);
				
				ss = new SpannableString("系统提示："+CurrentReqMM+"请求与您语音对话！\n");
		    	EnterRoomSS(ss);	
			}else{//拒绝请求
				cmd.iwID = VOICEREQUEST;
				cmd.iReserver[0]=1;
				cmd.iReserver[1]=VoiceState;
				us.SetIP(TCmdData.strIP);
				us.SendMsg(cmd.toByteArray());
				}
			break;
		case 1://拒绝通话
			if(TCmdData.strName.equals(CurrentReqMM)){//当前请求对象拒绝了自己
				//清空请求对象和状态，并提示
				switch(cmd.iReserver[1]){
				case 1:
					ss = new SpannableString("系统提示："+CurrentReqMM+"正在请求与他人通话。\n");
					break;
				case 2:
					ss = new SpannableString("系统提示："+CurrentReqMM+"已经接到他人通话请求。\n");
					break;
				case 3:
					ss = new SpannableString("系统提示："+CurrentReqMM+"正在于他人语音通话。\n");
					break;
				default:
					ss = new SpannableString("系统提示："+CurrentReqMM+"拒绝了您的通话请求。\n");
					break;
				}
		    	EnterRoomSS(ss);
		    	CurrentReqMM = null;
				CurrentReqIP = null;
				VoiceState = 0;//change to 等待状态
				btVoiceRequest.setEnabled(true);
				btVoiceClear.setEnabled(false);
				btVoiceOK.setEnabled(false);
				btVoiceReject.setEnabled(false);
				btVoiceCancle.setEnabled(false);
				}
			break;
		case 2://清除请求
			//清空请求对象和状态，并提示
			if(TCmdData.strName.equals(CurrentReqMM)){
				ss = new SpannableString("系统提示："+CurrentReqMM+"取消了与您的通话请求。\n");
				EnterRoomSS(ss);
				CurrentReqMM = null;
				CurrentReqIP = null;
				VoiceState = 0;//change to 等待状态
				
				btVoiceRequest.setEnabled(true);
				btVoiceClear.setEnabled(false);
				btVoiceOK.setEnabled(false);
				btVoiceReject.setEnabled(false);
				btVoiceCancle.setEnabled(false);
				
				
			}
			
			break;
		case 3://挂断
			if(TCmdData.strName.equals(CurrentReqMM) && VoiceState == 3){
				ss = new SpannableString("系统提示："+CurrentReqMM+"挂断了与您的通话。\n");
				EnterRoomSS(ss);
				
				CurrentReqMM = null;
				CurrentReqIP = null;
				VoiceState = 0;//change to 等待状态
				
				btVoiceRequest.setEnabled(true);
				btVoiceClear.setEnabled(false);
				btVoiceOK.setEnabled(false);
				btVoiceReject.setEnabled(false);
				btVoiceCancle.setEnabled(false);
				
				
			}
		case 4://请求对象发回同意通话
			if(TCmdData.strName.equals(CurrentReqMM)&& VoiceState == 1){
				ss = new SpannableString("系统提示："+CurrentReqMM+"同意了您的通话请求。\n");
				EnterRoomSS(ss);
				
				VoiceState = 3;//change to 通话状态
				btVoiceRequest.setEnabled(false);
				btVoiceClear.setEnabled(false);
				btVoiceOK.setEnabled(false);
				btVoiceReject.setEnabled(false);
				btVoiceCancle.setEnabled(true);
				
				
				
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
		tvSystemState.setText("学习交流");
		
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
