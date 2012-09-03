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
	/*-------------------�����Ķ�----------------------*/
	//��ҳ��ͼ
	private WebView webview;

	
	//��ʾ�б�
	protected PopupWindow popupReadingWindow;
	protected ListView lvPopupWindow;
	

	//��ť
	private Button DisplayButton;
	private Button OpenfileButton;
	private Button PreviousButton;
	private Button NextButton;
	
	//�ļ��б�
	List<String> urllist;  
    ArrayAdapter<String> adapter;//������
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
		//UPnPService����δ����
//		if (dlnaService == null) {
//		//	dlnaServiceDelayedStart = true;
//		}
		//����ļ���ַ
		if (mUrl != null) {
			//���õ�ǰ�ļ���
			txturl = mUrl.toString();
			//��֤�Ƿ������ͬ���ļ�
			boolean bFileIn = false;
			if(!urllist.isEmpty()){
				bFileIn = urllist.contains(txturl);
			}
				
			if(!bFileIn){
				//����������б�
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
			//�ǵ㲥��ת
//			shortcutResume = true;
		}

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
				AlertDialog.Builder NotifyDialog= new AlertDialog.Builder(ReadingActivity.this);
	
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
					Intent intent = new Intent().setClass(ReadingActivity.this,
							ClassTeachActivity.class);
					intent.setData(Uri.parse("0"));
					ReadingActivity.this.startActivity(intent);
					break;
				//---------------������Ϣ----------------------//
					
				}
				
			}
			};
	// ��ʼ��
		public void init() {
			super.init();
			initPopupReadingWnd();
		}
	public void initView(){
			setContentView(R.layout.reading);
//			Bundle bundle = this.getIntent().getExtras();
//	        txturl = this.getIntent().getData();;//��ǰ�����ļ���·��
	    	DisplayButton=(Button)findViewById(R.id.displaybutton);
	    	OpenfileButton=(Button)findViewById(R.id.openfilebutton);
	    	PreviousButton=(Button)findViewById(R.id.previousbutton);
	    	NextButton=(Button)findViewById(R.id.nextbutton);
	    	webview = (WebView) findViewById(R.id.readingWEB);
			
	    	DisplayButton.setOnClickListener(this);
	    	OpenfileButton.setOnClickListener(this);
	    	PreviousButton.setOnClickListener(this);
	    	NextButton.setOnClickListener(this);
	    	
	    	//ͨ��WebView�õ�WebSettings���� 
	    	WebSettings mWebSettings = webview.getSettings(); 
	    	//������ҳĬ�ϱ��� 
	    	webview.getSettings().setDefaultTextEncodingName("GBK");   
	    	
	    	BaseView();
	}
	
	BroadcastRe BR = new BroadcastRe(myHandler);
	protected void onStart(){
		//ע��㲥������
		super.onStart();
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction("android.intent.action.COMMAND");
		registerReceiver(BR,iFilter);	
	}
	protected void onStop(){
		//ע���㲥������
		super.onStop();
		unregisterReceiver(BR);
	}
	    	//��ť������
	  public void onClick(View v) {
	    			// TODO Auto-generated method stub			
	        		System.out.println("bool!!!");
	        		if (v == DisplayButton) {
	    				//��ʾ�б�	
	        			if(popupReadingWindow.isShowing()){
	        				DisplayButton.setText("�����б�");
	        				popupReadingWindow.dismiss();
	    				}else
	    					//popupReadingWindow.showAsDropDown(DisplayButton,DisplayButton.getWidth(),-lvPopupWindow.getHeight());
	    			       popupReadingWindow.showAsDropDown(DisplayButton,0,0);     
	    			}
	    			else if (v == OpenfileButton) {
	    				//��ת�������㲥
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
	    					Toast.makeText(getApplicationContext(), "�Ѿ��ǵ�һƪ",
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
		    					Toast.makeText(getApplicationContext(), "�Ѿ������һƪ",
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
			tvSystemState.setText("�ı��Ķ�");
			
			//��������
			seekBar = (SeekBar)findViewById(R.id.seekBar1);
			audioManager=(AudioManager)getSystemService(AUDIO_SERVICE);//��ȡ��������
	        int MaxSound=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//��ȡϵͳ�������ֵ
	        seekBar.setMax(MaxSound);//��������Bar�����ֵ����Ϊϵͳ�������ֵ
	        int currentSount=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//��ȡ��ǰ����
	        seekBar.setProgress(currentSount);//��������Bar�ĵ�ǰֵ����Ϊϵͳ������ǰֵ
		    seekBar.setOnSeekBarChangeListener(osbcl);
		}
	 
	 public void initPopupReadingWnd(){
			// ����������ʾ�Ķ��б�˵���PopupWindow����
						View popReadingView = View.inflate(this, R.layout.item_play_list, null);
						popupReadingWindow = new PopupWindow(popReadingView, LayoutParams.WRAP_CONTENT,
								LayoutParams.WRAP_CONTENT);
						// ʹPopupWindow���Ի�ý��㣬�Ա����ͨ���켣����ϡ��¡����Ҽ������Ʋ˵���,����͸������ɫ
						popupReadingWindow.setFocusable(true);
						popupReadingWindow.setOutsideTouchable(true);
						ColorDrawable dw = new ColorDrawable(0xb0000000);
						popupReadingWindow.setBackgroundDrawable(dw);
						
			//��ʼ���Ķ��˵��е��б�ListView
						lvPopupWindow = (ListView) popReadingView.findViewById(R.id.playlistview);						
			//��ʼ������
			UserInfo lds = ((UserInfo) getApplicationContext());
			urllist = lds.getTxtUrl();
			titleList = lds.getTxtTitle();			
			if(titleList == null){
					urllist = new ArrayList<String>();
					titleList=new ArrayList<String>();
			    	
			    }
			if(adapter== null)
			adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,titleList);
			//��Ӳ�����ʾ
			lvPopupWindow.setAdapter(adapter);
//	        //���ü��������Ե�����м���
			lvPopupWindow.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position,
						long id){ 
					// TODO Auto-generated method stub
					popupReadingWindow.dismiss();
					//���ļ�
					txturl = urllist.get(position);
					//��ʾ�ı�
					webview.loadUrl(txturl);
					}
	        }
	        );
	        
		
	}

}
	
	
