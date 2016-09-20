package com.moor.im.tcp.manager;

import android.content.Context;

import com.moor.im.app.MobileApplication;
import com.moor.im.common.db.dao.InfoDao;
import com.moor.im.common.utils.TimeUtil;
import com.moor.im.tcp.event.LoginFailedEvent;
import com.moor.im.tcp.event.LoginKickedEvent;
import com.moor.im.tcp.event.LoginSuccessEvent;
import com.moor.im.tcp.event.MsgEvent;
import com.moor.im.tcp.event.NetStatusEvent;
import com.moor.im.tcp.event.NewOrderEvent;
import com.moor.im.tcp.eventbus.EventBus;

import com.moor.imkf.netty.buffer.ChannelBuffer;
import com.moor.imkf.netty.channel.Channel;
import com.moor.imkf.netty.channel.ChannelHandlerContext;
import com.moor.imkf.netty.channel.ChannelStateEvent;
import com.moor.imkf.netty.channel.ExceptionEvent;
import com.moor.imkf.netty.channel.MessageEvent;
import com.moor.imkf.netty.handler.timeout.IdleStateAwareChannelHandler;
import com.moor.imkf.netty.handler.timeout.IdleStateEvent;

import java.nio.charset.Charset;



/**
 * tcp数据的处理器,接收到对应的数据后将对应的事件通过广播发送出去
 * @author LongWei
 *
 */
public class ServerMessageHandler extends IdleStateAwareChannelHandler {

	private Context context;

	
	public ServerMessageHandler(Context context) {
		this.context = context;
	}
	
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		super.channelConnected(ctx, e);
		SocketManager.getInstance(context).logger.error(TimeUtil.getCurrentTime()+"ServerMessageHandler:tcp连接成功");
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
        /**
         * 1. 已经与远程主机建立的连接，远程主机主动关闭连接，或者网络异常连接被断开的情况
         2. 已经与远程主机建立的连接，本地客户机主动关闭连接的情况
         3. 本地客户机在试图与远程主机建立连接时，遇到类似与connection refused这样的异常，未能连接成功时
         而只有当本地客户机已经成功的与远程主机建立连接（connected）时，连接断开的时候才会触发channelDisconnected事件，即对应上述的1和2两种情况。
         *
         **/
  		super.channelDisconnected(ctx, e);
		SocketManager.getInstance(context).logger.error(TimeUtil.getCurrentTime()+"ServerMessageHandler:tcp连接断开");
		SocketManager.getInstance(context).setStatus(SocketStatus.BREAK);
		EventBus.getDefault().post(NetStatusEvent.NET_RECONNECT);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		super.messageReceived(ctx, e);
		if(ctx.getChannel().getId() != SocketManager.getInstance(MobileApplication.getInstance()).getChannelId()) {
			return;
		}
		ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
        String result = buffer.toString(Charset.defaultCharset());
		SocketManager.getInstance(context).logger.error(TimeUtil.getCurrentTime()+"ServerMessageHandler:服务器返回的数据是：" + result);

        if ("3".equals(result)) {
        	//心跳管理器负责
		} else if ("4".equals(result)) {
			//被踢了
			//发送被踢了的事件
			SocketManager.getInstance(context).setLoginKicked(true);
			EventBus.getDefault().post(new LoginKickedEvent());
			SocketManager.getInstance(context).logger.debug(TimeUtil.getCurrentTime()+"ServerMessageHandler:被踢了");
		}else if ("100".equals(result)) {
			//有新消息之后的处理
			EventBus.getDefault().post(new MsgEvent());
		} else if ("400".equals(result)) {
			//登录失败，用户名或密码错误
			//发送登录失败的事件
			InfoDao.getInstance().saveIsChangePW("true");
			SocketManager.getInstance(context).setStatus(SocketStatus.LOGINFAILED);
			EventBus.getDefault().post(new LoginFailedEvent());
		} else if(result.startsWith("200")) {
			SocketManager.getInstance(context).setStatus(SocketStatus.LOGINED);
			String connectionId = result.substring(3);
			//保存connectionId到数据库中
			InfoDao.getInstance().saveConnectionId(connectionId);
			InfoDao.getInstance().setLoginStateToSuccess();
			InfoDao.getInstance().saveIsChangePW("false");
			SocketManager.getInstance(context).logger.error(TimeUtil.getCurrentTime()+"ServerMessageHandler:connectionId被保存了" + connectionId);
			EventBus.getDefault().post(new LoginSuccessEvent(connectionId));
		}else if("800".equals(result)) {
			//有新的工单
			EventBus.getDefault().post(new NewOrderEvent());
		} else {
//			MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "ServerMessageHandler，服务器返回的数据是："+ result+" 未知的标示");
		}
	}

    /**
	 *
     * */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        super.exceptionCaught(ctx, e);

		if(ctx.getChannel().getId().equals(SocketManager.getInstance(context).getChannelId())){
//				MobileApplication.logger.debug(TimeUtil.getCurrentTime() + "tcp1 开始连：");
			//发送tcp服务器连接断开的事件
			Channel ch = e.getChannel();
			ch.close();
			SocketManager.getInstance(context).setStatus(SocketStatus.BREAK);
			EventBus.getDefault().post(NetStatusEvent.NET_RECONNECT);
			SocketManager.getInstance(context).logger.error(TimeUtil.getCurrentTime()+"ServerMessageHandler:exceptionCaught被调用了");

		}else{
			SocketManager.getInstance(context).logger.error("发现了 old tcp channel 断开");
//			EventBus.getDefault().post(NetStatusEvent.NET_RECONNECT);
		}
	}


	@Override
	public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
		super.channelIdle(ctx, e);

		switch (e.getState()) {
			case READER_IDLE:
				SocketManager.getInstance(context).logger.error(TimeUtil.getCurrentTime()+"ServerMessageHandler:读取通道空闲了");
				Channel ch = e.getChannel();
				ch.close();
				SocketManager.getInstance(context).setStatus(SocketStatus.BREAK);
				EventBus.getDefault().post(NetStatusEvent.NET_RECONNECT);
				break;
		}
	}
}
