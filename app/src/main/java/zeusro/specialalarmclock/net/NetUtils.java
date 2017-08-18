package zeusro.specialalarmclock.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 *
 * @author lls
 * @since 2017/8/17 下午6:33
 */

public class NetUtils {

    public static String get(URL url) throws IOException {
        InputStream stream = null;
        HttpURLConnection connection = null;
        String result = null;
        try{
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if(responseCode==HttpURLConnection.HTTP_OK){
                stream = connection.getInputStream();
                if(stream!=null){
                    result = readStream(stream,500);
                }
            }else{
                throw new IOException("HTTP error code: " + responseCode);
            }
        }finally {
            if(connection!=null){
                connection.disconnect();
            }
            if(stream!=null){
                stream.close();
            }
        }
        return result;
    }

    private static String readStream(InputStream stream, int maxReadSize) throws IOException {
        Reader reader = new InputStreamReader(stream,"UTF-8");
        char[] rawBuffer = new char[maxReadSize];
        int readSize;
        StringBuilder buffer = new StringBuilder();
        while((readSize = reader.read(rawBuffer)) != -1 && maxReadSize>0){
            if (readSize > maxReadSize){
                readSize = maxReadSize;
            }
            buffer.append(rawBuffer, 0, readSize);
            maxReadSize -= readSize;
        }
        return buffer.toString();
    }
}
