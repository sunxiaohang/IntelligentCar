# IntelligentCar
��ҵ������ܳ�����app

> С������Ӳ��ʵ�壨С�����ܣ���ݮ�ɣ���Ƭ������Դ������������������������ͷ�ƶ�����Լ���ѹ��ѹģ�飩 ��ݮ����Ϊ�����ư�ʵ�ֿ�����Ľ��ռ���Ƶ����������֧��(MJPG��Ƶ�������ϴ�ý�������ݵ�����IP��ַ��8080�˿ڣ�ʵ�־�������Ƶ��������ʵʱ���䡣)��Ƭ�����ڿ� ��С����ת���ƶ������ƶ˰���Material Design������Android�ͻ���,Web�ͻ����Լ�����C#��Windows�ͻ��ˡ�


`���ܲ���`
>���:248mm*146mm;
����:724.5g;
��������:�������������;
ת�����:5V�����ת��;
���ֹ��:65mm����*4;

`�����������`
>5V�����ѹ��
ƽ��Ƕ�90��(PWMռ�ձ�7.5%),
��ת�Ƕ�45��(PWMռ�ձ�5%),
��ת�Ƕ�135��(PWMռ�ձ�10%).

`�������`
>����L298N�������,ռ�ձ�80%,
�����ѹ12V; 
����ת��rpm/min:592;���ص���A:0.08; 
����ת��rpm/min:475;���ص���A:0.6; 
�����kg.cm:0.23;
��ת����kg*cm:2;��ת����A:2;
�����䳤MM:17;����/g:80;����/W:2.3;

`����ͷ����`
>500W��ݮ�ɼ�������ͷ
�й�оƬOV5647
��̬ͼƬ�ֱ���Ϊ2592 �� 1944
֧��1080p30, 720p60�Լ�640��480p60/90��Ƶ¼��
�ߴ�:25mm �� 24mm �� 9mm

`�����Դ`
>��񣺵�ѹ12V����6800mah
������2A������·������
������228g
�ӿڣ�Ϊ2.1*5.5mm
�ߴ�:��100mm*��52mm*��25mm

####С���������� �ն� <·����>��ݮ��
>`Android APP,Windows APP,��ҳ�ж˷��Ϳ����뾭·����ת������ݮ�ɡ�`

>��ݮ�����þ�̬IP��ַΪ:192.168.1.88.

>`MJPG��ý�������IP��ַ192.168.1.88:8080,Ϊ�ͻ����ṩ��ҳ��Ƶ����`

>��Ƭ��������ݮ�ɷ��͵Ĵ��ڿ�����ִ�������ƶ���ת��Ȳ�����


####�Զ����ƴ�����ע��
```
sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fabstatus == true) {
                    fabstatus = false;
                    ObjectAnimator.ofFloat(autocontrol, "translationX", 0.0F, -200.0F).setDuration(400).start();
                    ObjectAnimator.ofFloat(handcontrol, "translationX", 0.0F, -400.0F).setDuration(600).start();
                    RotateAnimation ra = new RotateAnimation(0, 225, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    ra.setDuration(600);
                    ra.setFillAfter(true);
                    add.startAnimation(ra);
                } else {
                    fabstatus = true;
                    ObjectAnimator.ofFloat(autocontrol, "translationX", -200.0F, 0.0F).setDuration(400).start();
                    ObjectAnimator.ofFloat(handcontrol, "translationX", -400.0F, 0.0F).setDuration(600).start();
                    RotateAnimation ra = new RotateAnimation(255, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    ra.setDuration(600);
                    ra.setFillAfter(true);
                    add.startAnimation(ra);
                }
            }

        });
```

####��дonSensorChanged������ȡ�������������ڿ���С���ƶ�
```
 @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        LinearLayout ll = (LinearLayout) findViewById(R.id.auto_control_panel);
        int offsetX = (ll.getWidth() - autocontrol.getWidth()) / 2;
        int offsetY = (ll.getHeight() - autocontrol.getHeight()) / 2;
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_GRAVITY://���ٶȴ�����
                gravity[0] = sensorEvent.values[0];
                gravity[1] = sensorEvent.values[1];
                gravity[2] = sensorEvent.values[2];
                offsetX = (int) (offsetX - 50 * gravity[0]);
                offsetY = (int) (offsetY + 30 * gravity[1]);
                /***
                 * ״̬��
                 */
                if (gravity[0] > 1) {
                    if (left) {
                        SendControlCode sc = new SendControlCode(3);
                        sc.execute();//��
                        left = false;
                        right = true;
                    }
                } else if (gravity[0] < -1) {
                    if (right) {
                        SendControlCode sc = new SendControlCode(4);
                        sc.execute();//��
                        right = false;
                        left = true;
                    }
                }
                if (gravity[1] < -1) {
                    if (up) {
                        SendControlCode sc = new SendControlCode(1);
                        sc.execute();//��
                        up = false;
                        down = true;
                    }
                } else if (gravity[1] > 1) {
                    if (down) {
                        SendControlCode sc = new SendControlCode(2);
                        sc.execute();//��
                        down = false;
                        up = true;
                    }
                }
                layoutParams.leftMargin = offsetX;
                layoutParams.topMargin = offsetY;
                position_circle.setLayoutParams(layoutParams);
                break;
        }
    }
```


####Java�������� Date:2016/9/12;
```
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiClient extends Thread {
    private Socket client;
    private String controlcode = "";

    public MultiClient(Socket c) {
        this.client = c;
    }
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream());
            // Mutil User but can't parallel
            while (true) {
                String str = in.readLine();
                if (str == null) break;
                System.out.println(str);
                if (str.equals("110")) {
                    String[] loadvideo = {"/bin/bash", "-c", "/home/pi/Pictures/video.sh  >/dev/null 2>&1 &"};
                    java.lang.Process process = Runtime.getRuntime().exec(loadvideo);
                } else if (str.equals("119")) {
                    String[] command1 = {"/bin/bash", "-c", "killall -9 mjpg_streamer"};
                    String[] command2 = {"/bin/bash", "-c", "killall -9 video.sh"};
                    java.lang.Process process1 = Runtime.getRuntime().exec(command1);
                    java.lang.Process process2 = Runtime.getRuntime().exec(command2);
                } else {
                    controlcode = "echo -n " + str + " >/dev/ttyUSB0";
                    String[] sendcode = {"/bin/bash", "-c", controlcode};
                    java.lang.Process process = Runtime.getRuntime().exec(sendcode);
                    out.println("has receive....");
                    out.flush();
                }
            }
            client.close();
        } catch (IOException ex) {
        } finally {
        }
    }
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(6789);
        while (true) {
            // transfer location change Single User or Multi User
            MultiClient mc = new MultiClient(server.accept());
            mc.start();
        }
    }
}
```
