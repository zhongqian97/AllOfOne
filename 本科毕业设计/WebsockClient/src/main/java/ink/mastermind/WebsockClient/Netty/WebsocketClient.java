package ink.mastermind.WebsockClient.Netty;

import java.net.URI;
import java.util.concurrent.ExecutorService;

import ink.mastermind.WebsockClient.Handler.MessageHandler;
import ink.mastermind.WebsockClient.pojo.DeviceLogin;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class WebsocketClient {
	private Channel channel = null; 
	private EventLoopGroup group = new NioEventLoopGroup();
	public ChannelFuture start(String url, final DeviceLogin deviceLogin, final String path, final ExecutorService executorService) {
		try {
			Bootstrap boot = new Bootstrap();
			boot.option(ChannelOption.SO_KEEPALIVE, true)
			    .option(ChannelOption.TCP_NODELAY, true)
			    .group(group)
				.handler(new LoggingHandler(LogLevel.INFO))
				.channel(NioSocketChannel.class)
				.handler(new ChannelInitializer<SocketChannel>() {
					protected void initChannel(SocketChannel socketChannel) throws Exception {
						ChannelPipeline p = socketChannel.pipeline();
						p.addLast(new ChannelHandler[] { new HttpClientCodec(),
									new HttpObjectAggregator(64 * 1024 * 1024) });
						//http编码器 + http块处理器
						p.addLast("hookedHandler", new WebSocketClientHandler(deviceLogin, path, executorService));
					}
			});
			URI websocketURI = new URI(url);
			HttpHeaders httpHeaders = new DefaultHttpHeaders();
			
			//阻塞等待并连接服务器
			ChannelFuture future = boot.connect(websocketURI.getHost(), websocketURI.getPort()).sync();
			channel = future.channel();
			
			//配置升级协议
			WebSocketClientHandler handler = (WebSocketClientHandler) channel.pipeline().get("hookedHandler");
			WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(websocketURI,
				WebSocketVersion.V13, (String) null, true, httpHeaders, 64 * 1024 * 1024);
			handler.setHandshaker(handshaker);
			handshaker.handshake(channel);
			
			// 阻塞等待是否握手成功
			handler.handshakeFuture().sync();
			return future;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	
	public Channel getChannel() {
		return channel;
	}

	public void destroy() {
		// TODO Auto-generated method stub
		if (channel != null) {
            channel.close();
        }
        group.shutdownGracefully();
	}
	
//	public static void main(String[] args) throws Exception {
//		String url = "ws://localhost:5433/ws";
//		DeviceLogin deviceLogin = new DeviceLogin("qweqwe", "qweqwe", true);
//		final WebsocketClient endpoint = new WebsocketClient();
//        ChannelFuture future = endpoint.start(url, deviceLogin);
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            @Override
//            public void run() {
//                endpoint.destroy();
//            }
//        });
//        future.channel().closeFuture().syncUninterruptibly();
//	}
}