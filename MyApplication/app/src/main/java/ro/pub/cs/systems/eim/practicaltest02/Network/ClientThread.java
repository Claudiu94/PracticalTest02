package ro.pub.cs.systems.eim.practicaltest02.Network;

import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import ro.pub.cs.systems.eim.practicaltest02.General.Constants;
import ro.pub.cs.systems.eim.practicaltest02.General.Utilities;

/**
 * Created by student on 23.05.2017.
 */

public class ClientThread extends Thread{
    private String address;
    private int port;
    private String stock;
    private String informationType;
    private TextView weatherForecastTextView;

    private Socket socket;

    public ClientThread(String address, int port, String stock, TextView weatherForecastTextView) {
        this.address = address;
        this.port = port;
        this.stock = stock;
        this.weatherForecastTextView = weatherForecastTextView;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(address, port);
            if (socket == null) {
                Log.e(Constants.TAG, "[CLIENT THREAD] Could not create socket!");
                return;
            }
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[CLIENT THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            Log.d(Constants.TAG, "Heredfffffffffffffg");
            printWriter.println(stock);
            printWriter.flush();
            String info;
            Log.d(Constants.TAG, "Heredfffffffffffffg");
            final String datafronserver = bufferedReader.readLine();
            weatherForecastTextView.post(new Runnable() {
                @Override
                public void run() {
                    weatherForecastTextView.setText(datafronserver);
                }
            });
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }
}
