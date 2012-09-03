package com.xima.ui;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.xima.datadef.UserInfo;
import com.xima.datadef.tagCommandCode;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;

public class PlayerActivityfullscreen extends BaseActivity implements OnCompletionListener,
		OnErrorListener, OnInfoListener, OnPreparedListener,
		OnSeekCompleteListener, OnVideoSizeChangedListener,
		SurfaceHolder.Callback, MediaController.MediaPlayerControl {

	Display currentDisplay;
	SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;

	MediaPlayer mediaPlayer;
	MediaController mediaController;

	int videoWidth = 0;
	int videoHeight = 0;
	boolean readyToPlay = false;
	String playURI;

	public final static String LOGTAG = "Fullscreen";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//全屏
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//标题去掉
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.playingfullscreen);

		surfaceView = (SurfaceView) findViewById(R.id.videoView02);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		mediaPlayer = new MediaPlayer();

		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnErrorListener(this);
		mediaPlayer.setOnInfoListener(this);
		mediaPlayer.setOnPreparedListener(this);
		mediaPlayer.setOnSeekCompleteListener(this);
		mediaPlayer.setOnVideoSizeChangedListener(this);

		mediaController = new MediaController(this);
		Bundle bundle = this.getIntent().getExtras();
		playURI=bundle.getString("playurl");//当前播放文件的路径
		if (playURI!=null) {
		  try {
			mediaPlayer.setDataSource(playURI);
		} catch (IllegalArgumentException e) {
			Log.v(LOGTAG, e.getMessage());
			finish();
		} catch (IllegalStateException e) {
			Log.v(LOGTAG, e.getMessage());
			finish();
		} catch (IOException e) {
			Log.v(LOGTAG, e.getMessage());
			finish();
		}
		}
		
		currentDisplay = getWindowManager().getDefaultDisplay();
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
						AlertDialog.Builder NotifyDialog= new AlertDialog.Builder(PlayerActivityfullscreen.this);
			
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
						Intent intent = new Intent().setClass(PlayerActivityfullscreen.this,
								ClassTeachActivity.class);
						intent.setData(Uri.parse("0"));
						PlayerActivityfullscreen.this.startActivity(intent);
						break;
					//---------------自有消息----------------------//
						
					}
				}
				};

	@Override
	protected void onPause() {
		super.onPause();
		if( mediaPlayer !=null ) {
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if( mediaPlayer !=null ) {
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if( mediaPlayer !=null ) {
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mediaController.isShowing()) {
			mediaController.hide();
		} else {
			mediaController.show(3000);
		}
		return false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		Log.v(LOGTAG, "surfaceChanged Called");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		Log.v(LOGTAG, "surfaceCreated Called");
		mediaPlayer.setDisplay(holder);
		try {
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
			//Log.v(LOGTAG, e.getMessage());
			finish();
		} catch (IOException e) {
			//Log.v(LOGTAG, e.getMessage());
			finish();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		Log.v(LOGTAG, "surfaceDestroyed Called");
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		// TODO Auto-generated method stub
		Log.v(LOGTAG, "onVideoSizeChanged Called");
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		// TODO Auto-generated method stub
		Log.v(LOGTAG, "onSeekComplete Called");
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub
		Log.v(LOGTAG, "onPrepared Called");
		videoWidth = mp.getVideoWidth();
		videoHeight = mp.getVideoHeight();
		if (videoWidth > currentDisplay.getWidth()
				|| videoHeight > currentDisplay.getHeight()) {
			float heightRatio = (float) videoHeight
					/ (float) currentDisplay.getHeight();
			float widthRatio = (float) videoWidth
					/ (float) currentDisplay.getWidth();
			if (heightRatio > 1 || widthRatio > 1) {
				if (heightRatio > widthRatio) {
					videoHeight = (int) Math.ceil((float) videoHeight
							/ (float) heightRatio);
					videoWidth = (int) Math.ceil((float) videoWidth
							/ (float) heightRatio);
				} else {
					videoHeight = (int) Math.ceil((float) videoHeight
							/ (float) widthRatio);
					videoWidth = (int) Math.ceil((float) videoWidth
							/ (float) widthRatio);
				}
			}
		}
		//surfaceView.setLayoutParams(new LinearLayout.LayoutParams(videoWidth,videoHeight));
		if (playURI!=null) {
			mp.start();
		}
		

		mediaController.setMediaPlayer(this);
		mediaController.setAnchorView(this
				.findViewById(R.id.videoView02));
		mediaController.setEnabled(true);
		mediaController.show(3000);
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int whatInfo, int extra) {
		// TODO Auto-generated method stub
		if (whatInfo == MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING) {
			Log.v(LOGTAG, "Media Info, Media Info Bad Interleaving " + extra);
		} else if (whatInfo == MediaPlayer.MEDIA_INFO_NOT_SEEKABLE) {
			Log.v(LOGTAG, "Media Info, Media Info Not Seekable " + extra);
		} else if (whatInfo == MediaPlayer.MEDIA_INFO_UNKNOWN) {
			Log.v(LOGTAG, "Media Info, Media Info Unknown " + extra);
		} else if (whatInfo == MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING) {
			Log.v(LOGTAG, "MediaInfo, Media Info Video Track Lagging " + extra);
		} else if (whatInfo == MediaPlayer.MEDIA_INFO_METADATA_UPDATE) {
			Log.v(LOGTAG, "MediaInfo, Media Info Metadata Update " + extra);
		}
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		Log.v(LOGTAG, "onCompletion Called");
		finish();
	}

	@Override
	public boolean onError(MediaPlayer mp, int whatError, int extra) {
		// TODO Auto-generated method stub
		Log.v(LOGTAG, "onError Called");
		if (whatError == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
			Log.v(LOGTAG, "Media Error, Server Died " + extra);
		} else if (whatError == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
			Log.v(LOGTAG, "Media Error, Error Unknown " + extra);
		}
		return false;
	}

	@Override
	public boolean canPause() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean canSeekForward() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public int getBufferPercentage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCurrentPosition() {
		// TODO Auto-generated method stub
		return mediaPlayer.getCurrentPosition();
	}

	@Override
	public int getDuration() {
		// TODO Auto-generated method stub
		return mediaPlayer.getDuration();
	}

	@Override
	public boolean isPlaying() {
		// TODO Auto-generated method stub
		return mediaPlayer.isPlaying();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
		}
	}

	@Override
	public void seekTo(int pos) {
		// TODO Auto-generated method stub
		mediaPlayer.seekTo(pos);
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		mediaPlayer.start();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
