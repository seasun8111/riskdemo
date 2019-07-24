package demo;



import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.*;

public class RequestDemo {

    /** 机构请求地址 */
//    public static final String URL = "http://127.0.0.1:8888/beichen/pushreport/1/1";
    public static final String URL = "https://uat.crepolaris.com:1443/beichen/pushreport/1/1";
//    public static final String URL = "http://apiprod.crepolaris.com:8080/beichen/pushreport/1/1";

    private static final String BOUNDARY = "-------";
    private static final String FILE_ENCTYPE = "multipart/form-data";
//    private static final String FILE_ENCTYPE = "text/plain";

    public static  String FILE_PATH = "./";

    /** 由蜂顺提供给机构 */
    public static final String ORGANIZATION_KEY = "1";
    public static final String PRODUCT_KEY = "1";

    /** 1:RSA */
    public static final String SIGN_TYPE = "1";

    /** 未加密的AES密钥 */
    public static final String KEY = "a1Qu160#7ae;&bc2";

    /** RSA加密后的AES密钥 对接机构使用RSA私钥加密 */
    public static final String AES_KEY = "KcGPMn5I4vogX+KiP/kwVRels3cdO2jvih5rgDKGf+Eusxg0dk/eXccy+8bUBnCXEmYjHRUd2nwtRwDH4czyYA==\n";

//    /** 未加密的AES密钥 */
//    public static final String KEY = "CB4BCDA4C9426E0817578E5CADDDD445";
//
//    /** RSA加密后的AES密钥 对接机构使用RSA私钥加密 */
//    public static final String AES_KEY = "PhUMpu11hytnWz3ZxPLrhRRWw8IArXG0o3EL60QeU1z9lv5z9L2QFeYKl4jkEv1BPu9x1OeXqV5yohavZu67Ew==\n";

    /** RSA私钥 应该由对接机构提供，对传输文件进行加签 */
    public static final String PRIVATE_KEY = "MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAmXug8cAahQfFpHYGe3K9gHsBvhnD\n" +
            "VlfR+SkfVQ8pLFEeQGWu7C0xKLNgzkBKP8A9y9j+Zoyv1irPRJvasgR3uwIDAQABAkAN6AezH8bH\n" +
            "Wubrec4ojULiS0LjKI5sWlSqELHIETGX1DXPrkx61AojZGFdO+4rINkXgix5sQAkeExlWml8EMph\n" +
            "AiEAx4gOPVfATGBm7AWS74geXFaA0ONegSJy1i5oUJnHm/MCIQDE62Gyi1lzmCnC63S7EgmvbtK0\n" +
            "BzZhgs95k3NPLtEPGQIhAJQJ7ga1RIdmPvZ+bDYr19rKk2hoSYWl+W3PoLWsYtzhAiAWwGtlSZxo\n" +
            "MqiAkNvH0Wm1D0Tg8ARkd8yo61RjTbFx4QIgAzEzc/MYJubgOqjGB91Bo/GIWyx1NEmBstdA3G5W\n" +
            "f08=\n";

    /** RSA公钥 应该由对接机构提供 */
    public static final String PUBLIC_KEY = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJl7oPHAGoUHxaR2BntyvYB7Ab4Zw1ZX0fkpH1UPKSxR\n" +
            "HkBlruwtMSizYM5ASj/APcvY/maMr9Yqz0Sb2rIEd7sCAwEAAQ==\n";

    /** RSA加密后的三要素 */
    public static final String ELEMENTS3 = "C7Q6T5v2OYrVCK515SU6Lkd/25mnn8afPP82SU8FUQ2mDd7xxZFop/UT1UVSHXaT2YNcxycOIE22yDbIvSdb0A==";




    /**
     * 测试Demo
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        TestDemo demo = new TestDemo();
        demo.testDemo();
        Thread.sleep(5000);
    }



    /**
     * 测试Demo
     * @param args
     * @throws Exception
     */
    /*public static void main(String[] args) throws Exception {
        String filesDirS = "/Users/haiyangluan/Downloads/file";
        File filesDir = new File(filesDirS);
        File[] dirList = filesDir.listFiles();
        for (File dir: dirList) {
            FILE_PATH  = dir.getAbsolutePath()+"/";
            TestDemo demo = new TestDemo();
            demo.testDemo();
            Thread.sleep(5000);
        }
    }*/





    /**
     * 获取本次查询对流水号
     * @return
     */
    public static String getSn(){
        return UUID.randomUUID().toString();
    }

    /**
     * 将json的string串转换成文件
     * @param jsonStr 待转换的json串
     * @param fileName 转换后的文件名
     * @return
     */
    public static File stringToFile(String jsonStr, String fileName) {
        String fileNameTemp = FILE_PATH + fileName;
        File file = new File(fileNameTemp);

        try (BufferedReader bufferedReader = new BufferedReader(new StringReader(jsonStr));
             BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {

            char[] buf = new char[1024];
            int len;
            while ((len = bufferedReader.read(buf)) != -1) {
                bufferedWriter.write(buf, 0, len);
            }
            bufferedWriter.flush();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 将文件转换成zip包并加密
     * @param file 需要转换的文件
     * @param aesKey 加解密密码
     * @return
     */
    public static ZipFile fileToZip(File file, String aesKey) throws ZipException {


        String outFileName = file.getName();
        String destname = FILE_PATH + outFileName + ".zip";
        ZipParameters par = new ZipParameters();
        if (aesKey != null)
        {
            par.setEncryptFiles(true);
            par.setEncryptionMethod(EncryptionMethod.AES);
        }
        par.setCompressionLevel(CompressionLevel.NORMAL);
        ZipFile zipfile = new ZipFile(destname, aesKey.toCharArray());
        zipfile.addFile(file,par);
        return zipfile;
    }


    /**
     * post发送数据的同时发送文件
     * @param urlStr 请求接口url
     * @param params 参数map
     * @param files 文件map
     * @return
     */
    public static InputStream post(String urlStr, Map<String, String> params,
                                   Map<String, ZipFile> files) {
        InputStream is = null;

        try {
            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setConnectTimeout(5000);
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.setRequestMethod("POST");
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Charset", "UTF-8");
            con.setRequestProperty("Content-Type", FILE_ENCTYPE + "; boundary="
                    + BOUNDARY);

            StringBuilder sb = null;
            DataOutputStream dos = new DataOutputStream(con.getOutputStream());
            if (params != null) {
                sb = new StringBuilder();
                for (String s : params.keySet()) {
                    sb.append("--");
                    sb.append(BOUNDARY);
                    sb.append("\r\n");
                    sb.append("Content-Disposition: form-data; name=\"");
                    sb.append(s);
                    sb.append("\"\r\n\r\n");
                    sb.append(params.get(s));
                    sb.append("\r\n");
                }

                dos.write(sb.toString().getBytes());
            }


            if (files != null) {
                for (String s : files.keySet()) {
                    ZipFile f = files.get(s);
                    sb = new StringBuilder();
                    sb.append("--");
                    sb.append(BOUNDARY);
                    sb.append("\r\n");
                    sb.append("Content-Disposition: form-data; name=\"");
                    sb.append(s);
                    sb.append("\"; filename=\"");
                    sb.append(f.getFile().getName());
                    sb.append("\"\r\n");
                    sb.append("Content-Type: application/zip");
                    sb.append("Charset: UTF-8");

                    sb.append("\r\n\r\n");
                    dos.write(sb.toString().getBytes());

                    FileInputStream fis = new FileInputStream(f.getFile());
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) != -1) {
                        dos.write(buffer, 0, len);
                    }
                    dos.write("\r\n".getBytes());
                    fis.close();
                }

                sb = new StringBuilder();
                sb.append("--");
                sb.append(BOUNDARY);
                sb.append("--\r\n");
                dos.write(sb.toString().getBytes());
            }
            dos.flush();
            System.out.println(con.getResponseCode());

            if (con.getResponseCode() == 200) {
                is = con.getInputStream();
            }

            dos.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return is;
    }

    /**
     * 将文件转为byte[]
     * @param file 待转换的文件
     * @return
     */
    public static byte[] getBytes(File file){
        byte[] buffer = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    /**
     * 将json文件转为字符串
     * @param filePath
     * @return
     */
    public static String getJsonStr(String filePath){
        File file = new File(filePath);
        String json = "";
        try(FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr)) {
            int line = 1;
            String context;
            while ((context = br.readLine()) != null) {
                //Context就是读到的json数据
                if(context != null) {
                    json += context;
                }
                line++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }


}