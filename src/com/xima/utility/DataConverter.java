package com.xima.utility;

import java.io.UnsupportedEncodingException;

public class DataConverter {

	public DataConverter() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * bytesת����ʮ�������ַ���
	 */
	public String bytes2HexStr(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				hs = hs + "0" + stmp;
			else
				hs = hs + stmp;
		}
		return hs.toUpperCase();
	}

	/**
	 * bytesת����int
	 */
	public static int bytes2int(byte[] bytes) {
		if (bytes.length > 4)
			return 0;// ���ݹ���
		int addr = bytes[0] & 0xFF;
		if (bytes.length >= 2)
			addr |= ((bytes[1] << 8) & 0xFF00);
		if (bytes.length >= 3)
			addr |= ((bytes[2] << 16) & 0xFF0000);
		if (bytes.length == 4)
			addr |= ((bytes[3] << 24) & 0xFF000000);
		return addr;
	}

	/**
	 * intתΪbyte[] ipInt -> byte[]
	 * 
	 * @param ipInt
	 * @return byte[]
	 */
	public static byte[] int2bytes(int ipInt) {
		byte[] ipAddr = new byte[4];
		ipAddr[3] = (byte) ((ipInt >>> 24) & 0xFF);
		ipAddr[2] = (byte) ((ipInt >>> 16) & 0xFF);
		ipAddr[1] = (byte) ((ipInt >>> 8) & 0xFF);
		ipAddr[0] = (byte) (ipInt & 0xFF);
		return ipAddr;
	}
	/*
	 * ��ȡ\0���ַ���
	 */
	public static String trimString(byte[] b ){
		String str;
		try {
		str = new String(b,"GBK");
		String t;
		int ps = str.indexOf("\0");
		if(ps!=-1){
			t=str.substring(0,ps);
			return t;
		}else {
			return str;
			
		}

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			return null;
		}
	} 
	/**
	 * bytesת����ʮ�������ַ���
	 */
	public static String byte2HexStr(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				hs = hs + "0" + stmp;
			else
				hs = hs + stmp;
		}
		return hs.toUpperCase();
	}

	/**
	 * Unicode תGBK��
	 * 
	 * @param s
	 *            �ַ������� &#27993;&#33756;
	 * @return
	 */
	public static String UnicodeToGBK2(String s) {
		String[] k = s.split(";");
		String rs = "";
		for (int i = 0; i < k.length; i++) {
			int strIndex = k[i].indexOf("&#");
			String newstr = k[i];
			if (strIndex > -1) {
				String kstr = "";
				if (strIndex > 0) {
					kstr = newstr.substring(0, strIndex);
					rs += kstr;
					newstr = newstr.substring(strIndex);
				}
				int m = Integer.parseInt(newstr.replace("&#", ""));
				char c = (char) m;
				rs += c;
			} else {
				rs += k[i];
			}
		}
		return rs;
	}

}
