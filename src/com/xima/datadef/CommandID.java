package com.xima.datadef;

import com.xima.ui.R;

public interface CommandID {
	// APP内部ID
	static final int TIMEOUTCONNECTION = 6000;
	static final int COMMANDMSG = 6001;
	static final long TIMELONG = 15000L;// 连接时长-15秒
	static final int DEMODATALONG = 22;// 示范学生数据长度
	static final int DEMODATAMSG = 6002;
	static final int DATAGRAM = 6111;
	static final int DATALONG = 536;// 命令数据长度
	static final int DEMOINFOLONG = 21;// 学生信息数据长度
	static final int STULISTLONG = 22;
	// 聊天文本消息头
	static final int MSGCHATROOM = 6003;// 学生发送聊天室消息
	static final int MSGCHATPEER = 6004;// 学生发送聊天室消息
	static final int VOICEREQUEST = 6005;//语音请求
	// 命令ID
	static final int LOGIN = 200; // 登陆
	static final int ACCEPT = 2; // 允许登陆
	static final int BROADCAST = 3; // 广播音视频
	static final int CLOSEBROADCAST = 4; // 关闭广播
	static final int OPENMSGWINDOW = 5; // 打开消息对话框
	static final int CLOSEMSGWINDOW = 6; // 关闭消息对话框
	static final int MSGCONTENT = 7; // 消息内容
	static final int ENABLECHAT = 8; // 允许学生交谈
	static final int ISABLECHAT = 9; // 禁止学生交谈
	static final int FILETRANSFERS = 10; // 文件传输
	static final int FILECONTENT = 11; // 文件内容
	static final int TRANSFERSOVER = 12; // 传输完毕
	static final int REBOOT = 13; // 重新启动
	static final int SHUTDOWN = 14; // 关闭
	static final int RAISEHAND = 15; // 举手
	static final int CLEARRAISEHAND = 16; // 清除举手
	static final int DISRAISEHAND = 17;// 禁止举手
	static final int LOCK = 18; // 锁定学生
	static final int UNLOCK = 19; // 解锁学生
	static final int TUTORSHIP = 20; // 辅导
	static final int DEMONSTRATE = 21; // 示范
	static final int STOPDEMONSTRATE = 22; // 停止示范
	static final int CONTROL = 23; // 控制
	static final int EXAM = 24; // 考试
	static final int OPENPAPER = 25; // 打开试卷
	static final int CLOSEPAPER = 26; // 关闭试卷
	static final int EXAMKEY = 27; // 考试答案
	static final int RESUME = 28; // 全体恢复
	static final int CLOSE = 29; // 教师机退出
	static final int SERVECLOSE = 30; // 教师机退出
	static final int TALK = 31; // 对话
	static final int STARTTALK = 32; // 开始对话
	static final int STOPTALK = 33; // 停止对话
	static final int GROUPNO = 34; // 分组组号
	static final int CANCELGROUPNO = 35; // 取消分组
	static final int REMOTECMD = 36; // 远程命令
	static final int WARNING = 37; // 警告
	static final int REFRESH = 38; // 刷新
	static final int REFRESHRET = 39; // 刷新返回
	static final int INGROUP = 40; // 自由交谈进入组
	static final int OUTGROUP = 41; // 自由交谈退出组
	static final int NETPAINT = 42; // 网络画板
	static final int NOTIFY = 43; // 通知
	static final int SENDDOWN = 50; // 下传学生信息
	static final int LINEON = 51; // 学生在线返回
	static final int SELFSTUDYON = 52; // 学生自主学习
	static final int SELFSTUDYOFF = 53; // 学生自主学习取消
	static final int LOGOFF = 54;// 注销
	static final int SENDRECORDSTART = 55; // 下传节目开始
	static final int SENDRECORDEND = 56; // 下传节目完毕
	static final int FUNCHECKOK = 57; // 学生功能选择
	static final int BROADCASTSOUNDCARD = 58; // 广播声卡
	static final int BROADCASTMAINRADIO1 = 59; // 广播主录音机一
	static final int BROADCASTMAINRADIO2 = 60; // 广播主录音机二
	static final int BROADCASTMIC = 61;// 广播麦克风
	static final int TALKTOONE = 62; // 个别通话
	static final int STOPTALKTOONE = 63; // 取消个别通话
	static final int TAKEDEMONSTRATE = 64; // 接受示范
	static final int EAVESDROP = 65; // 监听
	static final int STOPEAVESDROP = 66; // 取消监听
	static final int SPEAK = 67; // 自由讨论申请
	static final int SPEAKOK = 68; // 自由讨论申请成功
	static final int SPEAKFALSE = 69; // 自由讨论申请失败
	static final int SPEAKCANCEL = 70; // 自由讨论退出
	static final int SPEAKCANCELOK = 71; // 自由讨论返回
	static final int ALLOWRECORD = 72; // 允许录音
	static final int NOTALLOWRECORD = 73; // 不允许录音
	static final int SPEAKON = 74; // 允许自由讨论
	static final int SPEAKOFF = 75; // 不允许自由讨论
	static final int GROUPINFORMATION = 76; // 分组信息 33停止对话
	static final int TEACHERIN = 77; // 教师加入分组对话
	static final int TEACHEROUT = 78; // 教师离开分组对话
	static final int DIALOG1 = 79; // 学生自主二人对话
	static final int DIALOG2 = 80; // 教师设置二人对话
	static final int RANDGROUPINFORMAION = 81; // 二人对话或自动分组信息
	static final int STOPBROADCASTMIC = 82; // 停止广播麦克风
	static final int CHIMEIN = 83; // 插话
	static final int STOPCHIMEIN = 84; // 停止插话
	static final int ADDINDEMON = 85; // 加入示范信息
	static final int CANCELDEMON = 86; // 离开示范信息
	static final int LEAVEDEMON = 87; // 离开示范
	static final int SEEABOUT = 88; // 学生1查询学生2的对话状态
	static final int SEERETURN = 89; // 学生1查询学生2的对话状态返回
	static final int TALKREQUEST_FALSE = 90; // 学生1加入学生2的组对话状态返回
	static final int NETPAINTOVER = 91; // 网络画板退出
	static final int ENABLERAISEHAND = 92; // 允许举手
	static final int DROPEHAND = 93; // 放下举手
	static final int REFUSESTUDENT = 94; // 刷新学生
	static final int SHAREORDER = 95; // 共享的信息
	static final int TALKREQUESTCANCEL = 96; // 对话请求取消
	static final int TALKCHANGEGROUPNO = 97; // 对话:组长退出，更改组号
	static final int UPDATEDATA = 98; // 刷新
	static final int SEE_TEACHERONLINE = 99; // 教师在线
	static final int TEACHERONLINE = 100; // 教师在线
	static final int BROADCASTVIDEO = 101; // 广播视频
	static final int BROADCASTAUDIO = 102; // 广播音频
	static final int CLOSEBROADCASTVIDEO = 103; // 停止广播视频
	static final int CLOSEBROADCASTAUDIO = 104; // 停止广播音频
	static final int FOLLOWREAD = 105; // 跟读
	static final int STOPFOLLOWREAD = 106;// 停止跟读
	static final int CLASSOVER = 107;// 下课
	static final int CLASSRESUME = 108; // 下课恢复
	static final int LOGINMSG = 109;// 登陆数据
	static final int HARDBROAD = 110; // 硬件广播
	static final int CLOSEHARDBROAD = 111;// 关硬件广播
	static final int TEACHEREXIST = 112;// 发教师存在
	static final int TRAN = 113;// 同声传译
	static final int STOPTRAN = 114; // 停止同声传译
	static final int TRANPATH = 115; // 同声传译路径
	static final int FNAMELOGIN = 116; // 实名登陆 (教师发)
	static final int FLOGIN = 117; // 实名登陆 (学生发)
	static final int BCHANNE = 118; // 影像广播:(教师发) Reserve[0]:5:打开(广播开始)
									// 1:停止(广播停止) 2:暂停 3:播放中（广播同步） 4:播放
	static final int REFUSHLIST = 119; // 刷新学生列表 (学生发)
	static final int FTP = 120; // FTP请求 (学生互发)
	static final int VNC = 121; // VNC请求 (学生互发)
	static final int SCH = 122; // 聊天请求 (学生互发)
	static final int LOGINEXAM = 123;// 登陆考试服务器 (学生发)
	static final int LOGINEXAM_R = 124; // 返回登陆信息 (教师发) :Reserve[0]:0:连接 1:试卷套数
	static final int LOGINFTP = 125;// 登陆FTP服务器 (学生发)
	static final int LOGINFTP_R = 126; // 返回登陆信息 (教师发)
	static final int ALLOWCLASS = 127; // 允许上课功能模块 (教师发)
	static final int SENDROOM = 128;// 学生发送的聊天室消息(学生发):Reserve[0]:1:发出加入请求
									// 2：发出退出请求 3:刷新 4:开始说话 5:停止说话
	static final int GETROOM = 129; // 教师返回的聊天室消息(教师发):Reserve[0]:1:返回加入请求
									// 2：返回退出请求 3:返回刷新 4:返回开始说话 5:返回停止说话
	static final int FILEDOWN = 130; // 文件下传
	static final int BVLC = 131; // vlc广播 Reserve[0]:0:停止 1:开始 (教师发)
	static final int TRAN_C = 132; // 同声传译 控制 (教师发)
	static final int COMEBACK = 133; // 回收录音文件Reserve[0]:0:教师发 1:学生发 (
										// 教师:Reserve[1]==1停止) (
										// 学生:Reserve[1]返回的进度)
	static final int DEMONSTRATEVIDEO = 134;// 屏幕示范
	static final int BVTRAN = 135; // 8屏广播 翻译 Reserve[0]:0:停止 1:开始 (教师发)
	static final int BVREP = 136; // 8屏广播 回放 Reserve[0]:0:停止 1:开始 (教师发)
	static final int RUNPROGRAM = 137; // Reserve[0]:0:kill 1:run (教师发)
	static final int GETODPATH = 138; // 取得点播地址哦 (学省发)
	static final int SETVOLUME = 139; // 设置学生默认音量(教师发)
	static final int VTESTSTART = 140; // 口语测试开始(教师发)
	static final int VTESTSTOP = 141; // 口语测试结束(教师发)
	static final int VTESTSELF = 142; // 口语测试自主练习(教师发) Reserve[0]:0不允许 1：允许
	static final int RAISEHAND_OK = 500; // 举手 成功返回（教师发）
	static final int CLEARRAISEHAND_OK = 501; // 举手取消 成功返回（教师发）
	static final int REFUSHLIST_RETURN = 502;     //教师返回学生列表（教师发）
	static final int GETSTUINFO	= 503;     //学生查询自己的信息如姓名等（学生发）
	static final int GETSTUINFO_RETURN = 504;     //教师返回学生信息（教师发）
	static final int STUSETVOLUME= 505;     //学生设置音量（学生发）
	static final int STURECORD	= 506;     //学生录音（学生发）:Reserve[0]==0为结束 1为开始 2为暂停 3为恢复 
	static final int STFOLLOWREAD = 507;     //学生跟读（学生发）Reserve[0]==0为停止 1为开始 
	static final int SELFTALK = 508;     //学生自主聊天（学生发）0为停止 1为开始Reserve[1]

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
