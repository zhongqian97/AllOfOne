package ink.mastermind.AllINOne.netty.Websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ink.mastermind.AllINOne.service.ChannelKeeper;
import ink.mastermind.AllINOne.service.UserGroupService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;


@Service("chatServerInitializer")
public class ChatServerInitializer extends ChannelInitializer<Channel> {

    @Autowired
    private ChannelKeeper channelKeeper;
    @Autowired
    private ChatServerInitializer chatServerInitializer;
    
    @Override
    //将所有需要的 ChannelHandler 添加到 ChannelPipeline 中
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec());
        //类HttpServerCodec是HttpRequestDecoder和HttpResponseEncoder的组合，可以简化HTTP的服务器端实现。
        pipeline.addLast(new ChunkedWriteHandler());
        //异步写大型数据流，而又不会导致大量的内存消耗。
        pipeline.addLast(new HttpObjectAggregator(64 * 1024));
        //处理传输编码为“块状”的HTTP消息
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        //该 WebSocketServerProtocolHandler 处理所有规定的 WebSocket 帧类型和升级握手本身。
        //如果握手成功所需的 ChannelHandler 被添加到管道，而那些不再需要的则被去除。
        pipeline.addLast(new TextWebSocketFrameHandler(chatServerInitializer));
        //文本桢处理类
    }

	public ChannelKeeper getChannelKeeper() {
		return channelKeeper;
	}

	public void setChannelKeeper(ChannelKeeper channelKeeper) {
		this.channelKeeper = channelKeeper;
	}

	public ChatServerInitializer getChatServerInitializer() {
		return chatServerInitializer;
	}

	public void setChatServerInitializer(ChatServerInitializer chatServerInitializer) {
		this.chatServerInitializer = chatServerInitializer;
	}
    
    
}
