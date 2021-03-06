package com.zhketech.mstapp.client.land.project.callbacks;

import com.zhketech.mstapp.client.land.project.base.App;
import com.zhketech.mstapp.client.land.project.global.AppConfig;
import com.zhketech.mstapp.client.land.project.utils.ByteUtils;
import com.zhketech.mstapp.client.land.project.utils.Logutils;
import com.zhketech.mstapp.client.land.project.utils.SharedPreferencesUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

/**
 * 发送心跳到服务器
 * Created by Root on 2018/4/20.
 */

public class SendHearToServerThread extends  Thread {

    List<String> mList;
    byte[] timeByte;
    public SendHearToServerThread(List<String> mList, byte[] timeByte){
        this.mList = mList;
        this.timeByte = timeByte;
    }

    @Override
    public void run() {
        super.run();

        byte[] requestBytes = new byte[96];
        byte[] flag = "ZDHB".getBytes();// 数据标识头字节
        byte[] id = new byte[48];
        byte[] idKey = mList.get(0).getBytes();//手机唯一 的标识字节
        System.arraycopy(flag,0,requestBytes,0,4);
        System.arraycopy(idKey,0,id,0,idKey.length);
        System.arraycopy(id,0,requestBytes,4,48);

        byte[] stamp = new byte[8];
        byte[] timeT =timeByte;
        System.arraycopy(timeT,0,stamp,0,timeT.length);
        System.arraycopy(stamp,0,requestBytes,52,stamp.length);


      //  double lat1 = (double) SharedPreferencesUtils.getObject(App.getInstance(),"lat",0);
        double lat = 10;
       // Logutils.i(lat1+":lat");
//        if (lat1 != 0){
//             lat = (byte)lat1;
//        }else {
//             lat = 41.1;
//        }
        // 纬度
        byte[] latB = ByteUtils.getBytes(lat);

        System.arraycopy(latB, 0, requestBytes, 64, 8);
        // 经度

       // double lon1 = (double) SharedPreferencesUtils.getObject(App.getInstance(),"long",0);
       // Logutils.i(lon1+":long");
        double lon = 0;
//        if (lon1 != 0){
//            lon = (byte)lon1;
//        }else {
//             lon = 136.301;
//        }


        byte[] lonB = ByteUtils.getBytes(lon);
        // System.out.println("经度:" + Arrays.toString(lonB));
        // System.out.println(getDouble(lonB));
        System.arraycopy(lonB, 0, requestBytes, 72, 8);

        //设备状态
        //剩余电量
        byte[] power = new byte[1];
        int battery = (int) SharedPreferencesUtils.getObject(App.getInstance(),"battery",0);
        power[0] = (byte)battery;
        System.arraycopy(power, 0, requestBytes, 80, 1);
        //内存使用
        byte[] mem = new byte[1];
        double ram = (int) SharedPreferencesUtils.getObject(App.getInstance(),"ram",0);
        mem[0] = (byte)ram;
        System.arraycopy(mem, 0, requestBytes, 81, 1);
        //cpu使用
        byte[] cpu = new byte[1];
        double cpu1 = (int) SharedPreferencesUtils.getObject(App.getInstance(),"cpu",0);
        cpu[0] = (byte)cpu1;
        System.arraycopy(cpu, 0, requestBytes, 82, 1);
        //信号强度
        byte[] signal = new byte[1];
        int wifi = (int) SharedPreferencesUtils.getObject(App.getInstance(),"wifi",0);

        signal[0] =(byte)wifi;
        System.arraycopy(signal, 0, requestBytes, 83, 1);
        //弹箱连接状态
        byte[] bluetooth = new byte[1];
        bluetooth[0] = 0;
        System.arraycopy(bluetooth, 0, requestBytes, 84, 1);
        //保留
        byte[] save = new byte[11];
        System.arraycopy(save, 0, requestBytes, 85, 11);

        //把第56到59位的四个字节反转
         byte[] temp = new byte[4];
        System.arraycopy(requestBytes, 56, temp, 0, 4);
        byte[] reversalByte = new byte[4];
        reversalByte[0] = temp[3];
        reversalByte[1] = temp[2];
        reversalByte[2] = temp[1];
        reversalByte[3] = temp[0];
        System.arraycopy(reversalByte, 0, requestBytes, 56, 4);

        //建立UDP请求
        DatagramSocket socketUdp = null;
        try {
            socketUdp = new DatagramSocket(AppConfig.heart_port);
            DatagramPacket datagramPacket = new DatagramPacket(requestBytes, requestBytes.length, InetAddress.getByName(AppConfig.server_ip), AppConfig.heart_port);
            socketUdp.send(datagramPacket);
            socketUdp.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
