package com.xima.ui;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import android.R.integer;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
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

import com.xima.adapter.ContentListAdapter;
import com.xima.adapter.ImageAdapter;
import com.xima.datadef.Item;
import com.xima.datadef.UserInfo;
import com.xima.datadef.tagCommandCode;
import com.xima.datadef.tagCommandStream;
import com.xima.datadef.tagStuList;
import com.xima.network.BroadcastRe;
import com.xima.network.UDPService;
import com.xima.network.UPnPService;
import com.xima.utility.DataConverter;

public class ClassTeachActivity extends BaseActivity {
	/*-------------------上课----------------------*/
	
	TextView tvState;//上课状态
	TextView tvRecord;//录音状态
	TextView tvFollowed;//跟读状态
	
	boolean bClass = false;//当前为上课状态
	//录音列表
	protected PopupWindow popupRecordListWindow;
	protected ListView lvPopupRecordListWindow;
	
	//录音
	protected PopupWindow popupRecordWindow;
	
	//自由讨论
	protected PopupWindow popupStuWindow;
	protected Button OKButtonStu;
	protected Button CancleButton;
	protected TextView tvStuInfo;
	public SimpleAdapter stAdapter;
	ArrayList<HashMap<String, Object>> stlist;
	protected GridView gvStu;
	ScrollView sView;
	//按钮	
	private ImageButton FollowreadButton;
	private ImageButton RecordButton;
	private ImageButton RecordListButton;
	private ImageButton RecordMenuButton;
	//private ImageButton DICButton;
	private Button pauseRecordButton;
	private Button stopRecordButton;

	protected boolean bRecording;
	protected boolean bFollowed;
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
	// -------------------------------------------------------------------
	// 本机信息
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.classteach);
		init();
		

	}
	
	// -------------------------------------------------------------------
	/* 通过Handler来接收消息进程所传递的信息 */
	private Handler myHandler = new Handler() {
		@Override
		public synchronized void handleMessage(Message msg) {
			// TODO Auto-generated method stub

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
				if (!popup.isShowing() || !popupStuWindow.isShowing() || !popupRecordListWindow.isShowing() || !popupRecordWindow.isShowing()) {
				boolean state = true;
				switch(new tagCommandStream(msg.getData().getByteArray("data")).toCode().iReserver[80]){
				case -1://正常状态
					break;
				case 0:
					state = true;
					tvState.setText("待命");
					break;
				case 1:
					state = true;
					tvState.setText("广播音频");
					break;
				case 4:
					state = true;
					tvState.setText("全通话");
					break;
				case 14:
					state = false;
					tvState.setText("自主学习");
				case 15:
					state = false;
					tvState.setText("下课");//???
					break;
				}
				StateChange(state);
				}
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
//				if (!initialed || connected) {
					StateChange(false);
					unconnected();
					connected = false;
					tvState.setText("待命");
//					}
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
				AlertDialog.Builder NotifyDialog= new AlertDialog.Builder(ClassTeachActivity.this);
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
				StateChange(true);
				break;
			case CLASSOVER://下课--同自主学习
			case SELFSTUDYON://自主学习-全部允许，掉线也是自主学习
				StateChange(false);
				break;
			case WARNING:
				WarnDialog.show();
				break;
			case LOCK:
				LockDialog.show();
				break;
			case UNLOCK:
				LockDialog.cancel();
				break;
			//---------------自有消息----------------------//
			case BROADCASTSOUNDCARD:// 广播音频
				tvState.setText("广播音频");
				break;
			case BROADCASTMIC:// 全通话
				tvState.setText("全通话");
				break;
			case TALKTOONE:// 个别通话
				tvState.setText("个别通话");
				break;
			case DEMONSTRATE://示范学生列表
				ListFill(msg.getData().getByteArray("data"));
				break;
			case STOPTALKTOONE:
			case STOPBROADCASTMIC:
			case CLOSEBROADCASTAUDIO:
				boolean state1 = true;
				switch(new tagCommandStream(msg.getData().getByteArray("data")).toCode().iReserver[80]){
				case -1://正常状态
					break;
				case 0:
					state1 = true;
					tvState.setText("待命");
					break;
				case 1:
					state1 = true;
					tvState.setText("广播音频");
					break;
				case 4:
					state1 = true;
					tvState.setText("全通话");
					break;
				case 14:
					state1 = false;
					tvState.setText("自主学习");
					break;
				case 15:
					state1 = false;
					tvState.setText("下课");//???
					break;
				}
				StateChange(state1);
				break;
			case STOPDEMONSTRATE:
				popup.dismiss();
				break;
			case SPEAKFALSE:
				SpannableString ss = new SpannableString("系统提示：自由讨论组已满.");
				ss.setSpan(new ForegroundColorSpan(Color.RED), 0, ss.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				tvStuInfo.append(ss);
				sView.pageScroll(View.FOCUS_DOWN);
				break;
			case SPEAKOK://加入讨论
				loadpopupSt(msg.getData().getByteArray("data"),false);
				break;
			case SPEAKCANCELOK://退出讨论
				loadpopupSt(msg.getData().getByteArray("data"), true);
				break;
			case SPEAKON://开始分组讨论
				tvState.setText("自由讨论");
				if (!popupStuWindow.isShowing())
				popupStuWindow.showAtLocation(findViewById(R.id.background_center_layout), 
						Gravity.TOP|Gravity.LEFT, 50, 0);
				Log.i("show", "speakon");
				break;
			case SPEAKOFF:
				stlist.clear();
				stAdapter.notifyDataSetChanged();
				tvStuInfo.setText("");
				OKButtonStu.setEnabled(true);
				CancleButton.setEnabled(false);
				if (popupStuWindow.isShowing())
				popupStuWindow.dismiss();
				break;
			default:
				break;
			}
		}
	};
	//初始化
    public void init(){
    	super.init();
    	initpopupRecordWindow();
    	initPopupRecordListWnd();
    	initpopupStuWindow();
    	initPopupST();
    }
    public void initView(){
    	bRecording = false;
    	bFollowed = false;
		
		FollowreadButton=(ImageButton)findViewById(R.id.followread);
		RecordButton=(ImageButton)findViewById(R.id.record);
		RecordListButton=(ImageButton)findViewById(R.id.recordlist);
		RecordMenuButton=(ImageButton)findViewById(R.id.recordmenu);
		//DICButton=(ImageButton)findViewById(R.id.DIC);			
		
		FollowreadButton.setOnClickListener(this);
		RecordButton.setOnClickListener(this);
		RecordListButton.setOnClickListener(this);
		RecordMenuButton.setOnClickListener(this);
		//DICButton.setOnClickListener(this);

		tvState = (TextView)findViewById(R.id.state);
	    tvRecord= (TextView)findViewById(R.id.tvrecord);
	    tvFollowed= (TextView)findViewById(R.id.tvfollowed);
		BaseView();		
		StateChange(true);
		Log.i("initView", "ok");
	}

	public void initNetwork() {
		// 初始化UDP Service
		Intent intent = new Intent();
		intent.setClass(ClassTeachActivity.this,UDPService.class);
		this.startService(intent);
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
		iFilter.addAction("android.intent.action.DEMONSTRATE");
		iFilter.addAction("android.intent.action.DEF");
		registerReceiver(BR,iFilter);	
	}
	protected void onStop(){
		//注销广播接收器
		super.onStop();
		unregisterReceiver(BR);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
//		Uri classnow = this.getIntent().getData();
//		if(classnow != null){
//			StateChange(true);
//		}
	}
	
	private void ListFill(byte[] data){
		byte[] cmd_data = new byte[DATALONG];// 命令
		System.arraycopy(data, 0, cmd_data, 0, DATALONG);
		tagCommandCode TCmdData = new tagCommandCode(cmd_data);
		int SNum = TCmdData.iReserver[0];
		boolean includeMe =false;
		if(SNum*DEMOINFOLONG == data.length - DATALONG){
			//清除列表
			mylist.clear();
			//添加示范列表
			for (int i = 0; i != SNum; i++) {
				byte[] demob = new byte[DEMOINFOLONG];
				System.arraycopy(data, DATALONG + i *DEMOINFOLONG,
						demob, 0, DEMOINFOLONG);
				String DStuName = DataConverter.trimString(demob);
				if(cmd.strName.equals(DStuName)) includeMe=true;
				mylist.add(DStuName);
				}
			stuAdapter.notifyDataSetChanged();
			//显示提示
			if(includeMe){
				tvState.setText("示范;被示范");
			}else {
				tvState.setText("示范");
			}
			//显示
			if(!popup.isShowing()){
				popup.showAtLocation(findViewById(R.id.background_center_layout), 
						Gravity.TOP|Gravity.LEFT, 50, 0);;
			}
			
		}
		
	}
	//按钮监听器
    public  void onClick(View v) {
			// TODO Auto-generated method stub
    		if (v == FollowreadButton) {
    			if (connected) {
    				if(bFollowed){
    					tvFollowed.setText("跟读开（麦克风开）");
    					//跟读开始命令
    					cmd.iReserver[0] = 1;
    					cmd.SetCmdID(STFOLLOWREAD);
    					us.SendMsg(cmd.toByteArray());
    					bFollowed = false;
    					}
    				else {
    					tvFollowed.setText("跟读关（麦克风关）");
    					//跟读停止命令
    					cmd.iReserver[0] = 0;
    					cmd.SetCmdID(STFOLLOWREAD);
    					us.SendMsg(cmd.toByteArray());
    					bFollowed = true;}
				}
			}
			else if (v == RecordButton) {
				//录音开始命令
				if(popupRecordWindow.isShowing()){
					tvRecord.setText("录音停止");
					//录音停止命令
					if(connected){
						cmd.iReserver[0] = 0;
						cmd.SetCmdID(STURECORD);
						us.SendMsg(cmd.toByteArray());
						bRecording = false; 
					}
					popupRecordWindow.dismiss();
				}else{
					if(connected){
						tvRecord.setText("录音开始");
						pauseRecordButton.setText("暂停");
						popupRecordWindow.showAsDropDown(RecordButton, 0, 0);
						cmd.iReserver[0] = 1;
						cmd.SetCmdID(STURECORD);
						us.SendMsg(cmd.toByteArray());
					}
				}		
			}
			else if (v == RecordListButton) {
				//显示列表	
				if(popupRecordListWindow.isShowing()){
    				popupRecordListWindow.dismiss();
				}else{
					if (dlnaService != null) {
						dlnaService.clearStack();
						scrollStack.clear();
						List<Item> items = dlnaService.getItems("0");
						if(items!=null){
							if (!items.isEmpty()) {
								for (int i = 0; i < items.size(); i++) {
									if ("AudioRecord".equals(items.get(i).getTitle())) {
										List<Item> items2 = dlnaService.getItems(items.get(i).getId());
										for (int j = 0; j < items2.size(); j++) {
											if (cmd.strIP.equals(items2.get(j).getTitle())) {
												getList(items2.get(j).getId());
												break;
											}
										}
										break;}
									}
							}
						}
					}		
				popupRecordListWindow.showAtLocation(findViewById(R.id.background_center_layout), Gravity.LEFT|Gravity.TOP, 0,0); 
				}	    			
			}
			else if (v == RecordMenuButton) {
				//暂不处理
			}
//			else if (v == DICButton) {
//				//英汉互译，暂不处理。
//			}
    		
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
			pbLandlight.setImageResource(R.anim.un_connected);
			pbLandlight.post(new Runnable() {
			    @Override
			    public void run() {
			        AnimationDrawable frameAnimation = (AnimationDrawable) pbLandlight.getDrawable();
			        frameAnimation.start();
			    }
			});
		}
		
		tvIP = (TextView)findViewById(R.id.ip);
		tvName = (TextView)findViewById(R.id.name);
		if(initialed){
			tvIP.setText(cmd.strIP);
			tvName.setText(cmd.strName);
		}
		tvSystemState=(TextView)findViewById(R.id.systemstate);
		tvSystemState.setText("上课");
		
		//音量控制
				seekBar = (SeekBar)findViewById(R.id.seekBar1);
				audioManager=(AudioManager)getSystemService(AUDIO_SERVICE);//获取音量服务
		        int MaxSound=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//获取系统音量最大值
		        seekBar.setMax(MaxSound);//音量控制Bar的最大值设置为系统音量最大值
		        int currentSount=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//获取当前音量
		        seekBar.setProgress(currentSount);//音量控制Bar的当前值设置为系统音量当前值
			    seekBar.setOnSeekBarChangeListener(osbcl);
	}
	
	protected PopupWindow popup;//示范列表
	protected ListView lvPopupWindow;// 网格
	private ArrayAdapter<String> stuAdapter;//学生数据
	ArrayList<String> mylist;
	
	public void initpopupStuWindow() {
		View popView = View.inflate(this, R.layout.stu_list_popup, null);
		popup = new PopupWindow(popView, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		// 使PopupWindow可以获得焦点，以便可以通过轨迹球或上、下、左、右键来控制菜单项,设置透明背景色
		popup.setFocusable(true);
		popup.setOutsideTouchable(true);
//		ColorDrawable dw = new ColorDrawable(0xb0000000);
//		popup.setBackgroundDrawable(dw);
		//初始化菜单中的图表Listview
		lvPopupWindow = (ListView) popView.findViewById(R.id.stulistview);
		//初始化mylist
		mylist = new ArrayList<String>();
		 //生成适配器，数组===》ListItem  使用item_stu_list的布局
		stuAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,mylist); 
		// 绑定Adapter对象
		lvPopupWindow.setAdapter(stuAdapter);
	}
	
	public void initpopupRecordWindow(){		
		// 创建用于显示录音菜单的PopupWindow对象。
		View myView= (ViewGroup)getLayoutInflater().inflate(R.layout.record, null);
		//后两个参数表示popupWindow的宽，高
		popupRecordWindow = new PopupWindow(myView,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);		
		popupRecordWindow.setFocusable(true);
		popupRecordWindow.setOutsideTouchable(true);
//		ColorDrawable dw = new ColorDrawable(0xb0000000);
//		popupRecordWindow.setBackgroundDrawable(dw);
		pauseRecordButton =(Button)myView.findViewById(R.id.recordbutton1);
		stopRecordButton =(Button)myView.findViewById(R.id.recordbutton2);

		pauseRecordButton.setOnClickListener(new OnClickListener() {                   
            public void onClick(View v) {   
			    // TODO Auto-generated method stub
            	if(connected){
            		if(bRecording){
    					pauseRecordButton.setText("恢复");
    					//录音开始命令
    					cmd.iReserver[0] = 3;
    					cmd.SetCmdID(STURECORD);
    					Log.i("sendbt", cmd.toString());
    					us.SendMsg(cmd.toByteArray());
    					Log.i("sendbt", cmd.toString());
    					bRecording = false;	
    				}
    			else {
    				pauseRecordButton.setText("暂停");
    				//录音停止命令 
    					cmd.iReserver[0] = 2;
    					cmd.SetCmdID(STURECORD);
    					us.SendMsg(cmd.toByteArray());
    					bRecording = true;
            	}
				}
            }
			        }); 
		stopRecordButton.setOnClickListener(new OnClickListener() {                   
            public void onClick(View v) {   
			    // TODO Auto-generated method stub  
            	tvRecord.setText("录音停止");
				//录音停止命令
            	if(connected){
				cmd.iReserver[0] = 0;
				cmd.SetCmdID(STURECORD);
				us.SendMsg(cmd.toByteArray());
				bRecording = false; 
				}popupRecordWindow.dismiss();
            	}}
            	);
				
	}
	
	public void initPopupRecordListWnd(){
		// 创建用于显示录音列表菜单的PopupWindow对象。
					View popRecordListView = View.inflate(this, R.layout.record_list, null);
					popupRecordListWindow = new PopupWindow(popRecordListView, LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					// 使PopupWindow可以获得焦点，以便可以通过轨迹球或上、下、左、右键来控制菜单项,设置透明背景色
					popupRecordListWindow.setFocusable(true);
					popupRecordListWindow.setOutsideTouchable(true);
					ColorDrawable dw = new ColorDrawable(0xb0000000);
					popupRecordListWindow.setBackgroundDrawable(dw);

					//初始化录音菜单中的列表ListView
					lvPopupRecordListWindow = (ListView) popRecordListView.findViewById(R.id.recordlistview);
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

		lvPopupRecordListWindow.setAdapter(new ContentListAdapter(this, items));
		lvPopupRecordListWindow.setTextFilterEnabled(true);
		lvPopupRecordListWindow.setOnItemClickListener(new OnItemClickListener() {
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
					Intent intent = new Intent().setClass(ClassTeachActivity.this,
							PlayerActivity.class);
					intent.setData(Uri.parse(item.getRes()));
					intent.putExtra("title", item.getTitle());
					ClassTeachActivity.this.startActivity(intent);
				} else if ("object.item.videoItem".equals(item.getObjectClass())) {
					Intent intent = new Intent().setClass(ClassTeachActivity.this,
							PlayerActivity.class);
					intent.setData(Uri.parse(item.getRes()));
					intent.putExtra("title", item.getTitle());
					ClassTeachActivity.this.startActivity(intent);
				} else if("object.item.textItem".equals(item.getObjectClass())){
					Intent intent = new Intent().setClass(ClassTeachActivity.this,
							ReadingActivity.class);
					intent.setData(Uri.parse(item.getRes()));
					intent.putExtra("title", item.getTitle());
					ClassTeachActivity.this.startActivity(intent);
				}else {
					getList(item.getId());
				}
			}
		});
	}
	//上课状态转换
	public void StateChange(boolean ClassNow){
		if(ClassNow){
			//右侧按钮置灰
			ClassingButton.setBackgroundResource(R.drawable.classing4);
			InternetingButton.setBackgroundResource(R.drawable.interneting4);
			VodingButton.setBackgroundResource(R.drawable.voding4);
			PlayingButton.setBackgroundResource(R.drawable.playing4);
			ReadingButton.setBackgroundResource(R.drawable.reading4);
			ChatingButton.setBackgroundResource(R.drawable.chating4);
			ChatroomButton.setBackgroundResource(R.drawable.chatroom4);
			//右侧按钮禁用
			ClassingButton.setEnabled(false);
			InternetingButton.setEnabled(false);
			VodingButton.setEnabled(false);
			PlayingButton.setEnabled(false);
			ReadingButton.setEnabled(false);
			ChatingButton.setEnabled(false);
			ChatroomButton.setEnabled(false);
			//录音功能置灰
//			FollowreadButton.setBackgroundDrawable(R.drawable.);
//			RecordButton.setBackgroundDrawable(R.drawable.);
//			RecordListButton.setBackgroundDrawable(R.drawable.);
//			RecordMenuButton.setBackgroundDrawable(R.drawable.);
			FollowreadButton.setEnabled(false);
			RecordButton.setEnabled(false);
			RecordListButton.setEnabled(false);
			RecordMenuButton.setEnabled(false);
		}else{
			//右侧按钮置灰&禁用
			ClassingButton.setBackgroundResource(R.drawable.classing_style);
			InternetingButton.setBackgroundResource(R.drawable.interneting_style);
			VodingButton.setBackgroundResource(R.drawable.voding_style);
			PlayingButton.setBackgroundResource(R.drawable.playing_style);
			ReadingButton.setBackgroundResource(R.drawable.reading_style);
			ChatingButton.setBackgroundResource(R.drawable.chating_style);
			ChatroomButton.setBackgroundResource(R.drawable.chatroom_style);
			//右侧按钮禁用
			ClassingButton.setEnabled(true);
			InternetingButton.setEnabled(true);
			VodingButton.setEnabled(true);
			PlayingButton.setEnabled(true);
			ReadingButton.setEnabled(true);
			ChatingButton.setEnabled(true);
			ChatroomButton.setEnabled(true);
			//录音功能置灰
//			FollowreadButton.setBackgroundDrawable(R.drawable.);
//			RecordButton.setBackgroundDrawable(R.drawable.);
//			RecordListButton.setBackgroundDrawable(R.drawable.);
//			RecordMenuButton.setBackgroundDrawable(R.drawable.);
			FollowreadButton.setEnabled(true);
			RecordButton.setEnabled(true);
			RecordListButton.setEnabled(true);
			RecordMenuButton.setEnabled(true);
		}
	}

	public void initPopupST(){
		View popStuView = View.inflate(this, R.layout.selftalkpop, null);
		popupStuWindow = new PopupWindow(popStuView, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		// 使PopupWindow可以获得焦点，以便可以通过轨迹球或上、下、左、右键来控制菜单项,设置透明背景色
		popupStuWindow.setFocusable(true);
		popupStuWindow.setOutsideTouchable(true);
//		ColorDrawable dw = new ColorDrawable(0xb0000000);
//		popupStuWindow.setBackgroundDrawable(dw);
		
		//初始化自由讨论菜单中的列表GridView
		 //添加并且显示
		stlist = new ArrayList<HashMap<String, Object>>();
		 //生成适配器，数组===》ListItem  使用item_room_list的布局
		stAdapter = new SimpleAdapter(this, //没什么解释  
		                              stlist,//数据来源   
		                              R.layout.item_stu_list,//ListItem的XML实现  
		                              //动态数组与ListItem对应的子项          
		                              new String[] {"ItemImage", "ItemText"},   
		                              //ListItem的XML文件里面的ID  
		                              new int[] {R.id.ivPicTou,R.id.tvNameStu}); 
		 //初始化GridView
		gvStu = (GridView)popStuView.findViewById(R.id.stugridview);
		gvStu.setAdapter(stAdapter);
		//初始化TextView
		tvStuInfo = (TextView)popStuView.findViewById(R.id.tvStuinfo);
		//初始化Button
		OKButtonStu = (Button)popStuView.findViewById(R.id.stuOK);
		OKButtonStu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (connected) {
					cmd.SetCmdID(SPEAK);
					us.SendMsg(cmd.toByteArray());
				}
			}
		});
		CancleButton = (Button)popStuView.findViewById(R.id.stuCancle);
		CancleButton.setEnabled(false);
		CancleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (connected) {
					cmd.SetCmdID(SPEAKCANCEL);
					us.SendMsg(cmd.toByteArray());
					CancleButton.setEnabled(false);
				}
				
				OKButtonStu.setEnabled(true);
			}
		});
		//初始化scrollview
		sView = (ScrollView)popStuView.findViewById(R.id.SVstu);
	}
	
	protected void loadpopupSt(byte[] data,boolean quit){
		byte[] cmd_data = new byte[DATALONG];// 命令
		System.arraycopy(data, 0, cmd_data, 0, DATALONG);
		tagCommandCode TCmdData = new tagCommandCode(cmd_data);
		int Num = TCmdData.iReserver[0];
		if (Num*STULISTLONG == data.length - DATALONG)
		{	
			stlist.clear();
			stAdapter.notifyDataSetChanged();
			for (int i = 0; i < Num; i++) {
				byte[] demob = new byte[STULISTLONG];
				System.arraycopy(data, DATALONG + i * STULISTLONG,demob, 0, STULISTLONG);
				tagStuList stuinfo = new tagStuList(demob);//成员信息
				HashMap<String, Object> map = new HashMap<String, Object>();
			    if(stuinfo.Sex == 0)map.put("ItemImage",R.drawable.homegreen);  //头像图片，判断男女,此时为男
			        else map.put("ItemImage",R.drawable.green); //女
			    map.put("ItemText", stuinfo.StrName);  //学生姓名
			    stlist.add(map);  	
			}
		}
		if (quit) {
			if (cmd.strIP.equals(TCmdData.strIP)) {
				SpannableString ss = new SpannableString("你已经退出自由讨论！\n");
				ss.setSpan(new ForegroundColorSpan(Color.RED), 0, ss.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				tvStuInfo.append(ss);
				sView.pageScroll(View.FOCUS_DOWN);
				CancleButton.setEnabled(false);
				OKButtonStu.setEnabled(true);
			}else {
				SpannableString ss = new SpannableString(TCmdData.strName+"退出了自由讨论\n");
				ss.setSpan(new ForegroundColorSpan(Color.RED), 0, ss.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				tvStuInfo.append(ss);
				sView.pageScroll(View.FOCUS_DOWN);
			}
		}else {
			if (cmd.strIP.equals(TCmdData.strIP)) {
				SpannableString ss = new SpannableString("自由讨论申请成功！你可以发言了\n");
				ss.setSpan(new ForegroundColorSpan(Color.RED), 0, ss.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				tvStuInfo.append(ss);
				sView.pageScroll(View.FOCUS_DOWN);
				CancleButton.setEnabled(true);
				OKButtonStu.setEnabled(false);
			}else {
				SpannableString ss = new SpannableString(TCmdData.strName+"已经加入自由讨论\n");
				ss.setSpan(new ForegroundColorSpan(Color.RED), 0, ss.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				tvStuInfo.append(ss);
				sView.pageScroll(View.FOCUS_DOWN);
			}
		}
		stAdapter.notifyDataSetChanged();
		if (!popupStuWindow.isShowing()) 
		popupStuWindow.showAtLocation(findViewById(R.id.background_center_layout), 
				Gravity.TOP|Gravity.LEFT, 50, 0);
	}
	
	public void checkpop(){
		if (popup.isShowing()) {
			popup.dismiss();
		}
		if (popupRecordListWindow.isShowing()) {
			popupRecordListWindow.dismiss();
		}
		if (popupRecordWindow.isShowing()) {
			popupRecordWindow.dismiss();
		}
		if (popupStuWindow.isShowing()) {
			popupStuWindow.dismiss();
		}
		if (LockDialog.isShowing()) {
			LockDialog.dismiss();
		}
	}
}