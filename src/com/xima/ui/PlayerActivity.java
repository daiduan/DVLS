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
    private int position;//记录播放位置
//    private Button playButton;//播放按钮
    private Button pauseButton;//暂停按钮
    private Button resetButton;//重新播放按钮
    private Button stopButton;//快退按钮
    private Button fastforwardButton;//快进按钮
    private Button fastbackButton;//快退按钮
    private Button displaysonglistButton;//显示歌曲列表按钮
    private Button fullscreenButton;//全屏按钮
    private SeekBar ProseekBar;//音量条，进度条
    private Button nextsongButton;//下一曲按钮
    private Button previoussongButton;//上下一曲按钮
    private PopupWindow popupPlayListWindow;
    private ListView lvPopupPlayListWindow;
    private HandlerInvocation handlerInvocation = new HandlerInvocation();
    private Handler timerHandler = new Handler();
    private final int updateInterval = 500;//进度条刷新间隔
    private MediaPlayerState mediaPlayerState = MediaPlayerState.STOPPED;
    private enum MediaPlayerState{STOPPED, PLAYING, PAUSED}//播放状态
    //播放地址
    private Uri mUri = null;
    List<String> urllist;  
    ArrayAdapter<String> adapter;//适配器
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
		//UPnPService服务未启动
		if (dlnaService == null) {
		//	dlnaServiceDelayedStart = true;
		}
		//获得文件地址
		if (mUri != null) {
			//设置当前文件播
			filename = mUri.toString();
			//验证是否存在相同的文件
			boolean bFileIn = false;
			if(!urllist.isEmpty()){
				bFileIn = urllist.contains(filename);
			}
				
			if(!bFileIn){
				//添加至播放列表
				urllist.add(filename);
				titleList.add(this.getIntent().getExtras().getString("title"));
				UserInfo lds = ((UserInfo) getApplicationContext());
				lds.setPlayUrl(urllist);
				lds.setPlayTitle(titleList);
				adapter.notifyDataSetChanged();
			}	

		}
		else {
			//非点播跳转
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
				AlertDialog.Builder NotifyDialog= new AlertDialog.Builder(PlayerActivity.this);
	
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
				Intent intent = new Intent().setClass(PlayerActivity.this,
						ClassTeachActivity.class);
				intent.setData(Uri.parse("0"));
				PlayerActivity.this.startActivity(intent);
				break;
 			//---------------自有消息----------------------//
				
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


	        /*下面设置Surface不维护自己的缓冲区，而是等待屏幕的渲染引擎将内容推送到用户面前*/

	        
	        /*SurfaceHolder中有4个type：
	        	SURFACE_TYPE_NORMAL：用RAM缓存原生数据的普通Surface
	        	SURFACE_TYPE_HARDWARE：适用于DMA(Direct memory access )引擎和硬件加速的Surface，现已经废弃
	        	SURFACE_TYPE_GPU：适用于GPU加速的Surface，现已经废弃
	        	SURFACE_TYPE_PUSH_BUFFERS：表明该Surface不包含原生数据，Surface用到的数据由其他对象提供。
	        	下面具体来介绍下SURFACE_TYPE_PUSH_BUFFERS。
	        	当需要很快的响应时，就需要SURFACE_TYPE_PUSH_BUFFERS的type参数，
	        	这样改surface就不用自己通过RAM来缓存了，其他的地方来缓存，如camera和VedioView等。
	        	例如网上比较多的解决SurfaceView只有声音没有图像的解决方案，就是用了此方法，
	        	将surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	        	类型必须是SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS
	        	为例避免卡死，MediaPlayer.prepareSync()异步加载，
	        	在onPrepared中处理加载完的后续事件（MediaPlayer.start）。*/

	        //this.surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	        //华为自带视频播放器采用ram方式
	        this.surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
	        /*surfaceHolder默认的格式PixelFormat.OPAQUE。
	         * 时间上的效率PixelFormat.OPAQUE最高，
	         * PixelFormat.RGB_565略低点，
	         * PixelFormat.RGBA_8888则要低很多。
			在ALG的demo中：surfaceHolder的格式为PixelFormat.OPAQUE，FPS稳定在帧数38左右。
				surfaceHolder的格式为PixelFormat.RGB_565，FPS稳定在帧数37左右。
				surfaceHolder的格式为PixelFormat.RGBA_8888，FPS稳定在帧数19左右。*/
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
	 //全屏按钮
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
	 

	 
	//进度条
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
    		try {
    			switch (v.getId()) {
//    				case R.id.playbutton://来自播放按钮
//    					 
//    					break;
    			case R.id.pauseplaybutton://来自暂停按钮
					if(mediaPlayerState == MediaPlayerState.STOPPED){
						this.surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
						this.surfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
				        this.surfaceView.getHolder().setKeepScreenOn(true);
				        this.surfaceView.getHolder().addCallback(new SurfaceListener());
						play(); 
						pauseButton.setText("暂停");
					}
					else if(mediaPlayer.isPlaying()){
						mediaPlayerpause();
						pauseButton.setText("播放");    					
					}else{
						mediaPlayer.start();
						mediaPlayerState = MediaPlayerState.PLAYING;
						timerHandler.postDelayed(handlerInvocation, updateInterval);
						pauseButton.setText("暂停");
					}
					break;
    				case R.id.resetplaybutton://来自重新播放按钮
    						//play();
    						mediaPlayer.seekTo(0);
    						mediaPlayer.start();
    		            	mediaPlayerState = MediaPlayerState.PLAYING;
    		            	timerHandler.postDelayed(handlerInvocation, updateInterval);
    		            	pauseButton.setText("暂停");
    					break;
    				case R.id.stopplaybutton://来自停止按钮
    					if(mediaPlayerState != MediaPlayerState.STOPPED) 
    			    		{pauseButton.setText("播放");
    						mediaPlayerstop();}
    					break; 
    				case R.id.fastforwardbutton://来自快进按钮
    					if(mediaPlayer.isPlaying()) 
    						fastforward();
    					break;
    				case R.id.fastbackbutton://来自快退按钮
    					if(mediaPlayer.isPlaying()) 
    						fastback();
    					break;
    				case R.id.nextsongbutton://来自下一曲按钮
    						nextsong();
    					break;
    				case R.id.previoussongbutton://来自上一曲按钮
    						previoussong();
    					break;    					
    				case R.id.displaylistbutton://显示列表
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
    	public void surfaceCreated(SurfaceHolder holder) {//方法在onResume()后被调用
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
     * 播放视频
     */

    	private void play() throws IOException {

    		if (mediaPlayerState == MediaPlayerState.STOPPED)
            {
    			mediaPlayer = new MediaPlayer();
    			//mediaPlayer.reset();		    
    	    	//mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); 
    		    /*准备完成后才可以播放,另外如果文件特别大或者从网上获得的资源 ,
    		     * 会在这里等待时间过长,造成堵塞,这样的话就得用this.mediaPlayer.prepareAsync(),
    		     * 然后监听是否准备完毕在开始*/
    	    	//mediaPlayer.setDataSource(this, Uri.parse(mUri));
    	    	//mediaPlayer.setDataSource(this, mUri);
    	    	mediaPlayer.setDataSource(filename);
    	    	mediaPlayer.setDisplay(surfaceView.getHolder());//把视频画面输出到SurfaceView     	
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
    		if(filename!=null)pauseButton.setText("暂停");
    	
    }  
    	
    protected void mediaPlayerpause() {//当其他Activity被打开，停止播放
    		
    			position = mediaPlayer.getCurrentPosition();//得到播放位置
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
			//停止当前播放
			if(mediaPlayerState != MediaPlayerState.STOPPED){
	    		mediaPlayer.stop();
	    		mediaPlayer.release();
	    		mediaPlayerState = MediaPlayerState.STOPPED;
	    		//timerHandler.removeCallbacks(handlerInvocation);
			}
			//开始播放
			try {
				play();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}else {
				Toast.makeText(getApplicationContext(), "已经是第一个",
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
				//停止当前播放
				if(mediaPlayerState != MediaPlayerState.STOPPED){
		    		mediaPlayer.stop();
		    		mediaPlayer.release();
		    		mediaPlayerState = MediaPlayerState.STOPPED;
		    		//timerHandler.removeCallbacks(handlerInvocation);
				}
				//开始播放
				try {
					play();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				}else {
					Toast.makeText(getApplicationContext(), "已经是最后一个",
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
		// 创建用于显示播放列表菜单的PopupWindow对象。
					View popPlayListView = View.inflate(this, R.layout.item_play_list, null);
					popupPlayListWindow = new PopupWindow(popPlayListView, LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					// 使PopupWindow可以获得焦点，以便可以通过轨迹球或上、下、左、右键来控制菜单项,设置透明背景色
					popupPlayListWindow.setFocusable(true);
					popupPlayListWindow.setOutsideTouchable(true);
					ColorDrawable dw = new ColorDrawable(0xb0000000);
					popupPlayListWindow.setBackgroundDrawable(dw);
					
					//初始化播放列表ListView
					lvPopupPlayListWindow = (ListView) popPlayListView.findViewById(R.id.playlistview);						
		
		//初始化数据
					UserInfo lds = ((UserInfo) getApplicationContext());
					urllist = lds.getPlayUrl();
					titleList = lds.getPlayTitle();
					if(titleList == null){
						urllist = new ArrayList<String>();
						titleList=new ArrayList<String>();
					}
					adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,titleList);  
		 //添加并且显示
					lvPopupPlayListWindow.setAdapter(adapter);
        //设置监听器，对点击进行监听
					lvPopupPlayListWindow.setOnItemClickListener(new OnItemClickListener() {
						public void onItemClick(AdapterView<?> parent, View view, int position,
								long id){ 
							// TODO Auto-generated method stub

							filename = urllist.get(position);
							//停止当前播放
							if(mediaPlayerState != MediaPlayerState.STOPPED){
					    		mediaPlayer.stop();
					    		mediaPlayer.release();
					    		mediaPlayerState = MediaPlayerState.STOPPED;
					    		//timerHandler.removeCallbacks(handlerInvocation);
							}
							//开始播放
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
		tvSystemState.setText("媒体播放器");
		
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
