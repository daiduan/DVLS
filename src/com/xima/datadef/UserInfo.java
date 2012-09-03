package com.xima.datadef;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;

public class UserInfo extends Application {
	private tagCommandCode CmdC;
	private String ServerIP = null;
	private boolean Empty = true;
	
	public tagCommandCode getInstant(tagCommandCode CmdC) {
		if (this.CmdC == null) {
			this.CmdC = CmdC;
			Empty = false;
			return CmdC;
		} else {
			return this.CmdC;
		}
	}
	
	public boolean isEmpty(){
		return Empty;
	}
	
	public tagCommandCode getData() {
		return CmdC;
	}
	
	public void setIP(String ServerIP){
		this.ServerIP = ServerIP;
	}
	
	public String getIP(){
		return ServerIP;
	}
	
	private List<String> playlisturl = null;
	private ArrayList<String> playlisttitle = null;
	private List<String> txtlisturl = null;
	private ArrayList<String> txtlisttitle = null;

	public List<String> getPlayUrl(){
		return playlisturl;
	}
	
	public void setPlayUrl(List<String> urllist){
		this.playlisturl = urllist;
	}
	
	public ArrayList<String> getPlayTitle(){
		return playlisttitle;
	}
	
	public void setPlayTitle(ArrayList<String> playlisttitle){
		this.playlisttitle = playlisttitle;
	}
	
	public List<String> getTxtUrl(){
		return txtlisturl;
	}
	
	public void setTxtUrl(List<String> txtlistur){
		this.txtlisturl = txtlistur;
	}
	
	public ArrayList<String> getTxtTitle(){
		return txtlisttitle;
	}
	
	public void setTxtTitle(ArrayList<String> txtlisttitle){
		this.txtlisttitle = txtlisttitle;
	}

}
