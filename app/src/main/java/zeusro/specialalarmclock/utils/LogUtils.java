package zeusro.specialalarmclock.utils;

import android.util.Log;

/**
 *
 * Created by lls on 2017/9/20.
 */

public class LogUtils {
    /**
     * 可以跳转到Log所在行的Log.w
     * @param tag tag
     * @param msg msg
     */
    public static void w(String tag, String msg){
        StackTraceElement stackTrace = new Exception().getStackTrace()[1];
        String fileName = stackTrace.getFileName();
        final String info = String.format("%s\nat %s.%s(%s:%s)",msg,stackTrace.getClassName(),stackTrace.getMethodName(),fileName,stackTrace.getLineNumber());
        Log.w(tag,info);
    }
}
