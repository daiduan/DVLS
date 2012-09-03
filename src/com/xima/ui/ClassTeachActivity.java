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
	/*-------------------�Ͽ�----------------------*/
	
	TextView tvState;//�Ͽ�״̬
	TextView tvRecord;//¼��״̬
	TextView tvFollowed;//����״̬
	
	boolean bClass = false;//��ǰΪ�Ͽ�״̬
	//¼���б�
	protected PopupWindow popupRecordListWindow;
	protected ListView lvPopupRecordListWindow;
	
	//¼��
	protected PopupWindow popupRecordWindow;
	
	//��������
	protected PopupWindow popupStuWindow;
	protected Button OKButtonStu;
	protected Button CancleButton;
	protected TextView tvStuInfo;
	public SimpleAdapter stAdapter;
	ArrayList<HashMap<String, Object>> stlist;
	protected GridView gvStu;
	ScrollView sView;
	//��ť	
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
	 * �����
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
	// ������Ϣ
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.classteach);
		init();
		

	}
	
	// -------------------------------------------------------------------
	/* ͨ��Handler��������Ϣ���������ݵ���Ϣ */
	private Handler myHandler = new Handler() {
		@Override
		public synchronized void handleMessage(Message msg) {
			// TODO Auto-generated method stub

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
				if (!popup.isShowing() || !popupStuWindow.isShowing() || !popupRecordListWindow.isShowing() || !popupRecordWindow.isShowing()) {
				boolean state = true;
				switch(new tagCommandStream(msg.getData().getByteArray("data")).toCode().iReserver[80]){
				case -1://����״̬
					break;
				case 0:
					state = true;
					tvState.setText("����");
					break;
				case 1:
					state = true;
					tvState.setText("�㲥��Ƶ");
					break;
				case 4:
					state = true;
					tvState.setText("ȫͨ��");
					break;
				case 14:
					state = false;
					tvState.setText("����ѧϰ");
				case 15:
					state = false;
					tvState.setText("�¿�");//???
					break;
				}
				StateChange(state);
				}
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
//				//����������ʾ
//				if (!initialed || connected) {
					StateChange(false);
					unconnected();
					connected = false;
					tvState.setText("����");
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
			case SETVOLUME://��ʦ��������
				tagCommandCode t = new tagCommandCode(msg.getData().getByteArray("data"));
				seekBar.setProgress(t.iReserver[0]);
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,t.iReserver[0], 0);
				break;
			case NOTIFY:
				  byte[] Note = new byte[480];
				  System.arraycopy(msg.getData().getByteArray("data"), DATALONG-480, Note, 0, 480);
				AlertDialog.Builder NotifyDialog= new AlertDialog.Builder(ClassTeachActivity.this);
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
				StateChange(true);
				break;
			case CLASSOVER://�¿�--ͬ����ѧϰ
			case SELFSTUDYON://����ѧϰ-ȫ����������Ҳ������ѧϰ
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
			//---------------������Ϣ----------------------//
			case BROADCASTSOUNDCARD:// �㲥��Ƶ
				tvState.setText("�㲥��Ƶ");
				break;
			case BROADCASTMIC:// ȫͨ��
				tvState.setText("ȫͨ��");
				break;
			case TALKTOONE:// ����ͨ��
				tvState.setText("����ͨ��");
				break;
			case DEMONSTRATE://ʾ��ѧ���б�
				ListFill(msg.getData().getByteArray("data"));
				break;
			case STOPTALKTOONE:
			case STOPBROADCASTMIC:
			case CLOSEBROADCASTAUDIO:
				boolean state1 = true;
				switch(new tagCommandStream(msg.getData().getByteArray("data")).toCode().iReserver[80]){
				case -1://����״̬
					break;
				case 0:
					state1 = true;
					tvState.setText("����");
					break;
				case 1:
					state1 = true;
					tvState.setText("�㲥��Ƶ");
					break;
				case 4:
					state1 = true;
					tvState.setText("ȫͨ��");
					break;
				case 14:
					state1 = false;
					tvState.setText("����ѧϰ");
					break;
				case 15:
					state1 = false;
					tvState.setText("�¿�");//???
					break;
				}
				StateChange(state1);
				break;
			case STOPDEMONSTRATE:
				popup.dismiss();
				break;
			case SPEAKFALSE:
				SpannableString ss = new SpannableString("ϵͳ��ʾ����������������.");
				ss.setSpan(new ForegroundColorSpan(Color.RED), 0, ss.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				tvStuInfo.append(ss);
				sView.pageScroll(View.FOCUS_DOWN);
				break;
			case SPEAKOK://��������
				loadpopupSt(msg.getData().getByteArray("data"),false);
				break;
			case SPEAKCANCELOK://�˳�����
				loadpopupSt(msg.getData().getByteArray("data"), true);
				break;
			case SPEAKON://��ʼ��������
				tvState.setText("��������");
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
	//��ʼ��
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
		// ��ʼ��UDP Service
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
		//ע��㲥������
		super.onStart();
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction("android.intent.action.COMMAND");
		iFilter.addAction("android.intent.action.DEMONSTRATE");
		iFilter.addAction("android.intent.action.DEF");
		registerReceiver(BR,iFilter);	
	}
	protected void onStop(){
		//ע���㲥������
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
		byte[] cmd_data = new byte[DATALONG];// ����
		System.arraycopy(data, 0, cmd_data, 0, DATALONG);
		tagCommandCode TCmdData = new tagCommandCode(cmd_data);
		int SNum = TCmdData.iReserver[0];
		boolean includeMe =false;
		if(SNum*DEMOINFOLONG == data.length - DATALONG){
			//����б�
			mylist.clear();
			//���ʾ���б�
			for (int i = 0; i != SNum; i++) {
				byte[] demob = new byte[DEMOINFOLONG];
				System.arraycopy(data, DATALONG + i *DEMOINFOLONG,
						demob, 0, DEMOINFOLONG);
				String DStuName = DataConverter.trimString(demob);
				if(cmd.strName.equals(DStuName)) includeMe=true;
				mylist.add(DStuName);
				}
			stuAdapter.notifyDataSetChanged();
			//��ʾ��ʾ
			if(includeMe){
				tvState.setText("ʾ��;��ʾ��");
			}else {
				tvState.setText("ʾ��");
			}
			//��ʾ
			if(!popup.isShowing()){
				popup.showAtLocation(findViewById(R.id.background_center_layout), 
						Gravity.TOP|Gravity.LEFT, 50, 0);;
			}
			
		}
		
	}
	//��ť������
    public  void onClick(View v) {
			// TODO Auto-generated method stub
    		if (v == FollowreadButton) {
    			if (connected) {
    				if(bFollowed){
    					tvFollowed.setText("����������˷翪��");
    					//������ʼ����
    					cmd.iReserver[0] = 1;
    					cmd.SetCmdID(STFOLLOWREAD);
    					us.SendMsg(cmd.toByteArray());
    					bFollowed = false;
    					}
    				else {
    					tvFollowed.setText("�����أ���˷�أ�");
    					//����ֹͣ����
    					cmd.iReserver[0] = 0;
    					cmd.SetCmdID(STFOLLOWREAD);
    					us.SendMsg(cmd.toByteArray());
    					bFollowed = true;}
				}
			}
			else if (v == RecordButton) {
				//¼����ʼ����
				if(popupRecordWindow.isShowing()){
					tvRecord.setText("¼��ֹͣ");
					//¼��ֹͣ����
					if(connected){
						cmd.iReserver[0] = 0;
						cmd.SetCmdID(STURECORD);
						us.SendMsg(cmd.toByteArray());
						bRecording = false; 
					}
					popupRecordWindow.dismiss();
				}else{
					if(connected){
						tvRecord.setText("¼����ʼ");
						pauseRecordButton.setText("��ͣ");
						popupRecordWindow.showAsDropDown(RecordButton, 0, 0);
						cmd.iReserver[0] = 1;
						cmd.SetCmdID(STURECORD);
						us.SendMsg(cmd.toByteArray());
					}
				}		
			}
			else if (v == RecordListButton) {
				//��ʾ�б�	
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
				//�ݲ�����
			}
//			else if (v == DICButton) {
//				//Ӣ�����룬�ݲ�����
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
		//״̬��
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
		tvSystemState.setText("�Ͽ�");
		
		//��������
				seekBar = (SeekBar)findViewById(R.id.seekBar1);
				audioManager=(AudioManager)getSystemService(AUDIO_SERVICE);//��ȡ��������
		        int MaxSound=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//��ȡϵͳ�������ֵ
		        seekBar.setMax(MaxSound);//��������Bar�����ֵ����Ϊϵͳ�������ֵ
		        int currentSount=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//��ȡ��ǰ����
		        seekBar.setProgress(currentSount);//��������Bar�ĵ�ǰֵ����Ϊϵͳ������ǰֵ
			    seekBar.setOnSeekBarChangeListener(osbcl);
	}
	
	protected PopupWindow popup;//ʾ���б�
	protected ListView lvPopupWindow;// ����
	private ArrayAdapter<String> stuAdapter;//ѧ������
	ArrayList<String> mylist;
	
	public void initpopupStuWindow() {
		View popView = View.inflate(this, R.layout.stu_list_popup, null);
		popup = new PopupWindow(popView, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		// ʹPopupWindow���Ի�ý��㣬�Ա����ͨ���켣����ϡ��¡����Ҽ������Ʋ˵���,����͸������ɫ
		popup.setFocusable(true);
		popup.setOutsideTouchable(true);
//		ColorDrawable dw = new ColorDrawable(0xb0000000);
//		popup.setBackgroundDrawable(dw);
		//��ʼ���˵��е�ͼ��Listview
		lvPopupWindow = (ListView) popView.findViewById(R.id.stulistview);
		//��ʼ��mylist
		mylist = new ArrayList<String>();
		 //����������������===��ListItem  ʹ��item_stu_list�Ĳ���
		stuAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,mylist); 
		// ��Adapter����
		lvPopupWindow.setAdapter(stuAdapter);
	}
	
	public void initpopupRecordWindow(){		
		// ����������ʾ¼���˵���PopupWindow����
		View myView= (ViewGroup)getLayoutInflater().inflate(R.layout.record, null);
		//������������ʾpopupWindow�Ŀ���
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
    					pauseRecordButton.setText("�ָ�");
    					//¼����ʼ����
    					cmd.iReserver[0] = 3;
    					cmd.SetCmdID(STURECORD);
    					Log.i("sendbt", cmd.toString());
    					us.SendMsg(cmd.toByteArray());
    					Log.i("sendbt", cmd.toString());
    					bRecording = false;	
    				}
    			else {
    				pauseRecordButton.setText("��ͣ");
    				//¼��ֹͣ���� 
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
            	tvRecord.setText("¼��ֹͣ");
				//¼��ֹͣ����
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
		// ����������ʾ¼���б�˵���PopupWindow����
					View popRecordListView = View.inflate(this, R.layout.record_list, null);
					popupRecordListWindow = new PopupWindow(popRecordListView, LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					// ʹPopupWindow���Ի�ý��㣬�Ա����ͨ���켣����ϡ��¡����Ҽ������Ʋ˵���,����͸������ɫ
					popupRecordListWindow.setFocusable(true);
					popupRecordListWindow.setOutsideTouchable(true);
					ColorDrawable dw = new ColorDrawable(0xb0000000);
					popupRecordListWindow.setBackgroundDrawable(dw);

					//��ʼ��¼���˵��е��б�ListView
					lvPopupRecordListWindow = (ListView) popRecordListView.findViewById(R.id.recordlistview);
 }
	/**
	 * getList ��ȡ�б���ʾ
	 * @param id
	 */
		private void getList(String id) {
			
			List<Item> items = dlnaService.getItems(id);
			if (items != null) {
				showItems(items);
			}

		}

	/**
	 * showItems ��ʾ�б�
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
				//��Ƶ����Ƶ��ת��PlayerActivity
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
	//�Ͽ�״̬ת��
	public void StateChange(boolean ClassNow){
		if(ClassNow){
			//�Ҳఴť�û�
			ClassingButton.setBackgroundResource(R.drawable.classing4);
			InternetingButton.setBackgroundResource(R.drawable.interneting4);
			VodingButton.setBackgroundResource(R.drawable.voding4);
			PlayingButton.setBackgroundResource(R.drawable.playing4);
			ReadingButton.setBackgroundResource(R.drawable.reading4);
			ChatingButton.setBackgroundResource(R.drawable.chating4);
			ChatroomButton.setBackgroundResource(R.drawable.chatroom4);
			//�Ҳఴť����
			ClassingButton.setEnabled(false);
			InternetingButton.setEnabled(false);
			VodingButton.setEnabled(false);
			PlayingButton.setEnabled(false);
			ReadingButton.setEnabled(false);
			ChatingButton.setEnabled(false);
			ChatroomButton.setEnabled(false);
			//¼�������û�
//			FollowreadButton.setBackgroundDrawable(R.drawable.);
//			RecordButton.setBackgroundDrawable(R.drawable.);
//			RecordListButton.setBackgroundDrawable(R.drawable.);
//			RecordMenuButton.setBackgroundDrawable(R.drawable.);
			FollowreadButton.setEnabled(false);
			RecordButton.setEnabled(false);
			RecordListButton.setEnabled(false);
			RecordMenuButton.setEnabled(false);
		}else{
			//�Ҳఴť�û�&����
			ClassingButton.setBackgroundResource(R.drawable.classing_style);
			InternetingButton.setBackgroundResource(R.drawable.interneting_style);
			VodingButton.setBackgroundResource(R.drawable.voding_style);
			PlayingButton.setBackgroundResource(R.drawable.playing_style);
			ReadingButton.setBackgroundResource(R.drawable.reading_style);
			ChatingButton.setBackgroundResource(R.drawable.chating_style);
			ChatroomButton.setBackgroundResource(R.drawable.chatroom_style);
			//�Ҳఴť����
			ClassingButton.setEnabled(true);
			InternetingButton.setEnabled(true);
			VodingButton.setEnabled(true);
			PlayingButton.setEnabled(true);
			ReadingButton.setEnabled(true);
			ChatingButton.setEnabled(true);
			ChatroomButton.setEnabled(true);
			//¼�������û�
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
		// ʹPopupWindow���Ի�ý��㣬�Ա����ͨ���켣����ϡ��¡����Ҽ������Ʋ˵���,����͸������ɫ
		popupStuWindow.setFocusable(true);
		popupStuWindow.setOutsideTouchable(true);
//		ColorDrawable dw = new ColorDrawable(0xb0000000);
//		popupStuWindow.setBackgroundDrawable(dw);
		
		//��ʼ���������۲˵��е��б�GridView
		 //��Ӳ�����ʾ
		stlist = new ArrayList<HashMap<String, Object>>();
		 //����������������===��ListItem  ʹ��item_room_list�Ĳ���
		stAdapter = new SimpleAdapter(this, //ûʲô����  
		                              stlist,//������Դ   
		                              R.layout.item_stu_list,//ListItem��XMLʵ��  
		                              //��̬������ListItem��Ӧ������          
		                              new String[] {"ItemImage", "ItemText"},   
		                              //ListItem��XML�ļ������ID  
		                              new int[] {R.id.ivPicTou,R.id.tvNameStu}); 
		 //��ʼ��GridView
		gvStu = (GridView)popStuView.findViewById(R.id.stugridview);
		gvStu.setAdapter(stAdapter);
		//��ʼ��TextView
		tvStuInfo = (TextView)popStuView.findViewById(R.id.tvStuinfo);
		//��ʼ��Button
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
		//��ʼ��scrollview
		sView = (ScrollView)popStuView.findViewById(R.id.SVstu);
	}
	
	protected void loadpopupSt(byte[] data,boolean quit){
		byte[] cmd_data = new byte[DATALONG];// ����
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
				tagStuList stuinfo = new tagStuList(demob);//��Ա��Ϣ
				HashMap<String, Object> map = new HashMap<String, Object>();
			    if(stuinfo.Sex == 0)map.put("ItemImage",R.drawable.homegreen);  //ͷ��ͼƬ���ж���Ů,��ʱΪ��
			        else map.put("ItemImage",R.drawable.green); //Ů
			    map.put("ItemText", stuinfo.StrName);  //ѧ������
			    stlist.add(map);  	
			}
		}
		if (quit) {
			if (cmd.strIP.equals(TCmdData.strIP)) {
				SpannableString ss = new SpannableString("���Ѿ��˳��������ۣ�\n");
				ss.setSpan(new ForegroundColorSpan(Color.RED), 0, ss.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				tvStuInfo.append(ss);
				sView.pageScroll(View.FOCUS_DOWN);
				CancleButton.setEnabled(false);
				OKButtonStu.setEnabled(true);
			}else {
				SpannableString ss = new SpannableString(TCmdData.strName+"�˳�����������\n");
				ss.setSpan(new ForegroundColorSpan(Color.RED), 0, ss.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				tvStuInfo.append(ss);
				sView.pageScroll(View.FOCUS_DOWN);
			}
		}else {
			if (cmd.strIP.equals(TCmdData.strIP)) {
				SpannableString ss = new SpannableString("������������ɹ�������Է�����\n");
				ss.setSpan(new ForegroundColorSpan(Color.RED), 0, ss.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				tvStuInfo.append(ss);
				sView.pageScroll(View.FOCUS_DOWN);
				CancleButton.setEnabled(true);
				OKButtonStu.setEnabled(false);
			}else {
				SpannableString ss = new SpannableString(TCmdData.strName+"�Ѿ�������������\n");
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