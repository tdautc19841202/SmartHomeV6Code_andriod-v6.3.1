package cc.wulian.smarthomev6.main.device.hisense;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hismart.easylink.localjni.DevCallBack;
import com.hismart.easylink.localjni.DevInfoLocal;
import com.hismart.easylink.localjni.EasylinkUtil;
import com.hismart.easylink.localjni.WiFiInfo;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import cc.wulian.smarthomev6.main.application.MainApplication;
import cc.wulian.smarthomev6.support.utils.LogUtil;
import cc.wulian.smarthomev6.support.utils.ToastUtil;
import cc.wulian.smarthomev6.support.utils.WLog;

/**
 * created by huxc  on 2018/1/2.
 * func： 海信WiFi设置类
 * email: hxc242313@qq.com
 */

public class HisenseWifiSet {
    private HisenseWifiSet() {
    }

    public List<String> sameLANDevArr;
    public String ssid;//被连接网络的MAC地址
    public String pass;//wifi密码
    public List<WiFiInfo> wifiListArr = null;
    private static String TAG = "hisense";
    private EasylinkUtil easylinkUtil;
    public static HisenseWifiSet sharedDeviceWifiSetInstance = null;

    public static HisenseWifiSet sharedDeviceWifiSet() {
        if (sharedDeviceWifiSetInstance == null) {
            sharedDeviceWifiSetInstance = new HisenseWifiSet();
            if (sharedDeviceWifiSetInstance.wifiListArr == null) {
                sharedDeviceWifiSetInstance.wifiListArr = new ArrayList<>();
            }
        }
        return sharedDeviceWifiSetInstance;
    }

    public void initDeviceWifISet() {
        this.startWifiServer();
    }

    private void startWifiServer() {
        easylinkUtil = new EasylinkUtil();
//        easylinkUtil.easylink_StopDeviceSmart();//停止设备配置
//        easylinkUtil.easylink_StopStatck();//停止WIFI服务
//        easylinkUtil.easylink_registerDevCallback(devCallback);//设备列表上下线监听函数
        String deviceIP = getIPAddress(MainApplication.getApplication().getApplicationContext());
        if (!TextUtils.isEmpty(deviceIP)) {
            int starWifiServerFlag = easylinkUtil.easylink_StartStatck(deviceIP);//启动wifi服务
            if (starWifiServerFlag != 0) {
                WLog.i(TAG, "第1次启动wifi服务失败 result=" + starWifiServerFlag);
                LogUtil.WriteBcLog("第1次启动wifi服务失败 result=" + starWifiServerFlag);
                starWifiServerFlag = easylinkUtil.easylink_StopStatck();//停止WIFI服务
                if (starWifiServerFlag != 0) {
                    WLog.i(TAG, "第2次启动wifi服务失败 result=" + starWifiServerFlag);
                    LogUtil.WriteBcLog("第2次启动wifi服务失败 result=" + starWifiServerFlag);
                }
            }
            if (starWifiServerFlag == 0) {
                easylinkUtil.easylink_registerDevCallback(devCallback);//设备列表上下线监听函数
                WLog.i(TAG, "启动wifi服务成功");
                LogUtil.WriteBcLog("启动wifi服务成功");
            }
        } else {
            WLog.i(TAG, "startWifiServer: 未获取到IP");
            LogUtil.WriteBcLog("startWifiServer: 未获取到IP");
        }
    }

    public void stopWifiServer() {
        easylinkUtil.easylink_StopDeviceSmart();
//        easylinkUtil.easylink_deRegisterDevCallback(devCallback);
        easylinkUtil.easylink_StopStatck();

    }

    /**
     * 用于设备上下线的回调
     */
    DevCallBack devCallback = new DevCallBack() {
        @Override
        public void devCb(int i, WiFiInfo wiFiInfo) {
            StringBuilder strbuilder = new StringBuilder();
            strbuilder.append("*********设备上下线监听函数信息*********\r\ni");
            strbuilder.append("标识:" + wiFiInfo.DID + "\r\n");//这个东西就是wifiid
            strbuilder.append("版本:" + wiFiInfo.RID + "\r\n");
            strbuilder.append("协议版本号:" + wiFiInfo.PLVer + "\r\n");
            strbuilder.append("厂家型号:" + wiFiInfo.CInfo + "\r\n");
            strbuilder.append("设备类型:" + wiFiInfo.HType + "\r\n");
            strbuilder.append("模块状态:" + wiFiInfo.HState + "\r\n");
            strbuilder.append("模块信息版本:" + wiFiInfo.HCause + "\r\n");
            strbuilder.append("描述或控制Uri:" + wiFiInfo.Uri + "\r\n");
            strbuilder.append("设备条形码:" + wiFiInfo.Barcode + "\r\n");

            strbuilder.append("设备ip:" + wiFiInfo.ip + "\r\n");
            strbuilder.append("TCP端口:" + wiFiInfo.ConnPort + "\r\n");

            int devCount = 0;
            if (wiFiInfo.devList != null) {
                devCount = wiFiInfo.devList.size();
                strbuilder.append("子设备数量:" + devCount + "\r\n");
                for (DevInfoLocal devInfoLocal : wiFiInfo.devList) {
                    strbuilder.append("----子设备devId:" + devInfoLocal.devId + "\r\n");
                    strbuilder.append("----子设备Barcode:" + devInfoLocal.Barcode + "\r\n");
                    strbuilder.append("----子设备DevType:" + devInfoLocal.DevType + "\r\n");
                    strbuilder.append("----子设备DState:" + devInfoLocal.DState + "\r\n");
                }
            }

            strbuilder.append("*********设备上下线监听函数信息*********\r\n");
            WLog.i(TAG, "devCallback: \r\n" + strbuilder);
            LogUtil.WriteBcLog("devCallback: \r\n" + strbuilder.toString());
            addWifiInfo(wiFiInfo);
        }
    };

    private void addWifiInfo(WiFiInfo wiFiInfo) {
        if (wifiListArr == null) {
            wifiListArr = new ArrayList<>();
        }
        WiFiInfo arrValue = getWifiInfoFromArr(wiFiInfo.DID);
        if (arrValue != null) {
            wifiListArr.add(wiFiInfo);
        }
    }

    private WiFiInfo getWifiInfoFromArr(String did) {
        WiFiInfo wiFiInfo = null;
        if (wifiListArr.size() > 0) {
            for (WiFiInfo wfi : wifiListArr) {
                if (!wfi.DID.equals(did)) {
                    wiFiInfo = wfi;
                    break;
                }
            }
        }
        return wiFiInfo;
    }


    public  List<WiFiInfo> getWifiList() {
        return wifiListArr;
    }

    private DevInfoLocal getDevInfoLoacl(List<DevInfoLocal> devArr, String devId) {
        DevInfoLocal devInfoLocal = null;
        for (DevInfoLocal tempDev : devArr) {
            if (tempDev.devId.equals(devId)) {
                devInfoLocal = tempDev;
                break;
            }
        }
        return devInfoLocal;
    }

    /**
     * 设置wifi名称及密码
     *
     * @param wifiname wifi名称
     * @param wifipass wifi密码
     * @return
     */
    public void setDeviceWifiSetSsid(String wifiname, String wifipass) {
        this.ssid = wifiname;
        this.pass = wifipass;

        String netMac = getSsidAdress(MainApplication.getApplication().getApplicationContext());
        if (!TextUtils.isEmpty(netMac)) {
            int configResult = easylinkUtil.easylink_StartDeviceSmartConfig(netMac, netMac.length(), this.pass, this.pass.length());
            if (configResult != 0) {
                WLog.i(TAG, "StartDeviceSmartConfig启动失败，resultCode=" + configResult);
                LogUtil.WriteBcLog("StartDeviceSmartConfig启动失败，resultCode=" + configResult);
            } else {
                WLog.i(TAG, "StartDeviceSmartConfig启动成功！");
                LogUtil.WriteBcLog("StartDeviceSmartConfig启动成功");
            }
        } else {
            WLog.i(TAG, "未获取到MAC地址！");
        }
        WLog.i(TAG, "wifiListArr=" + this.wifiListArr.size());
//        return wifiListArr;
    }

    /**
     * 获取json对象
     *
     * @return
     */
    public static JSONArray GetJsonForGetwifi(List<WiFiInfo> wifiInfoList) {
        JSONArray jsonArray = new JSONArray();
        for (WiFiInfo item : wifiInfoList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("did", item.DID);
            String deviceNickName = "";
            jsonObject.put("deviceNickName", deviceNickName);
            if (item.HType == 0) {
                if (item.devList != null && item.devList.size() > 0) {
                    int devType = item.devList.get(0).DevType;
                    jsonObject.put("HType", devType);
                }
            } else {
                jsonObject.put("HType", item.HType);
            }
            WLog.i(TAG, "Htype=" + jsonObject.getString("HType"));
            jsonArray.add(jsonObject);
        }
        WLog.i(TAG, "GetJsonForGetwifi:" + jsonArray.toJSONString());
        return jsonArray;
    }

    /**
     * 获取IP地址,该IP地址是当前手机的Ip地址
     *
     * @param context
     * @return
     */
    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress()).replace("\"", "");//得到IPV4地址
                WLog.i(TAG, "getIPAddress: " + ipAddress);
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

    /**
     * 获取手机的mac地址
     *
     * @param context
     * @return
     */
    private String getSsidAdress(Context context) {
        String netMac = "";
        WifiManager mWifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (mWifi.isWifiEnabled()) {
            WifiInfo wifiInfo = mWifi.getConnectionInfo();
            netMac = wifiInfo.getBSSID(); //获取被连接网络的mac地址
        }
        return netMac;
    }
}
