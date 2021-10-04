package ink.mastermind.WebsockClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ink.mastermind.WebsockClient.Handler.MessageHandler;
import ink.mastermind.WebsockClient.Netty.WebsocketClient;
import ink.mastermind.WebsockClient.pojo.DeviceLogin;
import ink.mastermind.WebsockClient.pojo.Message;
import io.netty.channel.ChannelFuture;

/**
 * Hello world!
 * 这个是客户端的，也叫设备端
 */
public class App {
	
	public static void main(String[] args) {
		System.out.println("Hello World!");
		String url = "ws://localhost:5433/ws";
		String path = "/Users/joshua/";
		DeviceLogin deviceLogin = new DeviceLogin("5ec8f8c329c76a4bfc09c0fb", "qweqwe", true);
		if (args != null && args.length == 4) {
			url = args[0];
			path = args[1];
			deviceLogin = new DeviceLogin(args[2], args[3], true);
		} 
		//else return;
		try {
			//设备线程池
			ExecutorService executorService = Executors.newCachedThreadPool();
			final WebsocketClient endpoint = new WebsocketClient();
	        ChannelFuture future = endpoint.start(url, deviceLogin, path, executorService);
	        while (future == null) {
	        	System.out.println("无法连接到服务器！等待1分钟后重新连接");
	        	Thread.currentThread().sleep(1000 * 60);
	        	future = endpoint.start(url, deviceLogin, path, executorService);
	        }
	        Runtime.getRuntime().addShutdownHook(new Thread() {
	            @Override
	            public void run() {
	                endpoint.destroy();
	            }
	        });
	        future.channel().closeFuture().syncUninterruptibly();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
