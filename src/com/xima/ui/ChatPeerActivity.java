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
 *-------------------ѧϰ����----------------------
 *
 */
public class ChatPeerActivity extends BaseChat {

	//����
	Button btVoiceRequest;//��������
	Button btVoiceClear;//����ֹͣ
	Button btVoiceOK;//ͬ������
	Button btVoiceReject;//�ܾ�����
	Button btVoiceCancle;//�Ҷ�
	/*
	 * �����������
	 *     ״̬                                             ����  ֹͣ  ͬ��  �ܾ�  �Ҷ�
	 * 0 �Լ�����������������0     1  0   0  0   0
	 *  1�Լ�����                             1     0  1   0  0   0
	 *  2��������                             2     0  0   1  1   0
	 *  3ͨ��                                     3     0  0   0  0   1
	 */
	int VoiceState;//��������״̬
	ArrayList<HashMap<String, Object>> mylist;  
	String CurrentReqMM = null;//��ǰ������󣬰�����1.�Լ��������2.�Է��������
	String CurrentReqIP = null;//��ǰ����IP��������1.�Լ��������2.�Է��������
	
	//IP��ַ�б�
	ArrayList<String> IPs = new ArrayList<String>();
	ArrayList<String> Users = new ArrayList<String>();
	//�Ի�����
	String CurrentMM;//�������
	String CurrentIP = null;//����IP
	boolean toPublic = false;//��ȫ��㲥
	Button PublicSwitch;//�Ƿ���ȫ��˵��
	TextView tvChatName;
	SimpleAdapter mSchedule;//������
	//�ı���ʾ
	SpannableString ss;
	protected GridView List;
	Button btRefresh;//ˢ��
	boolean NotReq = true;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chatpeer);//ERROR!��Ҫ�޸ģ�
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
				AlertDialog.Builder NotifyDialog= new AlertDialog.Builder(ChatPeerActivity.this);
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
				Intent intent = new Intent().setClass(ChatPeerActivity.this,
						ClassTeachActivity.class);
				intent.setData(Uri.parse("0"));
				ChatPeerActivity.this.startActivity(intent);
				break;
			//---------------������Ϣ----------------------//
				//������Ϣ����
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
		
		// ��ʼ����ͼ
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
			PublicSwitch.setText("���������˵��");
			
			tvChatName = (TextView)findViewById(R.id.tvChatName);
			//��ʼ��
			btVoiceRequest.setEnabled(true);
			btVoiceClear.setEnabled(false);
			btVoiceOK.setEnabled(false);
			btVoiceReject.setEnabled(false);
			btVoiceCancle.setEnabled(false);
			
			//Input
			ChatWindow = (TextView)findViewById(R.id.ChatWindow_p);
			ChatInput = (EditText)findViewById(R.id.ChatInput_p);
			sv = (ScrollView)findViewById(R.id.SV_p);//������
			//�����س��� 
	        ChatInput.setOnKeyListener(new OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					if(keyCode == KeyEvent.KEYCODE_ENTER){
						if((CurrentIP == null) && (!toPublic)){
							ChatInput.setError("��ѡ���������");
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
			//ע��㲥������
			super.onStart();
			IntentFilter iFilter = new IntentFilter();
			iFilter.addAction("android.intent.action.COMMAND");
			iFilter.addAction("android.intent.action.MSGCHATPEER");
			iFilter.addAction("android.intent.action.REFUSHLIST");
			registerReceiver(BR,iFilter);	
		}
		
		protected void onStop(){
			//ע���㲥������
			super.onStop();
			unregisterReceiver(BR);
		}	
	public void onClick(View v) {
		// TODO Auto-generated method stub
		/*������Ϣ�Զ���
		 * VOICEREQUEST
		 * 1.Reserve[0]=0  ����ͨ��
		 * 2.Reserve[0]=1 �ܾ�ͨ��
		 * 3.Reserve[0]=2 �������
		 * 3.Reserve[0]=3 �Ҷ�
		 * 4.Reserve[0]=4 ͬ��ͨ��
		 * cmd.iReserver[1]
		 * ��֪�����Լ���״̬
		 */
		if(v.equals(btVoiceRequest)){//��������
			if(VoiceState == 0){
				if(CurrentMM == null){
					btVoiceRequest.setError("��ѡ��һ���������");
				}else{//��ǰ��������������
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
			    	
					ss = new SpannableString("ϵͳ��ʾ��������������"+CurrentMM+"��������ͨ��\n");
			    	EnterRoomSS(ss);
				}
			}
		}else if(v.equals(btVoiceClear)){//����ȡ��
			if(VoiceState ==1 && (CurrentReqMM!=null)){
				//���͸�ͨ������
				cmd.iwID = VOICEREQUEST;
				cmd.iReserver[0]=2;						
				us.SetIP(CurrentReqIP);
				us.SendMsg(cmd.toByteArray());
				
				ss = new SpannableString("ϵͳ��ʾ�����Ѿ�ֹͣ��"+CurrentReqMM+"��ͨ������\n");
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
		}else if(v.equals(btVoiceOK)){          //ͬ������         ----��ʼͨ��
			if(VoiceState == 2 && CurrentReqMM != null){
					//���������������
					cmd.iwID = SELFTALK;
					cmd.iReserver[0]=1;
					String cmdIP = cmd.strIP.substring(cmd.strIP.lastIndexOf(".")+1,cmd.strIP.length());
					cmd.iReserver[1] = Integer.parseInt(cmdIP);
					cmd.iReserver[2] =  Integer.parseInt(CurrentReqIP.substring(CurrentReqIP.lastIndexOf(".")+1,CurrentReqIP.codePointCount(0, CurrentReqIP.length())));
					us.SetIP(ServerIP);
					us.SendMsg(cmd.toByteArray());
					//���͸�ͨ������
					cmd.iwID = VOICEREQUEST;
					cmd.iReserver[0]=4;						
					us.SetIP(CurrentReqIP);
					us.SendMsg(cmd.toByteArray());
					//��ʾ
					ss = new SpannableString("ϵͳ��ʾ������Կ�ʼ��"+CurrentReqMM+"��ͨ������\n");
			    	EnterRoomSS(ss);
					//���õ�ǰ״̬
					VoiceState = 3;//ͨ��
					//���ð�ť
					btVoiceReject.setEnabled(false);
					btVoiceRequest.setEnabled(false);
					btVoiceOK.setEnabled(false);
					btVoiceClear.setEnabled(false);
					btVoiceCancle.setEnabled(true);

			}
		}else if(v.equals(btVoiceReject)){//�ܾ�����
			if(CurrentReqMM !=null && VoiceState == 2){
				//�������߾ܾ�
				cmd.iwID = VOICEREQUEST;
				cmd.iReserver[0]=1;
				us.SetIP(CurrentReqIP);
				us.SendMsg(cmd.toByteArray());
				//��ʾ
				ss = new SpannableString("ϵͳ��ʾ�����Ѿ��ܾ���"+CurrentReqMM+"��ͨ������\n");
		    	EnterRoomSS(ss);
				//���õ�ǰ״̬
				VoiceState = 0;
		    	CurrentReqMM = null;
				CurrentReqIP = null;
				btVoiceRequest.setEnabled(true);
				btVoiceClear.setEnabled(false);
				btVoiceOK.setEnabled(false);
				btVoiceReject.setEnabled(false);
				btVoiceCancle.setEnabled(false);
				
			}
		}else if(v.equals(btVoiceCancle)){//�Ҷ�����        ----ֹͣͨ��
			if(VoiceState == 3 && CurrentReqMM!= null){
				//���͹Ҷ���������
				cmd.iwID = SELFTALK;
				cmd.iReserver[0]=0;
				cmd.iReserver[1] = Integer.parseInt(cmd.strIP.substring(cmd.strIP.lastIndexOf(".")+1, cmd.strIP.length()));
				cmd.iReserver[2] =  Integer.parseInt(CurrentReqIP.substring(CurrentReqIP.lastIndexOf(".")+1,CurrentReqIP.length()));
				us.SetIP(ServerIP);
				us.SendMsg(cmd.toByteArray());
				//���͹Ҷ�������
				cmd.iwID = VOICEREQUEST;
				cmd.iReserver[0]=3;
				us.SetIP(CurrentReqIP);
				us.SendMsg(cmd.toByteArray());
				//��ʾ
				ss = new SpannableString("ϵͳ��ʾ�����Ѿ��Ҷ�����"+CurrentReqMM+"��ͨ��\n");
		    	EnterRoomSS(ss);
				//���õ�ǰ״̬
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
			//�����������ˢ���б�
			if(connected){
				cmd.iwID =REFUSHLIST;
				us.SetIP(ServerIP);
				us.SendMsg(cmd.toByteArray());	
			}
		}else if(v.equals(chat_send)){
			//������Ϣ
			send();
		}else if(v.equals(chat_emotion)){
			//�����б�
			if(popup.isShowing()){
				popup.dismiss();
			}else
				popup.showAsDropDown(chat_emotion,chat_emotion.getWidth(),-gvPopupWindow.getHeight());
		}else if(v.equals(PublicSwitch)){
			if(toPublic){
				PublicSwitch.setText("���������˵��");
				toPublic = false;
			}else {
				PublicSwitch.setText("��ȫ��˵��");
				toPublic = true;
			}
		}
	}
		
	
	//��дsendout����
		public void sendout(Editable etb){
			//���뱾����Ϣ
			etb.insert(0, cmd.strName+"˵��");
			String StrChat = FilterHtml((Html.toHtml(etb)));
			int StrLen = StrChat.length();//���ݳ���
			//����
			cmd.SetCmdID(MSGCHATPEER);
			//��������������Ϣ�ϲ�
			byte[] msg = new byte[DATALONG+StrLen];
			System.arraycopy(cmd.toByteArray(), 0, msg, 0, DATALONG);//����
			System.arraycopy(StrChat.getBytes(), 0, msg, DATALONG, StrLen);//����
			
			if(toPublic)
				for(int i=0;i!=CurrentIPNum;i++){
					if(!cmd.strIP.equals(IPs.get(i))){//�����Լ�������Ϣ
						us.SetIP((String) IPs.get(i));
						us.SendMsg(msg);
					}				
				}
			else {
				us.SetIP(CurrentIP);
				us.SendMsg(msg);}
		}
		
	public void initList(){
		
		 //��Ӳ�����ʾ
		mylist = new ArrayList<HashMap<String, Object>>();
		 //����������������===��ListItem  ʹ��item_room_list�Ĳ���
		mSchedule = new SimpleAdapter(this, //ûʲô����  
		                              mylist,//������Դ   
		                              R.layout.item_room_list,//ListItem��XMLʵ��  
		                              //��̬������ListItem��Ӧ������          
		                              new String[] {"ItemImage", "ItemText"},   
		                              //ListItem��XML�ļ������ID  
		                              new int[] {R.id.ivRoom,R.id.tvRoom}); 
		 //��ʼ����Ա�б�
		List = (GridView)findViewById(R.id.LV_p);
		List.setAdapter(mSchedule);
        //���ü��������Ե�����м���
        List.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id){ 
				// TODO Auto-generated method stub
				if(!IPs.isEmpty()){
					//�����������
					CurrentMM = Users.get(position);
					CurrentIP = IPs.get(position);
					//ϵͳ��ʾ
					ss = new SpannableString("ϵͳ��ʾ����ѡ������"+CurrentMM +"����\n");
			    	EnterRoomSS(ss);
			    	//���õ�ǰ��ʾ�Ի�������
			    	tvChatName.setText(CurrentMM);
					}
				}
        }
        );
	}
	public void LoadList(byte[] data){
		
		byte[] cmd_data = new byte[DATALONG];// ����
		System.arraycopy(data, 0, cmd_data, 0, DATALONG);
		tagCommandCode TCmdData = new tagCommandCode(cmd_data);
		CurrentIPNum = TCmdData.iReserver[0];
		boolean MMNotIn = true;//��ǰ�����Ƿ�����������
		
	if(TCmdData.idwCmdSize == CurrentIPNum*STULISTLONG)//��֤����
	{
		//�������
		mylist.clear();
		mSchedule.notifyDataSetChanged();
		IPs.clear();
		Users.clear();
		//���ɶ�̬���飬��ʼ������  	   
	    for(int i=0;i<CurrentIPNum;i++)//����ѧ���������
	    {
	    	byte[] demob = new byte[STULISTLONG];
			System.arraycopy(data, DATALONG + i * STULISTLONG,
					demob, 0, STULISTLONG);
			tagStuList stuinfo = new tagStuList(demob);//��Ա��Ϣ
			if( (cmd.subIP+stuinfo.IP).equals(CurrentIP)|| CurrentIP == null) MMNotIn = false;//�б��������� 
			if(!cmd.strIP.equals(cmd.subIP+stuinfo.IP)){//�ų��Լ������б�
				 HashMap<String, Object> map = new HashMap<String, Object>();
			     if(stuinfo.Sex == 0)map.put("ItemImage",R.drawable.homegreen);  //ͷ��ͼƬ���ж���Ů,��ʱΪ��
			        else map.put("ItemImage",R.drawable.green); //Ů
			    	IPs.add(cmd.subIP+stuinfo.IP);//IP��ַ
					Users.add(stuinfo.StrName);
			        map.put("ItemText", stuinfo.StrName);  //ѧ������
			        mylist.add(map);  	
			}
	    }
	    CurrentIPNum = CurrentIPNum-1;
	    mSchedule.notifyDataSetChanged();

	}
	 if(MMNotIn){//������������뿪
	    	ss = new SpannableString("ϵͳ��ʾ��"+CurrentMM +"�Ѿ����ڷ����ˣ�\n");
	    	EnterRoomSS(ss);
	    	CurrentMM = null;
	    	CurrentIP = null;
	    	tvChatName.setText(" ");
	    }
}
	
	public void VoiceRequest(byte[] data){
		/*������Ϣ�Զ���
		 * VOICEREQUEST
		 * 1.Reserve[0]=0  ����ͨ��
		 * 2.Reserve[0]=1 �ܾ�ͨ��
		 * 3.Reserve[0]=2 �������
		 * 3.Reserve[0]=3 �Ҷ�
		 * 4.Reserve[0]=4 ͬ��ͨ��
		 * cmd.iReserver[1]
		 * ��֪�����Լ���״̬
		 */

		byte[] cmd_data = new byte[DATALONG];// ����
		System.arraycopy(data, 0, cmd_data, 0, DATALONG);
		tagCommandCode TCmdData = new tagCommandCode(cmd_data);
		
		switch(TCmdData.iReserver[0]){
		case 0://����ͨ��
			if(VoiceState == 0){//�ȴ�״̬
				VoiceState = 2;//change to ������״̬
				CurrentReqMM = TCmdData.strName;
				CurrentReqIP = TCmdData.strIP;;
				
				btVoiceRequest.setEnabled(false);
				btVoiceClear.setEnabled(false);
				btVoiceOK.setEnabled(true);
				btVoiceReject.setEnabled(true);
				btVoiceCancle.setEnabled(false);
				
				ss = new SpannableString("ϵͳ��ʾ��"+CurrentReqMM+"�������������Ի���\n");
		    	EnterRoomSS(ss);	
			}else{//�ܾ�����
				cmd.iwID = VOICEREQUEST;
				cmd.iReserver[0]=1;
				cmd.iReserver[1]=VoiceState;
				us.SetIP(TCmdData.strIP);
				us.SendMsg(cmd.toByteArray());
				}
			break;
		case 1://�ܾ�ͨ��
			if(TCmdData.strName.equals(CurrentReqMM)){//��ǰ�������ܾ����Լ�
				//�����������״̬������ʾ
				switch(cmd.iReserver[1]){
				case 1:
					ss = new SpannableString("ϵͳ��ʾ��"+CurrentReqMM+"��������������ͨ����\n");
					break;
				case 2:
					ss = new SpannableString("ϵͳ��ʾ��"+CurrentReqMM+"�Ѿ��ӵ�����ͨ������\n");
					break;
				case 3:
					ss = new SpannableString("ϵͳ��ʾ��"+CurrentReqMM+"��������������ͨ����\n");
					break;
				default:
					ss = new SpannableString("ϵͳ��ʾ��"+CurrentReqMM+"�ܾ�������ͨ������\n");
					break;
				}
		    	EnterRoomSS(ss);
		    	CurrentReqMM = null;
				CurrentReqIP = null;
				VoiceState = 0;//change to �ȴ�״̬
				btVoiceRequest.setEnabled(true);
				btVoiceClear.setEnabled(false);
				btVoiceOK.setEnabled(false);
				btVoiceReject.setEnabled(false);
				btVoiceCancle.setEnabled(false);
				}
			break;
		case 2://�������
			//�����������״̬������ʾ
			if(TCmdData.strName.equals(CurrentReqMM)){
				ss = new SpannableString("ϵͳ��ʾ��"+CurrentReqMM+"ȡ����������ͨ������\n");
				EnterRoomSS(ss);
				CurrentReqMM = null;
				CurrentReqIP = null;
				VoiceState = 0;//change to �ȴ�״̬
				
				btVoiceRequest.setEnabled(true);
				btVoiceClear.setEnabled(false);
				btVoiceOK.setEnabled(false);
				btVoiceReject.setEnabled(false);
				btVoiceCancle.setEnabled(false);
				
				
			}
			
			break;
		case 3://�Ҷ�
			if(TCmdData.strName.equals(CurrentReqMM) && VoiceState == 3){
				ss = new SpannableString("ϵͳ��ʾ��"+CurrentReqMM+"�Ҷ���������ͨ����\n");
				EnterRoomSS(ss);
				
				CurrentReqMM = null;
				CurrentReqIP = null;
				VoiceState = 0;//change to �ȴ�״̬
				
				btVoiceRequest.setEnabled(true);
				btVoiceClear.setEnabled(false);
				btVoiceOK.setEnabled(false);
				btVoiceReject.setEnabled(false);
				btVoiceCancle.setEnabled(false);
				
				
			}
		case 4://������󷢻�ͬ��ͨ��
			if(TCmdData.strName.equals(CurrentReqMM)&& VoiceState == 1){
				ss = new SpannableString("ϵͳ��ʾ��"+CurrentReqMM+"ͬ��������ͨ������\n");
				EnterRoomSS(ss);
				
				VoiceState = 3;//change to ͨ��״̬
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
		tvSystemState.setText("ѧϰ����");
		
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
