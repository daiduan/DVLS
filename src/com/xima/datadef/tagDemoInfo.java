package com.xima.datadef;

import com.xima.utility.DataConverter;

public class tagDemoInfo {
	byte[] Name = new byte[20];// ѧ������
	byte IPIndex;// ��ʾ��ѧ����IP�����һλ
	byte State;// ѧ��ʾ��״̬��0��û����ʾ���б��� 1����ʾ���б���

	private String strName;
	private int iIPIndex;
	private boolean bState;

	public tagDemoInfo() {

	}

	public tagDemoInfo(byte[] bytes) {
		System.arraycopy(bytes, 0, this.Name, 0, 20);
		System.arraycopy(bytes, 20, this.IPIndex, 0, 1);
		System.arraycopy(bytes, 21, this.State, 0, 1);
		decode();
	}

	public void decode() {
		strName = DataConverter.trimString(Name);
		iIPIndex = IPIndex & 0xFF;
		if ((State & 0xff) == 0) {
			bState = false;
		} else {
			bState = true;
		}
	}

}
