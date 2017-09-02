package zeusro.specialalarmclock.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 *
 *
 * @author lls
 * @since 2017/8/18 上午11:29
 */
public class DateTimeUtils {
    public static final int DAY_IN_SECOND = 24 * 60 * 60;
    public static final int DAY_IN_MILISECOND = 24 * 60 * 60 * 1000;

    /**
     * 格式化失败返回null
     *
     * @param date date
     * @param format format
     * @return 格式化的日期
     */
    public static String getFormatDate(long date, String format){
        try{
            SimpleDateFormat df = new SimpleDateFormat(format, Locale.CHINA);
            return df.format(date);
        }catch (IllegalArgumentException e){
            return null;
        }
    }
}
