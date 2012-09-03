	package com.xima.ui;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.xima.datadef.UserInfo;
import com.xima.datadef.tagCommandCode;
import com.xima.network.BroadcastRe;
import com.xima.network.UPnPService;

public class PlayerActivity extends BaseActivity implements MediaPlayer.OnCompletionListener{

    private static final String TAG = "VideoActivity";
    private SurfaceView surfaceView;
    private MediaPlayer mediaPlayer;
    private String filename;
    private int position;//��¼����λ��
//    private Button playButton;//���Ű�ť
    private Button pauseButton;//��ͣ��ť
    private Button resetButton;//���²��Ű�ť
    private Button stopButton;//���˰�ť
    private Button fastforwardButton;//�����ť
    private Button fastbackButton;//���˰�ť
    private Button displaysonglistButton;//��ʾ�����б�ť
    private Button fullscreenButton;//ȫ����ť
    private SeekBar ProseekBar;//��������������
    private Button nextsongButton;//��һ����ť
    private Button previoussongButton;//����һ����ť
    private PopupWindow popupPlayListWindow;
    private ListView lvPopupPlayListWindow;
    private HandlerInvocation handlerInvocation = new HandlerInvocation();
    private Handler timerHandler = new Handler();
    private final int updateInterval = 500;//������ˢ�¼��
    private MediaPlayerState mediaPlayerState = MediaPlayerState.STOPPED;
    private enum MediaPlayerState{STOPPED, PLAYING, PAUSED}//����״̬
    //���ŵ�ַ
    private Uri mUri = null;
    List<String> urllist;  
    ArrayAdapter<String> adapter;//������
    ArrayList<String> titleList;
    private UPnPService dlnaService;

	private ServiceConnection dlnaServiceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName name, IBinder service) {
			dlnaService = ((UPnPService.DlnaServiceBinder) service)
					.getService();
		}
		
		public void onServiceDisconnected(ComponentName name) {
			dlnaService = null;
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }
    
    @Override
    public void onCompletion(MediaPlayer mp) {
    	ProseekBar.setProgress(0);
    }

    @Override
	protected void onResume() {
		super.onResume();
		
		mUri = this.getIntent().getData();
		//UPnPService����δ����
		if (dlnaService == null) {
		//	dlnaServiceDelayedStart = true;
		}
		//����ļ���ַ
		if (mUri != null) {
			//���õ�ǰ�ļ���
			filename = mUri.toString();
			//��֤�Ƿ������ͬ���ļ�
			boolean bFileIn = false;
			if(!urllist.isEmpty()){
				bFileIn = urllist.contains(filename);
			}
				
			if(!bFileIn){
				//����������б�
				urllist.add(filename);
				titleList.add(this.getIntent().getExtras().getString("title"));
				UserInfo lds = ((UserInfo) getApplicationContext());
				lds.setPlayUrl(urllist);
				lds.setPlayTitle(titleList);
				adapter.notifyDataSetChanged();
			}	

		}
		else {
			//�ǵ㲥��ת
//			shortcutResume = true;
		}
	}
    
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mediaPlayerState!= MediaPlayerState.STOPPED) {
			mediaPlayer.stop();
    		mediaPlayer.release();
    		mediaPlayerState = MediaPlayerState.STOPPED;
    		timerHandler.removeCallbacks(handlerInvocation);
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
				AlertDialog.Builder NotifyDialog= new AlertDialog.Builder(PlayerActivity.this);
	
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
				Intent intent = new Intent().setClass(PlayerActivity.this,
						ClassTeachActivity.class);
				intent.setData(Uri.parse("0"));
				PlayerActivity.this.startActivity(intent);
				break;
 			//---------------������Ϣ----------------------//
				
			}
			
		}
		};
    
	 public void initView(){
		 	setContentView(R.layout.playing); 

	        surfaceView = (SurfaceView)findViewById(R.id.videoView);
//	        playButton = (Button)findViewById(R.id.playbutton);
	        pauseButton = (Button)findViewById(R.id.pauseplaybutton);
	        resetButton = (Button)findViewById(R.id.resetplaybutton);
	        stopButton = (Button)findViewById(R.id.stopplaybutton);
	        fastforwardButton = (Button)findViewById(R.id.fastforwardbutton);
	        fastbackButton = (Button)findViewById(R.id.fastbackbutton);
	        nextsongButton = (Button)findViewById(R.id.nextsongbutton);
	        previoussongButton = (Button)findViewById(R.id.previoussongbutton);
	        displaysonglistButton = (Button)findViewById(R.id.displaylistbutton);
	        fullscreenButton= (Button)findViewById(R.id.fullscreen);
	        ProseekBar = (SeekBar)findViewById(R.id.seekBar);
	        
	        
//	        playButton.setOnClickListener(this);
	        pauseButton.setOnClickListener(this);
	        resetButton.setOnClickListener(this);
	        stopButton.setOnClickListener(this);
	        fastforwardButton.setOnClickListener(this);
	        fastbackButton.setOnClickListener(this);
	        nextsongButton.setOnClickListener(this);
	        previoussongButton.setOnClickListener(this);
	        displaysonglistButton.setOnClickListener(this);
	        fullscreenButton.setOnClickListener(new fullscreenlistener());
	        
	        ProseekBar.setOnSeekBarChangeListener(new sbLis());


	        /*��������Surface��ά���Լ��Ļ����������ǵȴ���Ļ����Ⱦ���潫�������͵��û���ǰ*/

	        
	        /*SurfaceHolder����4��type��
	        	SURFACE_TYPE_NORMAL����RAM����ԭ�����ݵ���ͨSurface
	        	SURFACE_TYPE_HARDWARE��������DMA(Direct memory access )�����Ӳ�����ٵ�Surface�����Ѿ�����
	        	SURFACE_TYPE_GPU��������GPU���ٵ�Surface�����Ѿ�����
	        	SURFACE_TYPE_PUSH_BUFFERS��������Surface������ԭ�����ݣ�Surface�õ������������������ṩ��
	        	���������������SURFACE_TYPE_PUSH_BUFFERS��
	        	����Ҫ�ܿ����Ӧʱ������ҪSURFACE_TYPE_PUSH_BUFFERS��type������
	        	������surface�Ͳ����Լ�ͨ��RAM�������ˣ������ĵط������棬��camera��VedioView�ȡ�
	        	�������ϱȽ϶�Ľ��SurfaceViewֻ������û��ͼ��Ľ���������������˴˷�����
	        	��surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	        	���ͱ�����SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS
	        	Ϊ�����⿨����MediaPlayer.prepareSync()�첽���أ�
	        	��onPrepared�д��������ĺ����¼���MediaPlayer.start����*/

	        //this.surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	        //��Ϊ�Դ���Ƶ����������ram��ʽ
	        this.surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
	        /*surfaceHolderĬ�ϵĸ�ʽPixelFormat.OPAQUE��
	         * ʱ���ϵ�Ч��PixelFormat.OPAQUE��ߣ�
	         * PixelFormat.RGB_565�Ե͵㣬
	         * PixelFormat.RGBA_8888��Ҫ�ͺܶࡣ
			��ALG��demo�У�surfaceHolder�ĸ�ʽΪPixelFormat.OPAQUE��FPS�ȶ���֡��38���ҡ�
				surfaceHolder�ĸ�ʽΪPixelFormat.RGB_565��FPS�ȶ���֡��37���ҡ�
				surfaceHolder�ĸ�ʽΪPixelFormat.RGBA_8888��FPS�ȶ���֡��19���ҡ�*/
	        this.surfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
	        this.surfaceView.getHolder().setKeepScreenOn(true);
	        this.surfaceView.getHolder().addCallback(new SurfaceListener());
	       
			BaseView();
			initPopupPlayListWnd();
	 }
	 
	 public void initNetwork(){
		 super.initNetwork();
		 Intent service = new Intent(UPnPService.BIND_SERVICE);
		 this.getApplicationContext().bindService(service,
				dlnaServiceConnection, Context.BIND_AUTO_CREATE);
	 }
	 //ȫ����ť
	 class fullscreenlistener implements android.view.View.OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(mediaPlayerState != MediaPlayerState.STOPPED)
			{	mediaPlayer.stop();
				mediaPlayer.release();
				mediaPlayerState = MediaPlayerState.STOPPED;
			}
				timerHandler.removeCallbacks(handlerInvocation);
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString("playurl", filename);  
	            intent.putExtras(bundle);
				intent.setClass(PlayerActivity.this,PlayerActivityfullscreen.class);
				startActivity(intent);				
		}
		 
	 }
	 

	 
	//������
	 class  sbLis implements OnSeekBarChangeListener {
	   int progress; 
	  @Override
	  public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
	  if (mediaPlayerState==MediaPlayerState.PLAYING) {
	  this.progress = progress * mediaPlayer.getDuration()/ProseekBar.getMax();  
	}  
	  }     
	  @Override
	  public void onStartTrackingTouch(SeekBar seekBar) {    
	  }     
	  @Override
	  public void onStopTrackingTouch(SeekBar seekBar) {
	  if (mediaPlayerState==MediaPlayerState.PLAYING) {
	  mediaPlayer.seekTo(progress);
	}
	  }     
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
		
    	public void onClick(View v) {
    		try {
    			switch (v.getId()) {
//    				case R.id.playbutton://���Բ��Ű�ť
//    					 
//    					break;
    			case R.id.pauseplaybutton://������ͣ��ť
					if(mediaPlayerState == MediaPlayerState.STOPPED){
						this.surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
						this.surfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
				        this.surfaceView.getHolder().setKeepScreenOn(true);
				        this.surfaceView.getHolder().addCallback(new SurfaceListener());
						play(); 
						pauseButton.setText("��ͣ");
					}
					else if(mediaPlayer.isPlaying()){
						mediaPlayerpause();
						pauseButton.setText("����");    					
					}else{
						mediaPlayer.start();
						mediaPlayerState = MediaPlayerState.PLAYING;
						timerHandler.postDelayed(handlerInvocation, updateInterval);
						pauseButton.setText("��ͣ");
					}
					break;
    				case R.id.resetplaybutton://�������²��Ű�ť
    						//play();
    						mediaPlayer.seekTo(0);
    						mediaPlayer.start();
    		            	mediaPlayerState = MediaPlayerState.PLAYING;
    		            	timerHandler.postDelayed(handlerInvocation, updateInterval);
    		            	pauseButton.setText("��ͣ");
    					break;
    				case R.id.stopplaybutton://����ֹͣ��ť
    					if(mediaPlayerState != MediaPlayerState.STOPPED) 
    			    		{pauseButton.setText("����");
    						mediaPlayerstop();}
    					break; 
    				case R.id.fastforwardbutton://���Կ����ť
    					if(mediaPlayer.isPlaying()) 
    						fastforward();
    					break;
    				case R.id.fastbackbutton://���Կ��˰�ť
    					if(mediaPlayer.isPlaying()) 
    						fastback();
    					break;
    				case R.id.nextsongbutton://������һ����ť
    						nextsong();
    					break;
    				case R.id.previoussongbutton://������һ����ť
    						previoussong();
    					break;    					
    				case R.id.displaylistbutton://��ʾ�б�
    						displaylist();
    					break;
    			}
    		} catch (Exception e) {
    			Log.e(TAG, e.toString());
  				}
    	}
    	
	

    private class SurfaceListener implements SurfaceHolder.Callback{
    	@Override
    	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    	}
    	@Override
    	public void surfaceCreated(SurfaceHolder holder) {//������onResume()�󱻵���
    		Log.i(TAG, "surfaceCreated()");
    		if (filename!=null) {
			try {
				play();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
			
  }
    	@Override
    	public void surfaceDestroyed(SurfaceHolder holder) {
    		Log.i(TAG, "surfaceDestroyed()");
    	}      
    }
    	
    private class HandlerInvocation extends Thread
    {	

    	@Override
    	public void run() {
    		// TODO Auto-generated method stub
    			displayProgress();
				timerHandler.postDelayed(handlerInvocation, updateInterval);
    	}
    };

    private void displayProgress() {
       
        int progressValue = 0;
        if(mediaPlayer.getDuration()>0){
                progressValue = ProseekBar.getMax()*mediaPlayer.getCurrentPosition()/mediaPlayer.getDuration();
        }
        ProseekBar.setProgress(progressValue);
       
    }

    /**
     * ������Ƶ
     */

    	private void play() throws IOException {

    		if (mediaPlayerState == MediaPlayerState.STOPPED)
            {
    			mediaPlayer = new MediaPlayer();
    			//mediaPlayer.reset();		    
    	    	//mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); 
    		    /*׼����ɺ�ſ��Բ���,��������ļ��ر����ߴ����ϻ�õ���Դ ,
    		     * ��������ȴ�ʱ�����,��ɶ���,�����Ļ��͵���this.mediaPlayer.prepareAsync(),
    		     * Ȼ������Ƿ�׼������ڿ�ʼ*/
    	    	//mediaPlayer.setDataSource(this, Uri.parse(mUri));
    	    	//mediaPlayer.setDataSource(this, mUri);
    	    	mediaPlayer.setDataSource(filename);
    	    	mediaPlayer.setDisplay(surfaceView.getHolder());//����Ƶ���������SurfaceView     	
    	    	mediaPlayer.prepare();
//    	    	surfaceView.getHolder().setFixedSize(mediaPlayer.getVideoWidth(),mediaPlayer.getVideoHeight());
//    	    	int  videoHeight= mediaPlayer.getVideoHeight();     
//    	    	int videoWidth = mediaPlayer.getVideoWidth(); 
//    	    	int screenWidth = getWindowManager().getDefaultDisplay().getWidth(); 
//    	    	android.view.ViewGroup.LayoutParams lp = surfaceView.getLayoutParams(); 
//    	    	lp.height = (int) (((float)videoHeight / (float)videoWidth) * (float)screenWidth);  
//    	    	surfaceView.setLayoutParams(lp);
    	    	mediaPlayer.start();
    	    	displayProgress();
    	    	mediaPlayerState = MediaPlayerState.PLAYING;
    	    	timerHandler.postDelayed(handlerInvocation, updateInterval); 
            }
            else{
            	mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
            	mediaPlayer.start();
            	mediaPlayerState = MediaPlayerState.PLAYING;
            	timerHandler.postDelayed(handlerInvocation, updateInterval);
            }  
    		if(filename!=null)pauseButton.setText("��ͣ");
    	
    }  
    	
    protected void mediaPlayerpause() {//������Activity���򿪣�ֹͣ����
    		
    			position = mediaPlayer.getCurrentPosition();//�õ�����λ��
    			mediaPlayer.pause();
    			mediaPlayerState = MediaPlayerState.PAUSED;
    			timerHandler.removeCallbacks(handlerInvocation);    			
    			displayProgress();
    		
    }  

    
    protected void mediaPlayerstop() { 
    		mediaPlayer.stop();
    		mediaPlayer.release();
    		mediaPlayerState = MediaPlayerState.STOPPED;
    		timerHandler.removeCallbacks(handlerInvocation);
    		ProseekBar.setProgress(0);

    }
   
  
    private void fastforward()
    {
            if(mediaPlayerState != MediaPlayerState.STOPPED){
            	mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+3000);
            	mediaPlayer.start();
                timerHandler.postDelayed(handlerInvocation, updateInterval);
            }
            else{
                    timerHandler.removeCallbacks(handlerInvocation);
                    displayProgress();
            }
    }
   
    private void fastback()
    {
            if(mediaPlayerState != MediaPlayerState.STOPPED){
            	mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-3000);
            	mediaPlayer.start();
                    timerHandler.postDelayed(handlerInvocation, updateInterval);
            }
            else{
                    timerHandler.removeCallbacks(handlerInvocation);
                    displayProgress();
            }
    }
    
    private void previoussong() {
		// TODO Auto-generated method stub
    	int index = urllist.indexOf(filename);
		if(index!=-1 && index!= 0){
			filename = urllist.get(index-1);
			Toast.makeText(getApplicationContext(), titleList.get(index),
					Toast.LENGTH_SHORT).show();
			//ֹͣ��ǰ����
			if(mediaPlayerState != MediaPlayerState.STOPPED){
	    		mediaPlayer.stop();
	    		mediaPlayer.release();
	    		mediaPlayerState = MediaPlayerState.STOPPED;
	    		//timerHandler.removeCallbacks(handlerInvocation);
			}
			//��ʼ����
			try {
				play();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}else {
				Toast.makeText(getApplicationContext(), "�Ѿ��ǵ�һ��",
						Toast.LENGTH_SHORT).show();
			}
	}

    private void nextsong() {
		// TODO Auto-generated method stub
			int index = urllist.indexOf(filename);
			if(index!=-1 && index!= urllist.size()-1){
				filename = urllist.get(index+1);
				Toast.makeText(getApplicationContext(), titleList.get(index),
						Toast.LENGTH_SHORT).show();
				//ֹͣ��ǰ����
				if(mediaPlayerState != MediaPlayerState.STOPPED){
		    		mediaPlayer.stop();
		    		mediaPlayer.release();
		    		mediaPlayerState = MediaPlayerState.STOPPED;
		    		//timerHandler.removeCallbacks(handlerInvocation);
				}
				//��ʼ����
				try {
					play();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				}else {
					Toast.makeText(getApplicationContext(), "�Ѿ������һ��",
							Toast.LENGTH_SHORT).show();
				}
		
	}

	private void displaylist() {
		// TODO Auto-generated method stub
		if(popupPlayListWindow.isShowing()){
			popupPlayListWindow.dismiss();
		}else
//			popupPlayListWindow.showAsDropDown(displaysonglistButton, 0, -lvPopupPlayListWindow.getHeight()-displaysonglistButton.getHeight());  
		popupPlayListWindow.showAsDropDown(displaysonglistButton, 0, -500); 
	}

    
	public void initPopupPlayListWnd(){
		// ����������ʾ�����б�˵���PopupWindow����
					View popPlayListView = View.inflate(this, R.layout.item_play_list, null);
					popupPlayListWindow = new PopupWindow(popPlayListView, LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					// ʹPopupWindow���Ի�ý��㣬�Ա����ͨ���켣����ϡ��¡����Ҽ������Ʋ˵���,����͸������ɫ
					popupPlayListWindow.setFocusable(true);
					popupPlayListWindow.setOutsideTouchable(true);
					ColorDrawable dw = new ColorDrawable(0xb0000000);
					popupPlayListWindow.setBackgroundDrawable(dw);
					
					//��ʼ�������б�ListView
					lvPopupPlayListWindow = (ListView) popPlayListView.findViewById(R.id.playlistview);						
		
		//��ʼ������
					UserInfo lds = ((UserInfo) getApplicationContext());
					urllist = lds.getPlayUrl();
					titleList = lds.getPlayTitle();
					if(titleList == null){
						urllist = new ArrayList<String>();
						titleList=new ArrayList<String>();
					}
					adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,titleList);  
		 //��Ӳ�����ʾ
					lvPopupPlayListWindow.setAdapter(adapter);
        //���ü��������Ե�����м���
					lvPopupPlayListWindow.setOnItemClickListener(new OnItemClickListener() {
						public void onItemClick(AdapterView<?> parent, View view, int position,
								long id){ 
							// TODO Auto-generated method stub

							filename = urllist.get(position);
							//ֹͣ��ǰ����
							if(mediaPlayerState != MediaPlayerState.STOPPED){
					    		mediaPlayer.stop();
					    		mediaPlayer.release();
					    		mediaPlayerState = MediaPlayerState.STOPPED;
					    		//timerHandler.removeCallbacks(handlerInvocation);
							}
							//��ʼ����
							try {
								play();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							popupPlayListWindow.dismiss();
			}
	    }
        );
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
		tvSystemState.setText("ý�岥����");
		
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
