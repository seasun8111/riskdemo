package util;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * @Title:AesECBUtil.java
 * @Package:com.tuniu.finance.xff.vca.utils
 * @author: dengweiwei
 * @date:2016年11月12日 下午3:45:15
 * 算法模式：ECB 密钥
 * 长度：128bits 16位长
 * 偏移量： 默认
 * 补码方式：PKCS5Padding
 * 解密串编码方式：base64
 */

public class AesECBUtil {

    private static final String AES_KEY = "ZK34+THR+hP7546ww1utSz39mQRAyzCb3uinQPVTtoAhkuGrJg5QTVMHxKwCZK/GvVHHD4blyrE4IWeEE4SnFvKYLvLEhc15yN5k1jVD4BOUeSDjMHIlunpiCVeIwY547MKekpTE7cbOUH9wMmFpvyl1A0wYs9uh97XHf76Clrk=";

    public static final String PUB_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCcC2kVgzBvgGFKvIW6g3OhAV2SBaPxHJ0A/qsh/jq4bK8i9Ta3p8D5VTfeWJd4PSRqPxjKLgmxC9eEY+YOXinP/v2l5IY5tO0KD9flAjE+npCFoyXtB8coJIucqsBKMY399IJYHAC531BnpDHZbEc2pm/O/9doIRzKgITxcgejFwIDAQAB";

    /**
     * 加密
     *
     * @param sSrc
     * @param sKey
     * @return
     */
    public static String encrypt(String sSrc, String sKey) {
        if (StringUtils.isBlank(sSrc)) {
            return sSrc;
        }
        try {
            if (sKey.length() != 16) {
                System.out.println("the key should be 16 位长");
                return null;
            }
            byte[] raw = sKey.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            //"算法/模式/补码方式"
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));
            //此处使用BASE64做转码功能，同时能起到2次加密的作用。
            return new Base64().encodeToString(encrypted);
        } catch (Exception e) {
            System.out.println("AesECB Encrypt exception"+ e.getMessage());
            return null;
        }
    }


    public static void main(String[] args) {
        try {
            String phoneNo = AesECBUtil.encrypt("18018580713", RSACoderUtil.decryptAesKey(AES_KEY, PUB_KEY));
            System.out.println(phoneNo);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

