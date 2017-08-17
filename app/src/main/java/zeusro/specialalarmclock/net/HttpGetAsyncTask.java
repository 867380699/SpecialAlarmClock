package zeusro.specialalarmclock.net;

import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 *
 * @author lls
 * @since 2017/8/17 下午6:38
 */

public class HttpGetAsyncTask extends AsyncTask<String, Void, String> {
    private DownloadCallback<String> callback;

    public HttpGetAsyncTask(DownloadCallback<String> callback) {
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        if(this.callback!=null){
            NetworkInfo networkInfo = this.callback.getActiveNetworkInfo();
            if(networkInfo==null || !networkInfo.isConnected()){
                cancel(true);
            }
        }
    }

    @Override
    protected String doInBackground(String... params) {
        if(params[0]!=null){
            if (!isCancelled()){
                try {
                    URL url = new URL(params[0]);
                    String resultString = NetUtils.downloadUrl(url);
                    if(resultString!=null){
                        return resultString;
                    }else{
                        throw new IOException("No response received");
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    @Override
    protected void onPostExecute(String result) {
        if(result!=null && this.callback!=null){
            this.callback.showResult(result);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
