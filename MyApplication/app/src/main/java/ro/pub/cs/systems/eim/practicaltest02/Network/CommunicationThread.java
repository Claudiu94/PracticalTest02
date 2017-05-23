package ro.pub.cs.systems.eim.practicaltest02.Network;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;
import ro.pub.cs.systems.eim.practicaltest02.General.Constants;
import ro.pub.cs.systems.eim.practicaltest02.General.Utilities;
import ro.pub.cs.systems.eim.practicaltest02.Model.StockData;

/**
 * Created by student on 23.05.2017.
 */

public class CommunicationThread extends Thread {
    private ServerThread serverThread;
    private Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (city / information type!");
            String stock = bufferedReader.readLine();
            if (stock == null || stock.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (city / information type!");
                return;
            }
            HashMap<String, StockData> data = serverThread.getData();
            StockData stockdata = null;
            String result = "no text";
            if (false && data != null && data.containsKey(stock)) {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                stockdata = data.get(stock);
            }
            if (false && stockdata != null) {
                result = stockdata.price + stockdata.time;
            }
            else {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");
                HttpClient httpClient = new DefaultHttpClient();
                String address = Constants.WEB_SERVICE_ADDRESS + Constants.QUERY_ATTRIBUTE + stock + Constants.QUERY_ATTRIBUTE1;
                HttpGet httpGet = new HttpGet(address);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                Log.d(Constants.TAG, address);

                String pageSourceCode = httpClient.execute(httpGet, responseHandler);
                if (pageSourceCode == null) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                    return;
                }
                else
                    result = pageSourceCode;
            }
            if (result != null) {
                String key = stock;
                String info[] = result.split(",");
                Log.d(Constants.TAG, info.toString());
                String price = info[0];
                Log.d(Constants.TAG, String.valueOf(price));
                String date = info[1].substring(1, info[1].length() - 1);
                date = date.substring(0, date.length() -1);
                serverThread.setData(key, new StockData(date, price));
                Log.d(Constants.TAG, date);
            }
//            if (stockdata == null) {
//                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Stock Data Information is null!");
//                return;
//            }
            Log.d(Constants.TAG, result);
            printWriter.println(result);
            printWriter.flush();
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }   finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }
}
