package com.xima.datadef;

import com.xima.utility.DataConverter;

public class tagStuList {

	byte[] Name = new byte[20];//ѧ����ʾ������
	byte   IPIndex;//ѧ��IP�����һλ
	public byte   Sex;//�Ա�0�� 1Ů
	
	public String StrName;
	public int IP;
	
	public tagStuList(byte[] bytes){
		System.arraycopy(bytes, 0, Name, 0, 20);
		IPIndex = bytes[20];
		Sex = bytes[21];
		//ת������
		StrName = DataConverter.trimString(Name);
		IP = IPIndex & 0xFF;;
	}

}
