package com.jladder.net;

import com.jladder.data.Receipt;
import com.jladder.data.Record;
import com.jladder.lang.Times;
import com.jladder.net.http.HttpHelper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * 网络工具包
 */
public class NetUtils {
    /**
     * 测试网络状况
     * @param host 主机地址
     * @param port 端口号
     * @param timeout 超时时间 毫秒为单位
     * @return
     */
    public synchronized static Receipt<Integer> ping(String host, int port, int timeout) {
        Socket socket = null;
        Long start = Times.getTime();
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host.trim(), port), timeout);
            return new Receipt<Integer>(true).setData((int)(Times.getTime() - start));
        } catch (UnknownHostException e) {
            return new Receipt<Integer>(false, "未找到主机").setData((int)(Times.getTime() - start));
        } catch (SocketTimeoutException e) {
            return new Receipt<Integer>(false, "连接超时").setData((int)(Times.getTime() - start));
        } catch (IOException e) {
            return new Receipt<Integer>(false, e.getMessage()).setData((int)(Times.getTime() - start));
        } catch(Exception e) {
            return new Receipt<Integer>(false, e.getMessage()).setData((int)(Times.getTime() - start));
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            }
            catch (Exception e) {
            }
        }
    }

    public static Receipt<String> ip(String ip) {
        String url = "http://opendata.baidu.com/api.php?query=" + ip + "&co=&resource_id=6006&oe=utf8";
        Receipt<String> ret = HttpHelper.request(url, null, "GET");
        if(!ret.isSuccess())return ret;
        Record data = Record.parse(ret.data);
        //ret = Record.parse(HttpHelper.get(service));
        if (data == null) return new Receipt<String>(false,"未查询到结果");
        Object value = data.find("data[0].location");
        return new Receipt<String>().setData(value!=null?value.toString():"");
    }




}
