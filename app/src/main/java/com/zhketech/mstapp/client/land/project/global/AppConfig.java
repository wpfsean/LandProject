package com.zhketech.mstapp.client.land.project.global;

/**
 * Created by Root on 2018/6/28.
 */

public class AppConfig {

    public AppConfig()

    {
        throw new UnsupportedOperationException("can not construct");
    }


    //true为主码流，false为子码流
    public static boolean isMainStream = false;


    public  static  int direction = 2;//(1竖屏，2横屏)

    //播放视频是否有声音
    public static  boolean isVideoSound = false;

    //数据编码格式
    public static String dataFormat = "GB2312";
    //数据头
    public static String video_header_id = "ZKTH";

    //登录端口
    public static int server_port = 2010;
    //发送心跳的端口
    public static int heart_port = 2020;
    //服务器ip
    public static String server_ip = "19.0.0.28";
    //本机Ip
    public static String current_ip = "19.0.0.78";
    public static String current_user = "admin";
    public static String current_pass = "pass";
    //sip服务器ip
    public static String native_sip_server_ip = "19.0.0.60";
    public static String native_sip_name = "7008";
    //sip服务器管理员密码
    public static String sipServerPass = "123456";
    //sip服务器获取所有的sip用户信息
    public static String sipServerDataUrl = "http://" + native_sip_server_ip + ":8080/openapi/localuser/list?{\"syskey\":\"" + sipServerPass + "\"}";


    public static String data = "";

    //云台水平方向移动速率
    public static String x = "0.2";
    //云台垂直方向移动速率
    public static String y = "0.2";
    //云台的缩放速率
    public static String s = "0.2";

    //发送报文地ip和port
    public static String alarm_server_ip = "19.0.0.27";
    public static  int alarm_server_port = 2000;


    //值班室信息
 //   public static final String DUTY_ROOM_URL = "http://wang1210.gz01.bdysite.com/zhketech/dutyRoomData.php";\

    public static final String DUTY_ROOM_URL = "http://wesk.top/zhketech/dutyRoomData.php";


    /**
     * 蓝牙信息
     */
    public static String blueToothMac = "D0:33:8B:F6:1A:84";
    public static String serviceUuid = "0000FFF0-0000-1000-8000-00805F9B34FB";
    public static String charateristicUuid = "0000FFF6-0000-1000-8000-00805F9B34FB";

}
