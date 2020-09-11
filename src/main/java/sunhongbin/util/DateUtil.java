package sunhongbin.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * created by SunHongbin on 2020/9/11
 */
public class DateUtil {

    public static String getCurrentTime(String dateFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

}
