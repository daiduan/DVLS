package com.xima.datadef;

import java.io.ByteArrayOutputStream;

import android.util.Log;

import com.xima.utility.DataConverter;

public class tagCommandStream implements CommandID {

	private byte[] wID = new byte[4];// 命令
	private byte[] dwCmdSize = new byte[4];// 命令长度
	private byte[] IP = new byte[20];// 学生IP
	private byte[] Seat = new byte[8];// 学生座号
	private byte[] Name = new byte[20];// 姓名
	private byte[] Reserve = new byte[480];

	public tagCommandStream() {
		super();
	}

	public tagCommandStream(byte[] data) {
		System.arraycopy(data, 0, this.wID, 0, 4);
		System.arraycopy(data, 4, this.dwCmdSize, 0, 4);
		System.arraycopy(data, 8, this.IP, 0, 20);
		System.arraycopy(data, 28, this.Seat, 0, 8);
		System.arraycopy(data, 36, this.Name, 0, 20);
		System.arraycopy(data, 56, this.Reserve, 0, 480);
		System.out.println("" + DataConverter.byte2HexStr(data));
	}

	public tagCommandCode toCode() {
		tagCommandCode cmd = new tagCommandCode(true);
		cmd.iwID = DataConverter.bytes2int(wID);
		System.out.println("tagCommandStream：ID" + cmd.iwID);

		Log.i("ID", new String(wID));
		cmd.idwCmdSize = DataConverter.bytes2int(dwCmdSize);
		cmd.strIP = DataConverter.trimString(IP);
		cmd.strSeat = DataConverter.trimString(Seat);
		cmd.strName = DataConverter.trimString(Name);

		byte[] r = new byte[4];
		for (int i = 0; i != 120; i++) {
			System.arraycopy(Reserve, i * 4, r, 0, 4);
			cmd.iReserver[i] = DataConverter.bytes2int(r);
		}
		return cmd;
	}
	


	public tagCommandStream(int iwID, int dwCmdSize, String IP, String Seat,
			String Name, int[] iReserve) {
		this.wID = DataConverter.int2bytes(iwID);
		this.dwCmdSize = DataConverter.int2bytes(dwCmdSize);
		if(IP==null) IP = "0";
		System.arraycopy(IP.getBytes(), 0, this.IP, 0, IP.getBytes().length);
		if(Seat == null) Seat ="0";
		System.arraycopy(Seat.getBytes(), 0, this.Seat, 0,
				Seat.getBytes().length);
		if(Name == null)Name ="0";
		System.arraycopy(Name.getBytes(), 0, this.Name, 0,
				Name.getBytes().length);
		
		for (int i = 0; i < 120; i++) {
			System.arraycopy(DataConverter.int2bytes(iReserve[i]), 0,
					this.Reserve, i*4, 4);
		}

	}

	public byte[] toBytes() {
		byte[] bytes = new byte[536];
		System.arraycopy(this.wID, 0, bytes, 0, wID.length);
		System.arraycopy(this.dwCmdSize, 0, bytes, 4, dwCmdSize.length);
		
		System.arraycopy(this.IP, 0, bytes, 8, IP.length);
		System.arraycopy(this.Seat, 0, bytes, 28, Seat.length);
		System.arraycopy(this.Name, 0, bytes, 36, Name.length);
		System.arraycopy(this.Reserve, 0, bytes, 56, Reserve.length);
		return bytes;
	}

}
