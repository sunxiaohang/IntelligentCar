package com.iflytek.IntenligentCar;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by m1320 on 2016/9/6.
 * 发送控制码类，AsyncTask，简化Socket通信过程，
 */
public class SendControlCode extends AsyncTask {
    private int controlCode;
    private Socket socket;

    public SendControlCode(int _controlCode) {
        this.controlCode = _controlCode;
        try {
            socket = new Socket("192.168.1.88", 6789);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected Object doInBackground(Object[] objects) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.println(controlCode);
            out.flush();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
