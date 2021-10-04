/**
 * 
 */
package ink.mastermind.AllINOne.service;

import java.net.InetSocketAddress;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ink.mastermind.AllINOne.netty.Websocket.ChatServer;
import io.netty.channel.ChannelFuture;

/**
 * @author joshua
 *
 */
@Service("ChatServerService")
public class ChatServerService {
	
	@Autowired
	private ChatServer chatServer;
	private int port = 5433;//端口号
	private volatile boolean isStart = false;
	
	@PostConstruct
	public void ChatServer() {
		// TODO Auto-generated constructor stub
      synchronized (chatServer) {
    	  if (isStart) return;
    	  isStart = true;
    	  ChannelFuture future = chatServer.start(new InetSocketAddress(port));
          Runtime.getRuntime().addShutdownHook(new Thread() {
              @Override
              public void run() {
            	  chatServer.destroy();
              }
          });
      }
	}
	
}
