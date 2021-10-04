package ink.mastermind.WebsockClient.Netty;

import java.util.concurrent.ExecutorService;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import ink.mastermind.WebsockClient.Handler.MessageHandler;
import ink.mastermind.WebsockClient.pojo.DeviceLogin;
import ink.mastermind.WebsockClient.pojo.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.util.CharsetUtil;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
	private WebSocketClientHandshaker handshaker;
	private ChannelPromise handshakeFuture;
	private DeviceLogin deviceLogin;
	private String path;
	private ExecutorService executorService;
	
	public WebSocketClientHandler(DeviceLogin deviceLogin, String path, ExecutorService executorService) {
		super();
		this.deviceLogin = deviceLogin;
		this.path = path;
		this.executorService = executorService;
	}

	public void handlerAdded(ChannelHandlerContext ctx) {
		this.handshakeFuture = ctx.newPromise();
	}

	public WebSocketClientHandshaker getHandshaker() {
		return handshaker;
	}

	public void setHandshaker(WebSocketClientHandshaker handshaker) {
		this.handshaker = handshaker;
	}

	public ChannelPromise getHandshakeFuture() {
		return handshakeFuture;
	}

	public void setHandshakeFuture(ChannelPromise handshakeFuture) {
		this.handshakeFuture = handshakeFuture;
	}

	public ChannelFuture handshakeFuture() {
		return this.handshakeFuture;
	}

	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("channelRead0  " + this.handshaker.isHandshakeComplete());
		final Channel ch = ctx.channel();
		FullHttpResponse response;
		if (!this.handshaker.isHandshakeComplete()) {
			try {
				response = (FullHttpResponse) msg;
				// 握手协议返回，设置结束握手
				this.handshaker.finishHandshake(ch, response);
				// 设置成功
				this.handshakeFuture.setSuccess();
				System.out.println("WebSocket Client connected! response headers[sec-websocket-extensions]:{}"
						+ response.headers());
			} catch (WebSocketHandshakeException var7) {
				FullHttpResponse res = (FullHttpResponse) msg;
				String errorMsg = String.format("WebSocket Client failed to connect,status:%s,reason:%s", res.status(),
						res.content().toString(CharsetUtil.UTF_8));
				this.handshakeFuture.setFailure(new Exception(errorMsg));
			}
		} else if (msg instanceof FullHttpResponse) {
			response = (FullHttpResponse) msg;
			throw new IllegalStateException("Unexpected FullHttpResponse (getStatus=" + response.status() + ", content="
					+ response.content().toString(CharsetUtil.UTF_8) + ')');
		} else {
			WebSocketFrame frame = (WebSocketFrame) msg;
			if (frame instanceof TextWebSocketFrame) {
				TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
				final JSONObject jsonObject = JSON.parseObject(textFrame.text());
				if ("UseTask".equals(jsonObject.getString("type")) 
						&& jsonObject.getString("sender").equals(deviceLogin.getId()) == false) {
					executorService.execute(new Thread(new Runnable() {
						public void run() {
							MessageHandler.input(jsonObject, ch, path, deviceLogin);
						}
					}));
				} else if ("DeviceRegister".equals(jsonObject.getString("type"))) {
					System.out.println(jsonObject.getString("data"));
				}
				System.out.println("TextWebSocketFrame :" + textFrame.text());
			} else if (frame instanceof BinaryWebSocketFrame) {
				BinaryWebSocketFrame binFrame = (BinaryWebSocketFrame) frame;
				System.out.println("BinaryWebSocketFrame");
				MessageHandler.download(frame.content());
			} else if (frame instanceof CloseWebSocketFrame) {
				System.out.println("receive close frame");
				ch.close();
			}
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelActive(ctx);
		System.out.println("channelActive");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelInactive(ctx);
		System.out.println("channelInactive");
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelReadComplete(ctx);
		deviceLogin.register(ctx);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		// TODO Auto-generated method stub
		super.userEventTriggered(ctx, evt);
		System.out.println("userEventTriggered");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
	}
	
}
