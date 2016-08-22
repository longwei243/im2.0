package com.moor.im.tcp.manager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.moor.im.app.MobileApplication;
import com.moor.im.common.utils.TimeUtil;
import com.moor.im.common.utils.Utils;

import com.moor.imkf.netty.bootstrap.ClientBootstrap;
import com.moor.imkf.netty.buffer.ChannelBuffer;
import com.moor.imkf.netty.buffer.ChannelBuffers;
import com.moor.imkf.netty.channel.Channel;
import com.moor.imkf.netty.channel.ChannelFactory;
import com.moor.imkf.netty.channel.ChannelFuture;
import com.moor.imkf.netty.channel.ChannelFutureListener;
import com.moor.imkf.netty.channel.ChannelPipeline;
import com.moor.imkf.netty.channel.ChannelPipelineFactory;
import com.moor.imkf.netty.channel.Channels;
import com.moor.imkf.netty.channel.SimpleChannelHandler;
import com.moor.imkf.netty.channel.socket.nio.NioClientSocketChannelFactory;
import com.moor.imkf.netty.handler.codec.frame.LineBasedFrameDecoder;
import com.moor.imkf.netty.handler.timeout.IdleStateHandler;
import com.moor.imkf.netty.util.HashedWheelTimer;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created by longwei on 2016/3/23.
 */
public class SocketThread extends Thread{

    private SocketManager socketManager;

    private ClientBootstrap clientBootstrap = null;
    private ChannelFactory channelFactory = null;
    private ChannelFuture channelFuture = null;
    private Channel channel = null;

    private String ipAddress;
    private int ipPort;
    private String name;
    private String password;
    //重连次数
    private int connTryTimes = 0;
    //是否正在连接
    private boolean connecting = true;
    public void setConnecting(boolean connecting){
        this.connecting = connecting;
    }
    public boolean isConnecting() {
        return connecting;
    }

    public SocketThread(SocketManager socketManager, String ipAddress, int ipPort, SimpleChannelHandler handler, String name, String password) {
        this.socketManager = socketManager;
        this.ipAddress = ipAddress;
        this.ipPort = ipPort;
        this.name = name;
        this.password = password;
        init(handler);
    }
    @Override
    public void run() {
        doConnect();
    }

    /**
     * 初始化连接配置项
     * @param handler 负责业务数据的处理
     */
    private void init(final SimpleChannelHandler handler) {
        socketManager.logger.debug(TimeUtil.getCurrentTime()+"SocketThread:进入init方法");
        try {
            channelFactory = new NioClientSocketChannelFactory(
                    Executors.newSingleThreadExecutor(),
                    Executors.newSingleThreadExecutor());

            clientBootstrap = new ClientBootstrap(channelFactory);
            clientBootstrap.setOption("connectTimeoutMillis", 2000);
            clientBootstrap.setPipelineFactory(new ChannelPipelineFactory() {

                public ChannelPipeline getPipeline() throws Exception {
                    ChannelPipeline pipeline = Channels.pipeline();
                    // 接收的数据包解码
                    pipeline.addLast("decoder", new LineBasedFrameDecoder(1024));
                    // 发送的数据包编码
                    //pipeline.addLast("encoder", new PacketEncoder());
                    // 具体的业务处理，这个handler只负责接收数据，并传递给dispatcher
                    pipeline.addLast("IdleStateHandler", new IdleStateHandler(new HashedWheelTimer(), 490, 490, 490));
                    pipeline.addLast("handler", handler);
                    return pipeline;

                }

            });

            clientBootstrap.setOption("tcpNoDelay", true);
            clientBootstrap.setOption("keepAlive", true);
        }catch (Exception e) {
            Log.d("SocketThread", "netty 初始化失败了");
        }
    }

    /**
     * 进行连接
     * @return
     */
    public void doConnect() {
        socketManager.logger.debug(TimeUtil.getCurrentTime()+"SocketThread:进入doConnect方法");
        socketManager.logger.debug(TimeUtil.getCurrentTime()+"SocketThread:循环了一次:"+connTryTimes);
        connTryTimes++;
        try {
            if(channel != null){
                this.close();
                channel = null;
            }
            socketManager.setStatus(SocketStatus.CONNECTING);
            // Start the connection attempt.
            channelFuture = clientBootstrap.connect(new InetSocketAddress(
                    ipAddress, ipPort));
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if(channelFuture.isSuccess()) {
                        //连接成功
                        channel = channelFuture.getChannel();
                        socketManager.logger.debug(TimeUtil.getCurrentTime()+"SocketThread:tcp连接成功");
                        socketManager.setStatus(SocketStatus.CONNECTED);
                        login();
                    }else {
                        if(connTryTimes < 10) {
                            Thread.sleep(1000);
                        }else if(connTryTimes > 10 && connTryTimes < 30) {
                            Thread.sleep(3000);
                        }else if(connTryTimes > 30 && connTryTimes < 100) {
                            Thread.sleep(5000);
                        }if(connTryTimes> 150) {
                            Thread.sleep(20000);
                        }
                        doConnect();
                    }
                }
            });
        } catch (Exception e) {

        }
    }

    /**
     * 向服务器发送数据
     * @param data
     * @return
     */
    public boolean sendData(String data) {

        Channel currentChannel =  channelFuture.getChannel();
        boolean isW = currentChannel.isWritable();
        boolean isC  = currentChannel.isConnected();
        if(!(isW && isC)){
            System.out.println("sendData has Exception");
            throw  new RuntimeException("#sendData#channel is close!");
        }
        ChannelBuffer buffer = ChannelBuffers.buffer(data.length());
        buffer.writeBytes(data.getBytes());
        channelFuture.getChannel().write(buffer);
        socketManager.logger.debug(TimeUtil.getCurrentTime()+"SocketThread:向服务器发送数据:"+data);
        return true;
    }

    public void close() {
        if (null == channelFuture)
            return;
        if (null != channelFuture.getChannel()) {
            channelFuture.getChannel().close();
            channelFactory.releaseExternalResources();
            socketManager.logger.debug(TimeUtil.getCurrentTime()+"SocketThread:tcp关闭");
        }

    }

    public boolean isClose(){
        if(channelFuture != null && channelFuture.getChannel() != null){
            return !channelFuture.getChannel().isConnected();
        }
        return true;
    }

    private void login() {
        if(socketManager.getStatus().equals(SocketStatus.CONNECTED)) {
            System.out.println("登陆方法判断到了 CONNECTED 状态");
            int rom = (int) (Math.random() * 900) + 100;

            JSONObject jb = new JSONObject();
            try {
                jb.put("Action", "login");
                jb.put("LoginName", name);
                jb.put("Platform", "android");
                jb.put("RandomKey", rom + "");
                jb.put("MD5", Utils.getMD5(name + rom + password));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String str = "1" + jb.toString() + "\n";
//			MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "发送登陆请求" + name + "  " + password);
            socketManager.sendData(str);
            socketManager.setStatus(SocketStatus.WAIT_LOGIN);
            socketManager.logger.debug(TimeUtil.getCurrentTime()+"SocketThread:tcp登录:"+str);
        }else {
        }
    }

    public Channel getChannel() {
        return channel;
    }
}
