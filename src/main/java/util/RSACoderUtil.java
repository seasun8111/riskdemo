package util;


import javax.crypto.Cipher;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;
import java.util.HashMap;
import java.util.Map;

import static demo.RequestDemo.PUBLIC_KEY;
import static demo.RequestDemo.PRIVATE_KEY;

/**
 * RSA安全编码组件
 *
 * @author george on 2018/4/26 下午4:59
 * @version 1.0
 * @since 1.0
 */
public abstract class RSACoderUtil extends CoderUtil {
    public static final String KEY_ALGORITHM = "RSA";
    public static final String SIGNATURE_ALGORITHM = "MD5withRSA";

    /**
     * String to hold name of the public key file.
     */
    public static final String PUBLIC_KEY_FILE = "/Users/george/Downloads/public.key";


    public static void main(String[] args) throws Exception {
        /**
         * 测试RSA私钥
         */
        String prv_key = "MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAmXug8cAahQfFpHYGe3K9gHsBvhnD\n" +
                "VlfR+SkfVQ8pLFEeQGWu7C0xKLNgzkBKP8A9y9j+Zoyv1irPRJvasgR3uwIDAQABAkAN6AezH8bH\n" +
                "Wubrec4ojULiS0LjKI5sWlSqELHIETGX1DXPrkx61AojZGFdO+4rINkXgix5sQAkeExlWml8EMph\n" +
                "AiEAx4gOPVfATGBm7AWS74geXFaA0ONegSJy1i5oUJnHm/MCIQDE62Gyi1lzmCnC63S7EgmvbtK0\n" +
                "BzZhgs95k3NPLtEPGQIhAJQJ7ga1RIdmPvZ+bDYr19rKk2hoSYWl+W3PoLWsYtzhAiAWwGtlSZxo\n" +
                "MqiAkNvH0Wm1D0Tg8ARkd8yo61RjTbFx4QIgAzEzc/MYJubgOqjGB91Bo/GIWyx1NEmBstdA3G5W\n" +
                "f08=\n";
        /**
         * 测试RSA公钥
         */
        String pub_key = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJl7oPHAGoUHxaR2BntyvYB7Ab4Zw1ZX0fkpH1UPKSxR\n" +
                "HkBlruwtMSizYM5ASj/APcvY/maMr9Yqz0Sb2rIEd7sCAwEAAQ==\n";

        /**
         * 测试AES密钥
         */
        String aesKey = "CB4BCDA4C9426E0817578E5CADDDD445";

        String elements3 ="1&idCard&mobile";

        /**
         * RSA对AES密钥加密
         */
        byte[] result = encryptByPrivateKey(elements3,PRIVATE_KEY);
        System.out.println(Base64.encodeBase64String(result));



        /** 密钥解密 */
        System.out.println(decryptByPubKey(result,PUBLIC_KEY));

    }

    /**
     * 拼装明文进行签名
     * @param userName
     * @param phoneNo
     * @param timestamp
     * @param privateKey
     * @return 签名信息
     * @throws Exception
     */
    public static String getSign(String userName, String phoneNo, String timestamp, String privateKey) throws Exception {
        String result = "";
        StringBuffer sb = new StringBuffer();
        sb.append("name=").append(userName).append("&");
        sb.append("phoneNo=").append(phoneNo).append("&");
        sb.append("timestamp=").append(timestamp);
        result = sign(sb.toString().getBytes(), privateKey);
        result = result.replaceAll("\\n", "");
        result = result.replaceAll("\\r", "");
        return result;
    }

    /**
     * 拼装明文进行签名
     * @param userName
     * @param phoneNo
     * @param timestamp
     * @param privateKey
     * @return 签名信息
     * @throws Exception
     */
    public static String getSign2(String userName, String phoneNo, String orderNo, String timestamp, String privateKey) throws Exception {
        String result = "";
        StringBuffer sb = new StringBuffer();
        sb.append("name=").append(userName).append("&");
        sb.append("orderNo=").append(orderNo).append("&");
        sb.append("phoneNo=").append(phoneNo).append("&");
        sb.append("timestamp=").append(timestamp);
        result = sign(sb.toString().getBytes(), privateKey);
        result = result.replaceAll("\\n", "");
        result = result.replaceAll("\\r", "");
        return result;
    }

    /**
     * 用私钥对信息生成数字签名
     *
     * @param data
     *            加密数据
     * @param privateKey
     *            私钥
     *
     * @return 数字签名
     * @throws Exception 加密异常
     */
    public static String sign(byte[] data, String privateKey) throws Exception {
        // 解密由base64编码的私钥
        byte[] keyBytes = decryptBASE64(privateKey);

        // 构造PKCS8EncodedKeySpec对象
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);

        // KEY_ALGORITHM 指定的加密算法
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);

        // 取私钥匙对象
        PrivateKey priKey = keyFactory.generatePrivate(pkcs8KeySpec);

        // 用私钥对信息生成数字签名
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(priKey);
        signature.update(data);

        String sign = encryptBASE64(signature.sign());
        sign = sign.replaceAll("\\n", "");
        return sign;
    }

    /**
     * 校验数字签名
     *
     * @param data
     *            加密数据
     * @param publicKey
     *            公钥
     * @param sign
     *            数字签名
     *
     * @return 校验成功返回true 失败返回false
     * @throws Exception 解密异常
     *
     */
    public static boolean verify(byte[] data, String publicKey, String sign)
            throws Exception {

        // 解密由base64编码的公钥
        byte[] keyBytes = decryptBASE64(publicKey);

        // 构造X509EncodedKeySpec对象
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);

        // KEY_ALGORITHM 指定的加密算法
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);

        // 取公钥匙对象
        PublicKey pubKey = keyFactory.generatePublic(keySpec);

        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(pubKey);
        signature.update(data);

        // 验证签名是否正常
        return signature.verify(decryptBASE64(sign));
    }


    /**
     * 解密<br>
     * 用公钥解密
     *
     * @param data 加密数据
     * @param key 公钥
     * @return 返回公钥解密后的byte数组
     * @throws Exception 解密异常
     */
    public static byte[] decryptByPublicKey(byte[] data, String key)
            throws Exception {
        // 对密钥解密
        byte[] keyBytes = decryptBASE64(key);

        // 取得公钥
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicKey = keyFactory.generatePublic(x509KeySpec);

        // 对数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, publicKey);

        return cipher.doFinal(data);
    }

    /**
     * 解密<br>
     * 用公钥解密
     *
     * @param data 加密数据byte数组
     * @param key 公钥
     * @return 返回公钥解密后的字符串
     * @throws Exception 解密异常
     */
    public static String decryptByPubKey(byte[] data, String key)
            throws Exception {
        return new String(decryptByPublicKey(data, key)).replaceAll("\n","");
    }


    /**
     * 加密<br>
     * 用私钥加密
     *
     * @param data 待加密数据
     * @param key 私钥
     * @return 返回私钥加密后的byte数组
     * @throws Exception 加密异常
     */
    public static byte[] encryptByPrivateKey(byte[] data, String key)
            throws Exception {
        // 对密钥解密
        byte[] keyBytes = decryptBASE64(key);

        // 取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);

        // 对数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);

        return cipher.doFinal(data);
    }

    /**
     * 用私钥加密
     * @param data
     * @param key
     * @return 返回私钥加密后的数组
     * @throws Exception 加密异常，字符串转换异常
     */
    public static byte[] encryptByPrivateKey(String data, String key)
            throws Exception {
        return encryptByPrivateKey(data.getBytes(), key);
    }



    /**
     * 取得私钥
     *
     * @param keyMap 密钥对Map
     * @return 私钥
     * @throws Exception 加密异常
     */
    public static String getPrivateKey(Map<String, Object> keyMap)
            throws Exception {
        Key key = (Key) keyMap.get(PRIVATE_KEY);

        return encryptBASE64(key.getEncoded());
    }

    /**
     * 取得公钥
     *
     * @param keyMap 密钥对Map
     * @return 公钥
     * @throws Exception 加密异常
     */
    public static String getPublicKey(Map<String, Object> keyMap)
            throws Exception {
        Key key = (Key) keyMap.get(PUBLIC_KEY);

        return encryptBASE64(key.getEncoded());
    }

    /**
     * 初始化密钥
     *
     * @return 密钥对Map
     * @throws Exception 密钥生成异常
     */
    public static Map<String, Object> initKey() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator
                .getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(512);

        KeyPair keyPair = keyPairGen.generateKeyPair();

        // 公钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        // 私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        Map<String, Object> keyMap = new HashMap<String, Object>(2);

        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }

    /**
     * 获得AES密钥
     * AES算法密钥加密流程：1-RSA算法私钥加密，2-BASE64算法加密
     * AES算法密钥解密流程：1-BASE64算法解密，2-RSA算法公钥解密
     *
     * @param encryptedAesKey 已加密的AES密钥
     * @param rsaPublicKey RSA算法公钥
     * @return
     */
    public static String decryptAesKey(String encryptedAesKey, String rsaPublicKey) throws Exception {
        return decryptByPubKey(CoderUtil.decryptBASE64(encryptedAesKey), rsaPublicKey);
    }


}
