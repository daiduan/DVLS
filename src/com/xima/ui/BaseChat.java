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
	protected GridView gvPopupWindow;// ����
	protected PopupWindow popup;//������
	protected EditText ChatInput;//������
	protected TextView ChatWindow;//��Ϣ��
	protected ScrollView sv;
	//��ǰIP��
	int CurrentIPNum;
	
	//������ť
		protected Button chat_send;//����
		protected Button chat_emotion;//����
		
	ImageGetter imageGetter = new ImageGetter() {
		public Drawable getDrawable(String source) {
			int id = Integer.parseInt(source);
			Drawable d = getResources().getDrawable(id);
			d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
			return d;
		}
	};
		
	// ��ʼ����ͼ
		public void initView() {
			initPopupWnd();//PopupWindow��ʼ��
		}
		
	public void initPopupWnd(){
		// ����������ʾ����˵���PopupWindow����
					View popView = View.inflate(this, R.layout.item_gridview_emotion, null);
					popup = new PopupWindow(popView, LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					// ʹPopupWindow���Ի�ý��㣬�Ա����ͨ���켣����ϡ��¡����Ҽ������Ʋ˵���,����͸������ɫ
					popup.setFocusable(true);
					popup.setOutsideTouchable(true);
					ColorDrawable dw = new ColorDrawable(0xb0000000);
					popup.setBackgroundDrawable(dw);
					
		//��ʼ������˵��е�ͼ��GridView
					gvPopupWindow = (GridView) popView.findViewById(R.id.emgridview);
					// ΪGridView�ṩ���ݵ�Adapter����
					gvPopupWindow.setAdapter(new ImageAdapter(this));
					//���������
					gvPopupWindow.setOnItemClickListener((OnItemClickListener) this);
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// ������GridView�е�ÿһ��ʱ���ȹرյ������ڣ�Ȼ�������Ϣ
			popup.dismiss();

			//ת��������Ϣ
			CharSequence cs = Html.fromHtml("<img src='" + mThumbIds[position]
					+ "'/>", imageGetter, null);
			//�������������ݲ������
			 int index = ChatInput.getSelectionStart();
			 Editable etb =  ChatInput.getText();
			 int length = etb.length();
			 //���������������м�������
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
				//��������������¼
					SpannableString ss = new SpannableString("��˵��");
					ss.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, ss.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					ChatWindow.append(ss);
			  }
				ChatWindow.append(Html.fromHtml(StrChat,imageGetter, null));
				sv.pageScroll(View.FOCUS_DOWN);
		  }
	
	//-------------------------------------------------------------------------------------------
		/*
		 * ����InputWindow��Ϣ�����ش���+�������������ڳ�Ա 
		 */
		void send() {
			Editable etb = ChatInput.getText();
			int length = etb.length();
			if(length == 0)
			{
				ChatInput.setError("�������ݲ���Ϊ�գ�����������");
			}else if(CurrentIPNum == 0){
				ChatInput.setError("��������û��������Ա");
			}else if(CurrentIPNum == -1){
				ChatInput.setError("�㻹û�м���������");
			}else{
				etb.append("\n");
				String StrChat = FilterHtml((Html.toHtml(etb)));
				//���������
				add2InputWindow(StrChat,true);
				//����������Ϣ
				sendout(etb);
				//���
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
