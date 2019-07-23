# 北辰风控对接DEMO使用说明

## DEMO入口
src/main/java/demo/RequestDemo.java

## 准备工作

1. 测试服务器须要能连接公网
	
	使用`telnet beichencit.wnqb8.com 1443`

	确认连通正常

2. 在本地目录下准备 
	
	*	contacts.json（通讯录）
	*	bill.json（魔蝎运营商账单）
	*	report.json（魔蝎运营商报告）
		三个文件（已提供了样例）。
	
3. 开发环境

	JDK8 （201，211推荐）


本DEMO演示了从本地，推送魔蝎报告给北辰风控。请阅读对接文档。