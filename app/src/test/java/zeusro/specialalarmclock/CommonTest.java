package zeusro.specialalarmclock;

import org.junit.Test;

import java.util.Calendar;

import zeusro.specialalarmclock.utils.DateTimeUtils;

/**
 * @author lls
 * @since 2017/9/5 下午1:10
 */
public class CommonTest {
    @Test
    public void testDTU() {
        long date = DateTimeUtils.getDateFromString("10:00", "HH:mm");
        // 1970-01-01 10:00:00
        System.out.println(DateTimeUtils.getFormatDate(date, "yyyy-MM-dd HH:mm:ss"));
    }

    @Test
    public void testCalendar() {
        Calendar ca = Calendar.getInstance();
        System.out.println("The new instance of Calendar " + DateTimeUtils.getFormatDate(ca.getTimeInMillis(), "yyyy-MM-dd HH:mm:ss"));
        System.out.println("The day of year is " + ca.get(Calendar.DAY_OF_YEAR));
        ca.set(Calendar.DAY_OF_YEAR, 365);
        System.out.println("The 365th day of the year is " + DateTimeUtils.getFormatDate(ca.getTimeInMillis(), "yyyy-MM-dd"));
        ca.add(Calendar.DAY_OF_YEAR, 1);
        System.out.println("The next day of the 365th day is " + DateTimeUtils.getFormatDate(ca.getTimeInMillis(), "yyyy-MM-dd"));
    }
}