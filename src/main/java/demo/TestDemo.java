package demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tiefan.iwu.risk.CommentDTO;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.codec.digest.DigestUtils;
import util.AesECBUtil;
import util.RSACoderUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static demo.RequestDemo.*;

public class TestDemo {



    public void testDemo() throws Exception {
        String billStr = getJsonStr(FILE_PATH + "bill.json");
        JSONObject bill = JSONObject.parseObject(billStr);

        String reportStr = getJsonStr(FILE_PATH + "report.json");
        JSONObject report = JSONObject.parseObject(reportStr);

        String contactsStr = getJsonStr(FILE_PATH + "contacts.json");
        JSONArray contacts = JSONObject.parseArray(contactsStr);


        /**
         *  拼接文件名
         */
        String sn = getSn();
        long timestamp = System.currentTimeMillis();
        String md5Str = ORGANIZATION_KEY + PRODUCT_KEY + sn + timestamp;
        String md5 = DigestUtils.md5Hex(md5Str).substring(8,24);
        String fileName = md5 + ".json";

        /**
         * 将数据写入文件并打包，此处仅为示例，具体传参请参考文档
         */
        JSONObject json = new JSONObject();
        json.put("organizationKey","1");
        json.put("productKey","1");
        json.put("sn",sn);
        json.put("name","石明栋");
        json.put("moblie","13002982937");
        json.put("idCard","320322199408288651");

        JSONObject otherInfo = new JSONObject();

        JSONObject emgContact = new JSONObject();
        List<JSONObject> emgList = new ArrayList<>();
        emgContact.put("name","小明");
        emgContact.put("mobile","110");
        emgContact.put("rela","警民关系");
        emgList.add(emgContact);
        otherInfo.put("emgContacts",emgList);

        JSONObject deviceInfo = new JSONObject();
        JSONObject geo = new JSONObject();
        geo.put("latitude","123");
        geo.put("longitude","321");
        deviceInfo.put("os","android");
        deviceInfo.put("geo",geo);
        deviceInfo.put("deviceId","");
        deviceInfo.put("ip","10.10.10.110");
        otherInfo.put("deviceInfo",deviceInfo);
        json.put("otherInfo",otherInfo);

        JSONObject data = new JSONObject();
        report.put("type","moxie");
        report.put("conntent",null);
        bill.put("type","moxie");
        bill.put("conntent",null);
        data.put("contacts",contacts);
        data.put("report",report);
        data.put("bill",bill);
        json.put("data",data);

        File file = stringToFile(json.toJSONString(), fileName);
        Map<String, ZipFile> fileMap = new HashMap<>();

        /**
         * 打包并添加注释
         */
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setOrganizationId(ORGANIZATION_KEY);
        commentDTO.setProductId(PRODUCT_KEY);
        commentDTO.setSn(sn);
        commentDTO.setName("石明栋");
        commentDTO.setMobileNumber("13002982937");
        commentDTO.setIdCard("320322199408288651");
        ZipFile zipFile = fileToZip(file,KEY);

        String aesKey = RSACoderUtil.decryptAesKey(RequestDemo.AES_KEY, RequestDemo.PUBLIC_KEY);
        zipFile.setComment( AesECBUtil.encrypt(JSON.toJSONString(commentDTO), aesKey) );

        /**
         * 接口传参
         */
        String sign = RSACoderUtil.sign(getBytes(zipFile.getFile()),PRIVATE_KEY);
        Map<String,String> textMap = new HashMap<>();
        textMap.put("signType",SIGN_TYPE);
        textMap.put("aesKey",AES_KEY);
        textMap.put("sign",sign);
        textMap.put("loanType","FIRST");
        textMap.put("elements3",ELEMENTS3);

        /** 发送的文件 */
        fileMap.put("zipFile",zipFile);

        ZipFile zipFile2 = new ZipFile("/Users/haiyangluan/Downloads/file/0aba72870b344ca7f71a11a9cd81b5a1/"+fileName+".zip");

        /**
         *  发送请求，获取到蜂顺返回结果并解析
         */
//        InputStream in = post(URL,textMap,null);
        InputStream in = post(URL,textMap,fileMap);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i;
        while((i = in.read()) != -1){
            baos.write(i);
        }
        String result = baos.toString();
        System.out.println(result);

    }
}
