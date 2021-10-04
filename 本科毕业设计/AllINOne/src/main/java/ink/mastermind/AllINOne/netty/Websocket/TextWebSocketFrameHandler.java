package ink.mastermind.AllINOne.netty.Websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import ink.mastermind.AllINOne.pojo.Message;
import ink.mastermind.AllINOne.service.ChannelKeeper;
import ink.mastermind.AllINOne.service.UserGroupService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

//扩展 SimpleChannelInboundHandler，并处理 TextWebSocketFrame 消息
/**
 * @author joshua
 *
 */
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
	
	private ChatServerInitializer chatServerInitializer;
    /**
	 * @param chatServerInitializer
	 */
	public TextWebSocketFrameHandler(ChatServerInitializer chatServerInitializer) {
		// TODO Auto-generated constructor stub
		this.chatServerInitializer = chatServerInitializer; 
	}

	/**
     * 添加到用户在线池中，并返回频道ID供注册使用。
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            chatServerInitializer.getChannelKeeper().firstChannel(ctx, evt);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 收到信息后干嘛
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        //(3) 增加消息的引用计数，并将它写到 ChannelGroup 中所有已经连接的客户端
    	chatServerInitializer.getChannelKeeper().handleInformation(ctx, msg);
    }

	/**
	 * 创建连接，然而还没注册，没啥用的方法
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelActive(ctx);
	}

	/**
	 * 断开连接
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelInactive(ctx);
		chatServerInitializer.getChannelKeeper().removeChannel(ctx.channel());
	}

	/**
	 * 冲刷信息
	 */
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelReadComplete(ctx);
		ctx.flush();
	}

	/**
	 * 信息报错，反馈给管理员日志去
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
		chatServerInitializer.getChannelKeeper().channelLog(ctx, cause);
	}
    
}
