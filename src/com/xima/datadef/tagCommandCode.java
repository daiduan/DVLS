package com.xima.datadef;

public class tagCommandCode {

	public int iwID;// 命令->int
	public int idwCmdSize;// 长度->int
	public String strIP;// IP地址
	public String strSeat;// 学生座号
	public String strName;// 学生姓名
	public int[] iReserver = new int[120];
	public String subIP;//IP网段

	public tagCommandCode() {
		super();
		idwCmdSize =0;
		strIP = "0";
		strSeat="0"; 
		strName="0";
		for(int i =0;i!=120;i++){
			iReserver[i]=0;
		};
		
	}
	
	public tagCommandCode(boolean b){
		super();
	}

	// 学生端命令数据报初始化
	public tagCommandCode(String strIP, String strSeat, String strName) {
		// 初始化IP、座号、学生姓名
		this.strIP = strIP;
		this.strSeat = strSeat;
		this.strName = strName;
	}
	
	//信息初始化
	public tagCommandCode(String strIP, String strSeat, String strName ,String subIP){
		// 初始化IP、座号、学生姓名
		this.strIP = strIP;
		this.strSeat = strSeat;
		this.strName = strName;
		this.subIP = subIP;
	}

	public tagCommandCode(byte[] data) {
		tagCommandStream CmdS = new tagCommandStream(data);
		tagCommandCode tcc = CmdS.toCode();
		iwID = tcc.iwID;
		idwCmdSize = tcc.idwCmdSize;
		strIP = tcc.strIP;
		strSeat = tcc.strSeat;
		strName = tcc.strName;
		iReserver = tcc.iReserver;
	}

	// 设置命令ID
	public void SetCmdID(int iwID) {
		this.iwID = iwID;
	}

	public byte[] toByteArray() {
		tagCommandStream CmdS;
		CmdS = new tagCommandStream(iwID, idwCmdSize, strIP, strSeat, strName,iReserver);
		return CmdS.toBytes();
	}
}
