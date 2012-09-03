package com.xima.datadef;

public class tagCommandCode {

	public int iwID;// ����->int
	public int idwCmdSize;// ����->int
	public String strIP;// IP��ַ
	public String strSeat;// ѧ������
	public String strName;// ѧ������
	public int[] iReserver = new int[120];
	public String subIP;//IP����

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

	// ѧ�����������ݱ���ʼ��
	public tagCommandCode(String strIP, String strSeat, String strName) {
		// ��ʼ��IP�����š�ѧ������
		this.strIP = strIP;
		this.strSeat = strSeat;
		this.strName = strName;
	}
	
	//��Ϣ��ʼ��
	public tagCommandCode(String strIP, String strSeat, String strName ,String subIP){
		// ��ʼ��IP�����š�ѧ������
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

	// ��������ID
	public void SetCmdID(int iwID) {
		this.iwID = iwID;
	}

	public byte[] toByteArray() {
		tagCommandStream CmdS;
		CmdS = new tagCommandStream(iwID, idwCmdSize, strIP, strSeat, strName,iReserver);
		return CmdS.toBytes();
	}
}
