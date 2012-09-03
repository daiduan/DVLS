package com.xima.datadef;

import com.xima.ui.R;

public interface CommandID {
	// APP�ڲ�ID
	static final int TIMEOUTCONNECTION = 6000;
	static final int COMMANDMSG = 6001;
	static final long TIMELONG = 15000L;// ����ʱ��-15��
	static final int DEMODATALONG = 22;// ʾ��ѧ�����ݳ���
	static final int DEMODATAMSG = 6002;
	static final int DATAGRAM = 6111;
	static final int DATALONG = 536;// �������ݳ���
	static final int DEMOINFOLONG = 21;// ѧ����Ϣ���ݳ���
	static final int STULISTLONG = 22;
	// �����ı���Ϣͷ
	static final int MSGCHATROOM = 6003;// ѧ��������������Ϣ
	static final int MSGCHATPEER = 6004;// ѧ��������������Ϣ
	static final int VOICEREQUEST = 6005;//��������
	// ����ID
	static final int LOGIN = 200; // ��½
	static final int ACCEPT = 2; // �����½
	static final int BROADCAST = 3; // �㲥����Ƶ
	static final int CLOSEBROADCAST = 4; // �رչ㲥
	static final int OPENMSGWINDOW = 5; // ����Ϣ�Ի���
	static final int CLOSEMSGWINDOW = 6; // �ر���Ϣ�Ի���
	static final int MSGCONTENT = 7; // ��Ϣ����
	static final int ENABLECHAT = 8; // ����ѧ����̸
	static final int ISABLECHAT = 9; // ��ֹѧ����̸
	static final int FILETRANSFERS = 10; // �ļ�����
	static final int FILECONTENT = 11; // �ļ�����
	static final int TRANSFERSOVER = 12; // �������
	static final int REBOOT = 13; // ��������
	static final int SHUTDOWN = 14; // �ر�
	static final int RAISEHAND = 15; // ����
	static final int CLEARRAISEHAND = 16; // �������
	static final int DISRAISEHAND = 17;// ��ֹ����
	static final int LOCK = 18; // ����ѧ��
	static final int UNLOCK = 19; // ����ѧ��
	static final int TUTORSHIP = 20; // ����
	static final int DEMONSTRATE = 21; // ʾ��
	static final int STOPDEMONSTRATE = 22; // ֹͣʾ��
	static final int CONTROL = 23; // ����
	static final int EXAM = 24; // ����
	static final int OPENPAPER = 25; // ���Ծ�
	static final int CLOSEPAPER = 26; // �ر��Ծ�
	static final int EXAMKEY = 27; // ���Դ�
	static final int RESUME = 28; // ȫ��ָ�
	static final int CLOSE = 29; // ��ʦ���˳�
	static final int SERVECLOSE = 30; // ��ʦ���˳�
	static final int TALK = 31; // �Ի�
	static final int STARTTALK = 32; // ��ʼ�Ի�
	static final int STOPTALK = 33; // ֹͣ�Ի�
	static final int GROUPNO = 34; // �������
	static final int CANCELGROUPNO = 35; // ȡ������
	static final int REMOTECMD = 36; // Զ������
	static final int WARNING = 37; // ����
	static final int REFRESH = 38; // ˢ��
	static final int REFRESHRET = 39; // ˢ�·���
	static final int INGROUP = 40; // ���ɽ�̸������
	static final int OUTGROUP = 41; // ���ɽ�̸�˳���
	static final int NETPAINT = 42; // ���续��
	static final int NOTIFY = 43; // ֪ͨ
	static final int SENDDOWN = 50; // �´�ѧ����Ϣ
	static final int LINEON = 51; // ѧ�����߷���
	static final int SELFSTUDYON = 52; // ѧ������ѧϰ
	static final int SELFSTUDYOFF = 53; // ѧ������ѧϰȡ��
	static final int LOGOFF = 54;// ע��
	static final int SENDRECORDSTART = 55; // �´���Ŀ��ʼ
	static final int SENDRECORDEND = 56; // �´���Ŀ���
	static final int FUNCHECKOK = 57; // ѧ������ѡ��
	static final int BROADCASTSOUNDCARD = 58; // �㲥����
	static final int BROADCASTMAINRADIO1 = 59; // �㲥��¼����һ
	static final int BROADCASTMAINRADIO2 = 60; // �㲥��¼������
	static final int BROADCASTMIC = 61;// �㲥��˷�
	static final int TALKTOONE = 62; // ����ͨ��
	static final int STOPTALKTOONE = 63; // ȡ������ͨ��
	static final int TAKEDEMONSTRATE = 64; // ����ʾ��
	static final int EAVESDROP = 65; // ����
	static final int STOPEAVESDROP = 66; // ȡ������
	static final int SPEAK = 67; // ������������
	static final int SPEAKOK = 68; // ������������ɹ�
	static final int SPEAKFALSE = 69; // ������������ʧ��
	static final int SPEAKCANCEL = 70; // ���������˳�
	static final int SPEAKCANCELOK = 71; // �������۷���
	static final int ALLOWRECORD = 72; // ����¼��
	static final int NOTALLOWRECORD = 73; // ������¼��
	static final int SPEAKON = 74; // ������������
	static final int SPEAKOFF = 75; // ��������������
	static final int GROUPINFORMATION = 76; // ������Ϣ 33ֹͣ�Ի�
	static final int TEACHERIN = 77; // ��ʦ�������Ի�
	static final int TEACHEROUT = 78; // ��ʦ�뿪����Ի�
	static final int DIALOG1 = 79; // ѧ���������˶Ի�
	static final int DIALOG2 = 80; // ��ʦ���ö��˶Ի�
	static final int RANDGROUPINFORMAION = 81; // ���˶Ի����Զ�������Ϣ
	static final int STOPBROADCASTMIC = 82; // ֹͣ�㲥��˷�
	static final int CHIMEIN = 83; // �廰
	static final int STOPCHIMEIN = 84; // ֹͣ�廰
	static final int ADDINDEMON = 85; // ����ʾ����Ϣ
	static final int CANCELDEMON = 86; // �뿪ʾ����Ϣ
	static final int LEAVEDEMON = 87; // �뿪ʾ��
	static final int SEEABOUT = 88; // ѧ��1��ѯѧ��2�ĶԻ�״̬
	static final int SEERETURN = 89; // ѧ��1��ѯѧ��2�ĶԻ�״̬����
	static final int TALKREQUEST_FALSE = 90; // ѧ��1����ѧ��2����Ի�״̬����
	static final int NETPAINTOVER = 91; // ���续���˳�
	static final int ENABLERAISEHAND = 92; // �������
	static final int DROPEHAND = 93; // ���¾���
	static final int REFUSESTUDENT = 94; // ˢ��ѧ��
	static final int SHAREORDER = 95; // �������Ϣ
	static final int TALKREQUESTCANCEL = 96; // �Ի�����ȡ��
	static final int TALKCHANGEGROUPNO = 97; // �Ի�:�鳤�˳����������
	static final int UPDATEDATA = 98; // ˢ��
	static final int SEE_TEACHERONLINE = 99; // ��ʦ����
	static final int TEACHERONLINE = 100; // ��ʦ����
	static final int BROADCASTVIDEO = 101; // �㲥��Ƶ
	static final int BROADCASTAUDIO = 102; // �㲥��Ƶ
	static final int CLOSEBROADCASTVIDEO = 103; // ֹͣ�㲥��Ƶ
	static final int CLOSEBROADCASTAUDIO = 104; // ֹͣ�㲥��Ƶ
	static final int FOLLOWREAD = 105; // ����
	static final int STOPFOLLOWREAD = 106;// ֹͣ����
	static final int CLASSOVER = 107;// �¿�
	static final int CLASSRESUME = 108; // �¿λָ�
	static final int LOGINMSG = 109;// ��½����
	static final int HARDBROAD = 110; // Ӳ���㲥
	static final int CLOSEHARDBROAD = 111;// ��Ӳ���㲥
	static final int TEACHEREXIST = 112;// ����ʦ����
	static final int TRAN = 113;// ͬ������
	static final int STOPTRAN = 114; // ֹͣͬ������
	static final int TRANPATH = 115; // ͬ������·��
	static final int FNAMELOGIN = 116; // ʵ����½ (��ʦ��)
	static final int FLOGIN = 117; // ʵ����½ (ѧ����)
	static final int BCHANNE = 118; // Ӱ��㲥:(��ʦ��) Reserve[0]:5:��(�㲥��ʼ)
									// 1:ֹͣ(�㲥ֹͣ) 2:��ͣ 3:�����У��㲥ͬ���� 4:����
	static final int REFUSHLIST = 119; // ˢ��ѧ���б� (ѧ����)
	static final int FTP = 120; // FTP���� (ѧ������)
	static final int VNC = 121; // VNC���� (ѧ������)
	static final int SCH = 122; // �������� (ѧ������)
	static final int LOGINEXAM = 123;// ��½���Է����� (ѧ����)
	static final int LOGINEXAM_R = 124; // ���ص�½��Ϣ (��ʦ��) :Reserve[0]:0:���� 1:�Ծ�����
	static final int LOGINFTP = 125;// ��½FTP������ (ѧ����)
	static final int LOGINFTP_R = 126; // ���ص�½��Ϣ (��ʦ��)
	static final int ALLOWCLASS = 127; // �����Ͽι���ģ�� (��ʦ��)
	static final int SENDROOM = 128;// ѧ�����͵���������Ϣ(ѧ����):Reserve[0]:1:������������
									// 2�������˳����� 3:ˢ�� 4:��ʼ˵�� 5:ֹͣ˵��
	static final int GETROOM = 129; // ��ʦ���ص���������Ϣ(��ʦ��):Reserve[0]:1:���ؼ�������
									// 2�������˳����� 3:����ˢ�� 4:���ؿ�ʼ˵�� 5:����ֹͣ˵��
	static final int FILEDOWN = 130; // �ļ��´�
	static final int BVLC = 131; // vlc�㲥 Reserve[0]:0:ֹͣ 1:��ʼ (��ʦ��)
	static final int TRAN_C = 132; // ͬ������ ���� (��ʦ��)
	static final int COMEBACK = 133; // ����¼���ļ�Reserve[0]:0:��ʦ�� 1:ѧ���� (
										// ��ʦ:Reserve[1]==1ֹͣ) (
										// ѧ��:Reserve[1]���صĽ���)
	static final int DEMONSTRATEVIDEO = 134;// ��Ļʾ��
	static final int BVTRAN = 135; // 8���㲥 ���� Reserve[0]:0:ֹͣ 1:��ʼ (��ʦ��)
	static final int BVREP = 136; // 8���㲥 �ط� Reserve[0]:0:ֹͣ 1:��ʼ (��ʦ��)
	static final int RUNPROGRAM = 137; // Reserve[0]:0:kill 1:run (��ʦ��)
	static final int GETODPATH = 138; // ȡ�õ㲥��ַŶ (ѧʡ��)
	static final int SETVOLUME = 139; // ����ѧ��Ĭ������(��ʦ��)
	static final int VTESTSTART = 140; // ������Կ�ʼ(��ʦ��)
	static final int VTESTSTOP = 141; // ������Խ���(��ʦ��)
	static final int VTESTSELF = 142; // �������������ϰ(��ʦ��) Reserve[0]:0������ 1������
	static final int RAISEHAND_OK = 500; // ���� �ɹ����أ���ʦ����
	static final int CLEARRAISEHAND_OK = 501; // ����ȡ�� �ɹ����أ���ʦ����
	static final int REFUSHLIST_RETURN = 502;     //��ʦ����ѧ���б���ʦ����
	static final int GETSTUINFO	= 503;     //ѧ����ѯ�Լ�����Ϣ�������ȣ�ѧ������
	static final int GETSTUINFO_RETURN = 504;     //��ʦ����ѧ����Ϣ����ʦ����
	static final int STUSETVOLUME= 505;     //ѧ������������ѧ������
	static final int STURECORD	= 506;     //ѧ��¼����ѧ������:Reserve[0]==0Ϊ���� 1Ϊ��ʼ 2Ϊ��ͣ 3Ϊ�ָ� 
	static final int STFOLLOWREAD = 507;     //ѧ��������ѧ������Reserve[0]==0Ϊֹͣ 1Ϊ��ʼ 
	static final int SELFTALK = 508;     //ѧ���������죨ѧ������0Ϊֹͣ 1Ϊ��ʼReserve[1]

	public static Integer[] mThumbIds = { R.drawable.face1, R.drawable.face2,
			R.drawable.face3, R.drawable.face4, R.drawable.face5,
			R.drawable.face6, R.drawable.face7, R.drawable.face8,
			R.drawable.face9, R.drawable.face10, R.drawable.face11,
			R.drawable.face12, R.drawable.face13, R.drawable.face14,
			R.drawable.face15, R.drawable.face16, R.drawable.face17,
			R.drawable.face18, R.drawable.face19, R.drawable.face20,
			R.drawable.face21, R.drawable.face22, R.drawable.face23,
			R.drawable.face24, R.drawable.face25, R.drawable.face26,
			R.drawable.face27, R.drawable.face28, R.drawable.face29,
			R.drawable.face30, R.drawable.face31, R.drawable.face32,
			R.drawable.face33, R.drawable.face34, R.drawable.face35,
			R.drawable.face36, R.drawable.face37, R.drawable.face38,
			R.drawable.face39, R.drawable.face40, R.drawable.face41,
			R.drawable.face42, R.drawable.face43, R.drawable.face44,
			R.drawable.face45, R.drawable.face46, R.drawable.face47,
			R.drawable.face48, R.drawable.face49, R.drawable.face50, };

}
