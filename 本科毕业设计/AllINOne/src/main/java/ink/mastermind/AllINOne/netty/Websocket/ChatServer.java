package ink.mastermind.AllINOne.netty.Websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.ImmediateEventExecutor;

import java.net.InetSocketAddress;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ink.mastermind.AllINOne.service.ChannelKeeper;

@Service("chatServer")
public class ChatServer {
    //创建 DefaultChannelGroup，其将保存所有已经连接的 WebSocket Channel
	@Autowired
	private ChatServer chatServer;
	@Autowired
	private ChannelInitializer<Channel> chatServerInitializer;
	@Autowired
	private ChannelKeeper channelKeeper;
	
	private int port = 5433;//端口号
    private final EventLoopGroup group = new NioEventLoopGroup();
    private Channel channel;

    public ChannelFuture start(InetSocketAddress address) {
        //引导服务器
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(group)
   	 			.handler(new LoggingHandler(LogLevel.INFO))
        		.channel(NioServerSocketChannel.class)
        		.childHandler(createInitializer());
        ChannelFuture future = bootstrap.bind(address);
        future.syncUninterruptibly();
        //等待此将来直到完成，如果此将来失败，则将失败的原因重新抛出。
        channel = future.channel();
        return future;
    }

    //创建 ChatServerInitializer
    protected ChannelInitializer<Channel> createInitializer() {
        return chatServerInitializer;
    }

    //处理服务器关闭，并释放所有的资源
    public void destroy() {
        if (channel != null) {
            channel.close();
        }
        channelKeeper.close();
        group.shutdownGracefully();
    }

	public ChatServer() {
		super();
		// TODO Auto-generated constructor stub
	}

	
//	
//	public static void main(int port) throws Exception {
//        ChannelFuture future = chatServer.start(new InetSocketAddress(port));
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            @Override
//            public void run() {
//            	chatServer.destroy();
//            }
//        });
//        future.channel().closeFuture().syncUninterruptibly();
//    }
    
}
