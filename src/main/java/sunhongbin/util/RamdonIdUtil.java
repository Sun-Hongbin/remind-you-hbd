package sunhongbin.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.UUID;

/**
 * created by SunHongbin on 2020/9/11
 */
public class RamdonIdUtil {

    public static String getRandomIdWithUsrNam(String userName) {
        String ramdonId = DigestUtils.md5Hex(UUID.randomUUID().toString());
        return userName + ramdonId.substring(0, 10);
    }
}
