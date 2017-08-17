package zeusro.specialalarmclock.net;

import android.net.NetworkInfo;

/**
 *
 *
 * @author lls
 * @since 2017/8/17 下午6:01
 */

public interface DownloadCallback<T> {

    NetworkInfo getActiveNetworkInfo();

    void showResult(T result);
}
