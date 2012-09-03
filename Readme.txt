

/****---对应功能---****/
ClassTeachingActivity   -上课
InternetActivity 		-网上冲浪
BrowserActivity			-自主点播
PlayerActivity			-媒体播放器
ReadingActivity			-电子阅读
ChatPeerActivity		-学习交流
ChatRoomActivity		-聊天室

2011.12.30 by MW
1.修改了UDPReceiver，简化数据处理
2.优化了UDPService，明确了广播数据类型

2011.12.31 by MW
已完成：
1.简化了BroadcastRe的消息处理，将数据直接提交至activity的handler处理
2.在UDPReceiver中添加IP获取，传至Service，Broadcast，handler
3.优化了接收“教师存在”，“掉线”。简单处理了“上课”中示范列表
4.模板化了activity，但是internet，reading，player三个未处理

未完成：
1.比对自己是否在示范列表中。显示示范列表。-预计使用popupwindow
2.聊天室
3.Cling的使用

2012.1.2 by MW
已完成:
1.完善了ChatRoom，完成所有消息处理，添加了“语音开”“语音关”按钮
2.基本完成ChatPeer，添加语音通话功能

未完成：
1.ChatRoom接收命令需要测试
2.ChatPeer语音请求逻辑尚需完善

2012.1.3 by MW
已完成：
1.ChatPeer语音请求逻辑

未完成：
1.ChatPeer布局,UDP命令在Service等的定义

2012.1.6 by MW
已完成：
1.导入Cling库，可以接收数据

未完成:

2012.1.7 by MW
未完成：
1.细化整个程序启动流程，根据教师机存在和不存在两种情况做初始化。针对所有程序完成初始化配置
2.检查合同要求，尽快完成未完成部分。包括收到教师要求学生开始上课的转变
3.搞清service启动流程，保证能够接收数据
4.Cling的优化和载入处理
5.未完成部分

2012.1.11 by MW
未完成：
1.掉线测试
2.录音及录音列表等
3.UPNP服务

2012.1.24 by MW
未完成：
1.UPNP服务及播放、电子阅读
2.掉线流程处理
3.录音功能及列表处理
4.聊天室功能修正bug
5.上课下课ClassTeachActivity状态转换
6.关机
7.媒体播放器移植
8.activity单一模式

已完成：
1.聊天室功能修正，未测试

2012.2.9 by MW
已完成：
1.UPNP服务
2.媒体播放器移植
3.activity单一模式

未完成：
1.掉线流程处理
2.录音功能及列表处理
3.上课下课ClassTeachActivity状态转换（部分完成）
4.关机
5.文本阅读器
6.媒体播放器播放列表
7.音量调节
8.学生列表

2012.2.17 by MW
已完成：
1.录音功能及列表处理
2.状态转换
3.文本阅读
4.媒体播放器列表（有bug）
5.音量调节
6.学生列表

未完成：
1.掉线流程小问题--通过服务器端缩短掉线判断来实现？
2.开机启动程序
3.关机
4.弹出窗口布局

优化surfaceview与mediaplayer关系

已完成：
1.弹出窗口布局
2.开机启动程序

未完成：
1.关机

未完成：
1.通知公告栏
2.警告公告栏
3.自由讨论
4.分组对话
5.注销重启关机

测试报告：
1.