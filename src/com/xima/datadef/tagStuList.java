package com.xima.datadef;

import com.xima.utility.DataConverter;

public class tagStuList {

	byte[] Name = new byte[20];//学生显示的姓名
	byte   IPIndex;//学生IP的最后一位
	public byte   Sex;//性别：0男 1女
	
	public String StrName;
	public int IP;
	
	public tagStuList(byte[] bytes){
		System.arraycopy(bytes, 0, Name, 0, 20);
		IPIndex = bytes[20];
		Sex = bytes[21];
		//转换数据
		StrName = DataConverter.trimString(Name);
		IP = IPIndex & 0xFF;;
	}

}
