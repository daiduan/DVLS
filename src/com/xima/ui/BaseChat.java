package com.xima.ui;

import com.xima.adapter.ImageAdapter;
import com.xima.datadef.CommandID;
import com.xima.utility.DataConverter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.Html.ImageGetter;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

public abstract class BaseChat extends BaseActivity implements CommandID, OnItemClickListener  {
	protected GridView gvPopupWindow;// 网格
	protected PopupWindow popup;//表情栏
	protected EditText ChatInput;//输入栏
	protected TextView ChatWindow;//消息栏
	protected ScrollView sv;
	//当前IP数
	int CurrentIPNum;
	
	//基本按钮
		protected Button chat_send;//发送
		protected Button chat_emotion;//表情
		
	ImageGetter imageGetter = new ImageGetter() {
		public Drawable getDrawable(String source) {
			int id = Integer.parseInt(source);
			Drawable d = getResources().getDrawable(id);
			d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
			return d;
		}
	};
		
	// 初始化视图
		public void initView() {
			initPopupWnd();//PopupWindow初始化
		}
		
	public void initPopupWnd(){
		// 创建用于显示表情菜单的PopupWindow对象。
					View popView = View.inflate(this, R.layout.item_gridview_emotion, null);
					popup = new PopupWindow(popView, LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					// 使PopupWindow可以获得焦点，以便可以通过轨迹球或上、下、左、右键来控制菜单项,设置透明背景色
					popup.setFocusable(true);
					popup.setOutsideTouchable(true);
					ColorDrawable dw = new ColorDrawable(0xb0000000);
					popup.setBackgroundDrawable(dw);
					
		//初始化表情菜单中的图表GridView
					gvPopupWindow = (GridView) popView.findViewById(R.id.emgridview);
					// 为GridView提供数据的Adapter对象
					gvPopupWindow.setAdapter(new ImageAdapter(this));
					//表情监听器
					gvPopupWindow.setOnItemClickListener((OnItemClickListener) this);
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// 当单击GridView中的每一项时，先关闭弹出窗口，然后添加消息
			popup.dismiss();

			//转换输入信息
			CharSequence cs = Html.fromHtml("<img src='" + mThumbIds[position]
					+ "'/>", imageGetter, null);
			//处理在聊天内容插入表情
			 int index = ChatInput.getSelectionStart();
			 Editable etb =  ChatInput.getText();
			 int length = etb.length();
			 //处理在聊天内容中间插入表情
			 if(index < length)
			 {
				 CharSequence cs1 = etb.subSequence(0, length);
				 CharSequence cs2 = cs1.subSequence(0, index);
				 CharSequence cs3 = cs1.subSequence(index, length);
				 ChatInput.setText(null);
				 ChatInput.append(cs2);
				 ChatInput.append(cs);
				 ChatInput.append(cs3);
			 }else
			 {
				 ChatInput.append(cs);
			 }
			 ChatInput.setSelection(index+1);
		}
		
	public void add2InputWindow(String StrChat,boolean Local){
			  if(Local){
				//添加至本地聊天记录
					SpannableString ss = new SpannableString("我说：");
					ss.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, ss.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					ChatWindow.append(ss);
			  }
				ChatWindow.append(Html.fromHtml(StrChat,imageGetter, null));
				sv.pageScroll(View.FOCUS_DOWN);
		  }
	
	//-------------------------------------------------------------------------------------------
		/*
		 * 发送InputWindow消息至本地窗口+发送至聊天室内成员 
		 */
		void send() {
			Editable etb = ChatInput.getText();
			int length = etb.length();
			if(length == 0)
			{
				ChatInput.setError("发送内容不能为空，请重新输入");
			}else if(CurrentIPNum == 0){
				ChatInput.setError("聊天室内没有其他成员");
			}else if(CurrentIPNum == -1){
				ChatInput.setError("你还没有加入聊天室");
			}else{
				etb.append("\n");
				String StrChat = FilterHtml((Html.toHtml(etb)));
				//添加至本地
				add2InputWindow(StrChat,true);
				//发送聊天信息
				sendout(etb);
				//清空
				ChatInput.setText(null);
			}
		}
		
		public void sendout(Editable etb){
			
		}

	
	public void EnterRoomSS(SpannableString ss){
		ss.setSpan(new ForegroundColorSpan(Color.RED), 0, ss.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		ChatWindow.append(ss);
		sv.pageScroll(View.FOCUS_DOWN);
	}
		  
	public static String FilterHtml(String str){
	          str = str.replaceAll("<(?!br|img)[^>]+>", "").trim();
	          return str;
	      }
}
