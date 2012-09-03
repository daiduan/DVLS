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
 *-------------------������----------------------
 *
 */
public class ChatRoomActivity extends BaseChat{
	
	byte[] msg_data = new byte[DATALONG];// ����
	// ����	
	protected int CurrentRoomNum = -1;// ��ǰ�����
	protected String chat_msg;// ����Ϣ
	//IP��ַ�б�
	ArrayList<String> IPs = new ArrayList<String>();

	protected TextView tvMember;
	int preRoom = -1;
	protected GridView RoomList;
	//����
	Button btVoiceOpen;//��������
	Button btVoiceCls;
	boolean VoiceOn = false;//��������
	//�˳�����
	Button quitBT;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chatroom);
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
//				//����������ʾ
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
				AlertDialog.Builder NotifyDialog= new AlertDialog.Builder(ChatRoomActivity.this);
	
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
				Intent intent = new Intent().setClass(ChatRoomActivity.this,
						ClassTeachActivity.class);
				intent.setData(Uri.parse("0"));
				ChatRoomActivity.this.startActivity(intent);
				break;
			//---------------������Ϣ----------------------//
			case GETROOM:
				getRoomList(msg.getData().getByteArray("data"));
				break;
			// ------------------------------------------------------------
			//������Ϣ����
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
	
	// ��ʼ����ͼ
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
		sv = (ScrollView)findViewById(R.id.SV);//������
		//�����س��� 
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
	
	//��෿���б�
	private void initRoomList(){
		//��ʼ�������б�
		RoomList = (GridView)findViewById(R.id.RoomListView);
		//���ɶ�̬���飬��ʼ������  
	    ArrayList<HashMap<String, Object>> mylist = new ArrayList<HashMap<String, Object>>();  
	    
	    for(int i=0;i<10;i++)  //��ΪGridView��child������Ϊ12�����Բ��ܳ������ֵ�����Ժ���
	    {  
	        HashMap<String, Object> map = new HashMap<String, Object>();  
	        map.put("ItemImage",R.drawable.homegreen);  
	        map.put("ItemText", "����"+(i+1));  //1-10��ľ��0��
	        mylist.add(map);  
	    }
	    
	    //����������������===��ListItem  
	   SimpleAdapter mSchedule = new SimpleAdapter(this, //ûʲô����  
	                                                mylist,//������Դ   
	                                                R.layout.item_room_list,//ListItem��XMLʵ��  
	                                                //��̬������ListItem��Ӧ������          
	                                                new String[] {"ItemImage", "ItemText"},   
	                                                //ListItem��XML�ļ������ID  
	                                                new int[] {R.id.ivRoom,R.id.tvRoom});  
	    //��Ӳ�����ʾ
        RoomList.setAdapter(mSchedule);
        //���ü��������Ե�����м���
        RoomList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id){ 
				// TODO Auto-generated method stub
				if(connected)//ֻ��������IP�ſ��Խ��뷿��
				if(CurrentRoomNum != position){
					//�½��뷿��
					/*
					 * Reserve[0]������Ϣ���ͣ�
					 * 1:������������ 2�������˳�����  3:ˢ��   4:��ʼ˵��  5:ֹͣ˵��
					 * 
					 * Reserve[1]�������:1~15 Reserve[119]����ѧ���Ա�
					 * 
					 */	
					//�ı�ѡ����ͼƬ
					ImageView iv;
					//-----------�˳��ɷ���----------------
					if(preRoom != -1){
						//�ı�ɷ�����ɫ
						iv = (ImageView)RoomList.getChildAt(CurrentRoomNum).findViewById(R.id.ivRoom);
						iv.setImageResource(R.drawable.homegreen);
						//�����˳�����
						cmd.iReserver[0] = 2;
						//�����˳������
						cmd.iReserver[1]=CurrentRoomNum;
						//������������
						cmd.SetCmdID(SENDROOM);
						us.SetIP(ServerIP);
						us.SendMsg(cmd.toByteArray());
					}
					
					//-----------�����·���-------------------
					//�ı��·�����ɫ
					iv = (ImageView)view.findViewById(R.id.ivRoom);
					iv.setImageResource(R.drawable.homered);
					//������з���
					if(CurrentRoomNum == -1){
						preRoom = position;
					}else preRoom = CurrentRoomNum;
					//���浱ǰ�����
					CurrentRoomNum = position;
					//���ü�������
					cmd.iReserver[0]= 1;
					//����ѡ�з����
					cmd.iReserver[1]= position;
					//������������
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
		//ע��㲥������
		super.onStart();
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction("android.intent.action.COMMAND");
		iFilter.addAction("android.intent.action.GETROOM");
		iFilter.addAction("android.intent.action.MSGCHATROOM");
		registerReceiver(BR,iFilter);	
	}
	
	protected void onStop(){
		//ע���㲥������
		super.onStop();
		unregisterReceiver(BR);
	}	
	
	//��дsendout����
	public void sendout(Editable etb){
		//���뱾����Ϣ
		etb.insert(0,cmd.strName+"˵��");
		String StrChat = FilterHtml((Html.toHtml(etb)));
		int StrLen = StrChat.length();//���ݳ���
		//����
		cmd.SetCmdID(MSGCHATROOM);
		//��������������Ϣ�ϲ�
		byte[] msg = new byte[DATALONG+StrLen];
		System.arraycopy(cmd.toByteArray(), 0, msg, 0, DATALONG);//����
		System.arraycopy(StrChat.getBytes(), 0, msg, DATALONG, StrLen);//����
		
		for(int i=0;i!=CurrentIPNum;i++){
			if(!cmd.strIP.equals(IPs.get(i))){//�����Լ�������Ϣ
				String IP = IPs.get(i);
				us.SetIP(IP);
				us.SendMsg(msg);
			}	
		}
	}
	
	//--------------------------------------------------------------------------------
	//GetRoom
	public void getRoomList(byte[] data){
		byte[] cmd_data = new byte[DATALONG];// ����
		System.arraycopy(data, 0, cmd_data, 0, DATALONG);
		tagCommandCode TCmdData = new tagCommandCode(cmd_data);
		
		SpannableString ss;//������Ϣ
		/*
		 * command.Reserve[3];//�������ͷ���(1���� ��2�˳�  ��3˵��  ��4��˵��)
		 * command.Reserve[2];//��Ա��
		 * command.Reserve[1];//�����
		 * command.Reserve[0]=3;//ˢ��
		 * command.Reserve[0]=6;//�ɹ�
		 */
		
		if(TCmdData.iReserver[0] == 3 && TCmdData.iReserver[1] == CurrentRoomNum){
			boolean ListRe =true; 
			//������ˢ�·����б�
			if(!TCmdData.strIP.equals(cmd.strIP))
				switch(TCmdData.iReserver[3]){
				case 2://ĳͬѧ�˳�����
					ss = new SpannableString("ϵͳ��ʾ��"+TCmdData.strName+"�˳��˱�����\n");
					EnterRoomSS(ss);
					break;
				case 1://ĳͬѧ���뷿��
					ss = new SpannableString("ϵͳ��ʾ��"+TCmdData.strName+"�����˱�����\n");
					EnterRoomSS(ss);
					break;
				case 4://ĳͬѧ��ʼ˵��
					ss = new SpannableString("ϵͳ��ʾ��"+TCmdData.strName+"��ʼ����\n");
					EnterRoomSS(ss);
					ListRe = false;
					break;
				case 5://ĳͬѧֹͣ˵��
					ss = new SpannableString("ϵͳ��ʾ��"+TCmdData.strName+"ֹͣ����\n");
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
					IPs.add(cmd.subIP+new tagStuList(demob).IP);//IP��ַ
					SpannableString s = new SpannableString(new tagStuList(demob).StrName+"  ");
					tvMember.append(s);
				}
			}	
		}else if(TCmdData.iReserver[0] == 6) {//�ɹ�����
			if(cmd.strIP.equals(TCmdData.strIP)){
				switch(TCmdData.iReserver[3]){
			case 0:
				break;
			case 1://������ͬ���Լ�����ĳ������
				//���浱ǰ�����
				//CurrentRoomNum = TCmdData.iReserver[1];
				//��ʾ���뷿��
				ss = new SpannableString("ϵͳ��ʾ��������˷���"+String.valueOf(CurrentRoomNum+1)+"\n");
				EnterRoomSS(ss);
				break;
			case 3://������ͬ���Լ��˳�����
				//����ɷ����
				//preRoom = CurrentRoomNum;
				ss = new SpannableString("ϵͳ��ʾ�����˳��˷���"+String.valueOf(TCmdData.iReserver[1]+1)+"\n");
				EnterRoomSS(ss);
				break;
			case 4://ͬ��˵��
				ss = new SpannableString("ϵͳ��ʾ������Կ�ʼ������"+"\n");
				EnterRoomSS(ss);
				break;
			case 5://ֹͣ˵��
				ss = new SpannableString("ϵͳ��ʾ�����Ѿ��˳�������"+"\n");
				EnterRoomSS(ss);
				break;}
			}else{
				switch(TCmdData.iReserver[3]){
			case 0:
				break;
			case 4://ͬ��˵��
				ss = new SpannableString("ϵͳ��ʾ��"+TCmdData.strName+"��ʼ������\n");
				EnterRoomSS(ss);
				break;
			case 5://ֹͣ˵��
				ss = new SpannableString("ϵͳ��ʾ��"+TCmdData.strName+"�˳�������\n");
				EnterRoomSS(ss);
				break;
			}
			}
		}			
	}
	
	public void onClick(View v) {
		// TODO Auto-generated method stub
			if (v == chat_send) {
				send();//������Ϣ
			}else if(v == chat_emotion){
				if(popup.isShowing()){
					popup.dismiss();
				}else
					popup.showAsDropDown(chat_emotion,chat_emotion.getWidth(),-gvPopupWindow.getHeight());
			}else if(v.equals(btVoiceOpen)){
				if (connected) {
				//��ʼ˵������
				cmd.iReserver[0] = 4;
				//���÷����
				cmd.iReserver[1]=CurrentRoomNum;
				//������������
				cmd.SetCmdID(SENDROOM);
				us.SetIP(ServerIP);
				us.SendMsg(cmd.toByteArray());
				btVoiceCls.setEnabled(true);
				btVoiceOpen.setEnabled(false);				
				}
			}else if(v.equals(btVoiceCls)){
				if (CurrentRoomNum!= -1) {
					//����ֹͣ����
					cmd.iReserver[0] = 5;
					//�����˳������
					cmd.iReserver[1]=CurrentRoomNum;
					//������������
					cmd.SetCmdID(SENDROOM);
					us.SetIP(ServerIP);
					us.SendMsg(cmd.toByteArray());
					btVoiceCls.setEnabled(false);
					btVoiceOpen.setEnabled(true);
				}
			}else if(v.equals(quitBT)){
				//�����˳�����
				cmd.iReserver[0] = 2;
				//�����˳������
				cmd.iReserver[1]=CurrentRoomNum;
				//������������
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
		tvSystemState.setText("������");
		
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
