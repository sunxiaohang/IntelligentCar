# IntelligentCar
毕业设计智能车控制app

> 小车包括硬件实体（小车车架，树莓派，单片机，电源，驱动电机，导航舵机，摄像头移动舵机以及降压稳压模块） 树莓派作为主控制板实现控制码的接收及视频服务器数据支持(MJPG视频服务器上传媒体流数据到本地IP地址的8080端口，实现局域网视频数据流的实时传输。)单片机用于控 制小车的转向及移动。控制端包括Material Design制作的Android客户端,Web客户端以及基于C#的Windows客户端。


`车架参数`
>规格:248mm*146mm;
重量:724.5g;
驱动类型:单电机后轮驱动;
转向控制:5V舵机、转向杯;
车轮规格:65mm轮子*4;

`导航舵机参数`
>5V供电电压；
平衡角度90°(PWM占空比7.5%),
左转角度45°(PWM占空比5%),
右转角度135°(PWM占空比10%).

`电机参数`
>采用L298N电机驱动,占空比80%,
供电电压12V; 
空载转速rpm/min:592;空载电流A:0.08; 
负载转速rpm/min:475;负载电流A:0.6; 
额定力矩kg.cm:0.23;
堵转力矩kg*cm:2;堵转电流A:2;
减速箱长MM:17;重量/g:80;功率/W:2.3;

`摄像头参数`
>500W树莓派兼容摄像头
感光芯片OV5647
静态图片分辨率为2592 × 1944
支持1080p30, 720p60以及640×480p60/90视频录像
尺寸:25mm × 24mm × 9mm

`供电电源`
>规格：电压12V容量6800mah
电流：2A（带短路保护）
重量：228g
接口：为2.1*5.5mm
尺寸:长100mm*宽52mm*高25mm

####小车控制流程 终端 <路由器>树莓派
>`Android APP,Windows APP,网页中端发送控制码经路由器转发到树莓派。`

>树莓派设置静态IP地址为:192.168.1.88.

>`MJPG流媒体服务器IP地址192.168.1.88:8080,为客户端提供网页视频服务。`

>单片机接收树莓派发送的串口控制码执行左右移动及转向等操作。


####自动控制传感器注册
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

####重写onSensorChanged方法获取传感器参数用于控制小车移动
```
 @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        LinearLayout ll = (LinearLayout) findViewById(R.id.auto_control_panel);
        int offsetX = (ll.getWidth() - autocontrol.getWidth()) / 2;
        int offsetY = (ll.getHeight() - autocontrol.getHeight()) / 2;
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_GRAVITY://加速度传感器
                gravity[0] = sensorEvent.values[0];
                gravity[1] = sensorEvent.values[1];
                gravity[2] = sensorEvent.values[2];
                offsetX = (int) (offsetX - 50 * gravity[0]);
                offsetY = (int) (offsetY + 30 * gravity[1]);
                /***
                 * 状态码
                 */
                if (gravity[0] > 1) {
                    if (left) {
                        SendControlCode sc = new SendControlCode(3);
                        sc.execute();//左
                        left = false;
                        right = true;
                    }
                } else if (gravity[0] < -1) {
                    if (right) {
                        SendControlCode sc = new SendControlCode(4);
                        sc.execute();//右
                        right = false;
                        left = true;
                    }
                }
                if (gravity[1] < -1) {
                    if (up) {
                        SendControlCode sc = new SendControlCode(1);
                        sc.execute();//上
                        up = false;
                        down = true;
                    }
                } else if (gravity[1] > 1) {
                    if (down) {
                        SendControlCode sc = new SendControlCode(2);
                        sc.execute();//下
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


####Java服务器端 Date:2016/9/12;
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
