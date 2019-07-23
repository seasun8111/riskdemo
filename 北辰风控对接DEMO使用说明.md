##北辰风控系统接口定义1.0##

|版本号|说明|
|:------------- |:---------------|
1.0|初始版本
1.4|修改了接口名，修改了参数loanHistory为loanType。


##概述

北辰风控系统为贷款机构提供一个风控分值查询服务。

贷款机构下载魔蝎报表和详情后，发送北辰服务。北辰服务计算出风控分值后，推送给贷款机构的回调接口。


此接口提供给贷款机构（以下简称为“机构”），机构提供第三方数据报告（例如魔蝎报告），查询用户的风控值。


加解签流程：
机构方使用蜂顺提供的公钥对内容加签。
蜂顺使用机构方提供的公钥对内容进行加签。
双方提供IP名单加入防火墙配置。


通讯方式：HTTPS协议-POST-application;
编码格式：UTF-8
签名算法：RSA/MD5 with RSA


#### !! <对接前请先导入提供的demo例子，以下功能皆以提供参考代码。>

###1.1 报告上传接口(/beichen/pushreport)参数

UAT接口地址：http://uat.crepolaris.com/beichen/pushdata

生产环境：https://apiprod.crepolaris.com/beichen/pushdata



| 参数  | 类型  | 必传| 描述|
|:------------- |:---------------:| -------------:|:-------:|
|sign |string|Y|api请求的签名
|signType|String|Y|“1”  表示使用RSA
|aesKey|String|Y|RSA加密后的密钥
|organizationId|String|Y|机构ID
|productId|String|Y|产品ID
|aesKey|String|Y|RSA加密后的密钥
|loanType|String|Y|（1 "`FIRST`"首贷，2 "`RELOAN_N`"复贷时使用新报告文件 3 "`RELOAN_R`"复贷时重用上次的报告文件）`FIRST`，`RELOAN_N`时报告文件必须有，`RELOAN_R`时没有报告文件，北辰系统将采用用户在此产品上最后一次提交的报告为依据计算。
|elements3|String|Y|三要素，连接（姓名&手机号&身份证号）再用RSA加密|

此接口用于传送魔蝎数据，魔蝎数据由机构方自行下载报告（report）和详单（bill）组成json格式保存为文本文件，使用zip压缩为zip文件，通过https post到此接口。
原文件要使用zip4j库以AES加密并压缩。（参考对接示例代码）
文件名命名规则： md5(organizationKey+productKey+sn+timestamp).json.zip


元信息要加到zip文件的注释中，示例如下：

```
{
	"organizationId":"机构ID",
	"productId":"产品号",
	"sn":"流水号",
	"name","姓名",
	"mobile_number":"手机号",
	"id_card":"身份证"
}
```
由于注释为敏感信息需要使用aes加密，加密后类似如下形式：

	eOkCp/P6nCpG3n5fRvrtjgw4LCs8Oo04pebtTggW0tEgNFOWrEFjQk3a3yX5AsPDyITCsvMWn6QCtD6YC9cnR2sPxD4uW7QdQczKNppLO25ZEoHl8kj173V4mu4swXP/AIdcg2WcjxljieNHWx6UUdQGBw28vPROBfA3KiKfjnlZEoHl8kj173V4mu4swXP/1AYHDby89E4F8DcqIp+OeVkSgeXySPXvdXia7izBc/8DBsiXgLSiNtEMzpGm3hQCL8b1YSoM++BrTE7fN9R6btlLb0GO95F0ZFh3Zfy4qk6GtI6FoIZx7WZAqp5KqzBkGD9OKtd1/YXLg95mQSJ0pJv9MwRPek4sPX58LH1dfTTHqeuirUAM0mZn1W3jeDv9


* AES加密模式: ECB 

* 填充: zeropadding 

* 数据块: 128位

* 密码: i8ThKoeUFA9SuWAP

* 输出: BASE64

* 字符集: utf8 



报表文件的内容结构：

```
{
	"organizationKey":"机构ID",
	"productKey":"产品号",
	"sn":"流水号",
	"name","姓名",
	"mobile":"手机号",
	"idCard":"身份证",
	"channel":"渠道名称"      #可选项
	"otherInfo":{            #紧急联系人信息
		"emgContacts":[
		{"name":"紧急联系人1姓名","mobile":"紧急联系人1姓名","rela":"与紧急联系人1的关系"},
		{"name":"紧急联系人2姓名","mobile":"紧急联系人2姓名","rela":"与紧急联系人2的关系"}
		],
		"deviceInfo":{
			"os":"android 8.0",
			"geo":{		
				latitude:31.2034221337,
				longitude:121.5088003897
			}
			"deviceId":""
			"ip":"120.32.134.12"
		}
	},
	"Data":{
		"contacts":{通讯录内容}
		"report":{
			"type":"moxie",
		  	"conntent":{report内容}
		 },
		"bill":{
			"type":"moxie",
			"conntent":{bill内容}
		}
	}
}
```
对加密后的zip文件内容加签。

所有操作请参考示例代码，一般无需自行修改。示例代码可以完成文件压缩，加注释，加密，加签的过程。机构只需要按上述接口调用HTTPS接口传送最终的压缩包。

####返回参数（蜂顺返回）



| 参数  | 类型  | 必传| 描述|
|:------------- |:---------------:| -------------:|:-------:|
scuccess|boolean|Y|接口处理是否成功
code|int|Y|响应码
msg|string|Y|返回失败原因等
data|object|N|返回数据


###1.2 风控结果接收接口(机构提供，蜂顺推送)

| 参数  | 类型  | 必传| 描述|
|:------------- |:---------------:| -------------:|:-------:|
sign|string|Y|RSA签名，使用机构提供的公钥加签|
bizParams|string|Y|业务消息体，使用json格式转为string后的内容|
timestamp|string|Y|调用时间戳|


蜂顺收到报告文件（1.1所述）后，通过后台分析后给出授信结果，由蜂顺的推送服务发送到此接口上。


bizParams内容：

```
{
	"organizationId":"机构ID",
	"productId":"产品号",
	"sn":"流水号",
	"name","姓名",
	"mobile_number":"手机号",
	"id_card":"身份证",
	"decisions":
		{
			"score":514,
			"result":"pass"|"deny",
		},
	"error":{
		"code":"错误码",
		"msg":"上传数据解析错误(见附录：错误码信息)"
	}
	"timestamp": 1562227102000
}
```







sign加签方式：bizParams+timestamp。



####返回参数

|参数 |值类型| 必传| 备注
|:------------- |:---------------:| -------------:| :-------------:|
success| boolean |Y |接⼝处理是否成功
code| string| Y| 失败码
msg |string |Y| 失败原因
data| object| N| 业务数据
costTime| long |Y |响应时⻓，毫秒

-------------------

### RESTful接口通用定义

请求参数(request)

|参数名称| 类型| 必传| 备注|
|---|---|---|---|
|method| string| Y| 要请求的API⽅法名称|
sign |string| Y| API请求的签名|
signType |string| Y |1:RSA|
|bizParams |string |Y| 请求的业务数据，此处数据格式为Json封装。具体参数说明⻅详细接⼝的参数列表
|bizEnc| string| N| bizParams加密⽅式（0不加密，1加密:采⽤AES加密算法）|
|aesKey| string |N| RSA加密后的密钥（bizEnc为1时为必传）|
|appId| string| Y| 分配给应⽤的唯⼀标识|
|timestamp |string| Y| 13位时间戳，精确到毫秒|
|callbackUrl| string| N| 回调url|

返回参数(response)

|参数| 类型| 必传| 备注|
|---|---|---|---|
|success |boolean| Y| 接⼝处理是否成功|
|code| int| Y| 响应码|
|msg |string| Y| 返回失败原因等|
|data |object |N |返回数据|



###加密加签描述###


为保证数据传输过程不被篡改，所有接⼝需要进⾏加签和验签，加签算法为rsa，接收参数时需要验签，验签失败拒绝
请求，不处理任何逻辑。

机构调⽤蜂顺流程：

1.获取appId(例如"CP00001")

2.确定业务数据是否需要加密，若需要加密，则加密算法为AES，将AES密钥通过rsa加密放⼊参数，通过AES对业务参
数进⾏加密

3.构造业务参数（业务参数为jsonString作为整体参数加签）和基础参数并对其按key值按ASCII进⾏排序

例如订单状态变化回调(以数据不加密的⽅式为例，若是加密⽅式则处理为先加密后加签)

```
appId = "CP00001"
bizEnc = 0
bizParams= "{
\"orderNo\":\"14201811161018036595\",
\"orderStatus\":100,
\"updateTime\":\"1545013020962\"
}"
method = "ORDER_CALLBACK"
signType = 1
timestamp = "1545013220838"

```

4.拼接参数（key0=value0&key1=value1...）
appId=CP00001&bizEnc=0&bizParams=
{"orderNo":"14201811161018036595","orderStatus":100,"updateTime":"1545013020962"}&method=ORDER_CALLBACK&signType=1&t
imestamp=1545013220838

5.RSA私钥加签
使⽤机构的RSA私钥(priKey="ABCD")进⾏加签，⽣成签名 （sign="*************"）


## 还款计划接⼝

接口地址：`https://xxx.yyy.com/beichen/repay/order_repay_plan_risk_record`

生产环境：`https://xxx.yyy.com/beichen/repay/order_repay_plan_risk_record`


当贷款发放时，机构需要把还款计划发送到以下接口：

1.接⼝描述

机构向北辰系统推送⽤户的还款计划和账单信息

2.调⽤场景及流程

放款完成后，调⽤接⼝告知还款计划

⽤户还款后

⽤户逾期后

⽤户展期后

⽤户更换还款银⾏卡后

其他情况导致还款计划变更

3.参数定义

3.1 请求参数`method="REPAY_PLAN"`

|参数 |名称| 类型|是否为空|备注|
|---|---|---|---|---|
|prodKey|产品KEY|string|否|产品KEY|
|orderNo|订单编号|string|否|查询账单的订单编号（对应报表接口的流水号）|
|openBank| 银⾏名称 |string| 是|还款银⾏名，中⽂名，不要传代码，会展示给⽤户|
|bankCode| 银⾏代码| string| 是| ⻅附录2 银⾏卡列表 |
|bankCard| 银⾏卡号 |string |是| 还款银⾏卡号|
|repaymentPlan| 还款计划 |array| 否 |具体⻅下表格。|
|canPrepay| 是否⽀持提前全部结清 |int| 是| 仅多期产品需回传，1=⽀持；0=不⽀持。|
|canPrepayTime| 可提前全部结清的开始时间|string| 是|当⽀持提前全部结清时需回传。时间戳精确到毫秒|





repaymentPlan元素：

|参数| 名称| 类型| 是否可空| 备注|
|---|---|---|---|---|
|periodNo| 期数| int| 否 |具体的第⼏期|
|dueTime |到期时间|string |否 |到期时间，时间戳 精确到毫秒|
amount| 还款总⾦额|bigDecimal |否|应该还的⾦额 1. 逾期后，需要加⼊逾期费⽤ 2.注意：当该期完成还款后，该⾦额与之前保持⼀致，不要传0|
|paidAmount| 已还⾦额|bigDecimal| 是|当机构有部分扣款的时候，必传。已经还成功的⾦额，amount-paid_amount计算出待还⾦额|
|billStatus| 账单状态|int| 否|1未到期；2已还款；3逾期 4.提前结清全部（当⽤户选择结清全部时，后⾯所有的还款计划不⽤再传）|
|payType|⽀持的还款⽅式类型int| 否| 机构当前⽀持的还款⽅式， 1=主动还款；2=跳转H5|
|successTime| 还款成功时间|string |是 |时间戳，毫秒，当账单状态bill_status为2时必传|
|canRepayTime| 可以还款时间|string| 否| 时间戳，毫秒，当期最早可以还款的时间|
|remark| 描述| string| 否|当期还款⾦额的相关描述,需可读。 例如：含本⾦473.10元，利息&⼿续费172.40元，初审费15.00元，逾期费20.00元|
|fine| 罚息| bigDecimal| 是||
|principal| 本⾦| bigDecimal| 是||
|interest| 还款总⾦额|bigDecimal| 是||


3.2.1 请求数据示例

```
{
	"sign":"xxxxxxxxxxxxxxxxxxxxxx",
	"productId":"1200000",
	"method":"REPAY_PLAN_CALLBACK",
	"signType":1,
	"bizEnc":0,
	"timestamp":"1481347807321",
	"bizParams":"{
		"prodKey":"CP0001",
		"orderNo":"245132241561415",
		"openBank":"中国⼯商银⾏",
		"bankCard":"62122623080043*****",
		"bankCode":"ICBC",
		"canPrepay":1,
		"canPrepayTime":"1473350400000",
		"repaymentPlan":[{
			"periodNo":1,
			"dueTime":"1473350400000",
			"amount":660.5,
			"paidAmount":120.00,
			"payType":1,
			"billStatus":1,
			"canRepayTime":"1476115200000",
			"remark":"含本⾦473.10元，利息&⼿续费172.40元，初审费15.00元，逾期费20.00元。"
		},...]
	}"
}

```

3.3 响应数据示例

```
{
	"success": true,
	"code":"200",
	"msg":"成功",
	"data":{},
}
```





## 附录
#### 1. 错误码信息
|code|msg|
|:------------- |:---------------:|
200| 成功
999999|系统异常
170006|返回数据为空
170010|产品不存在
170011|上传数据解析错误
170012|上传数据缺少必要信息。缺少{属性名....}
190002|⽆法连接到接⼝或服务端内部错误
190001|参数错误
190002|⽆法连接到接⼝或服务端内部错误
190003| 文件缺少
190004| 文件接收失败
190005| 文件数量有误
190006| 验签失败
190007|AES密钥解密失败
190008|复贷时三要素不得为空
190009|三要素解密失败
190010|三要素不合法
190011| 产品不存在




#### 2. 银⾏卡简码列表

|银⾏简码| 银⾏名称|
|---|---|
|ICBC| ⼯商银⾏|
|ABC| 农业银⾏|
|CMB| 招商银⾏|
|CCB| 建设银⾏|
|BCCB |北京银⾏|
|BOC| 中国银⾏|
|COMM |交通银⾏|
|CMBC| ⺠⽣银⾏|
|BOS |上海银⾏|
|CBHB |渤海银⾏|
|CEB| 光⼤银⾏|
|CIB |兴业银⾏|
|CITIC| 中信银⾏|
|CZB |浙商银⾏|
|GDB |⼴发银⾏|
|HXB |华夏银⾏|
|HZCB |杭州银⾏|
|NJCB |南京银⾏|
|PINGAN| 平安银⾏|
|PSBC| 邮政储蓄银⾏|
|SPDB |浦发银⾏|
|NINGBO| 宁波银⾏|
|CITI |花旗银⾏|
|JIANGSU| 江苏银⾏|
