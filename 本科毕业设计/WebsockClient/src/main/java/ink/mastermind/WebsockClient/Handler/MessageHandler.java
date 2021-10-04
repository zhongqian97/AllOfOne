/**
 * 
 */
package ink.mastermind.WebsockClient.Handler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Time;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.alibaba.fastjson.JSONObject;

import ink.mastermind.WebsockClient.pojo.DeviceLogin;
import ink.mastermind.WebsockClient.pojo.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @author joshua
 * 任务处理
 */

public class MessageHandler {
	//频道进程管理池
	private static final ConcurrentMap<String, Thread> channelThreadMap = new ConcurrentHashMap<String, Thread>();
	private static final ConcurrentMap<String, Runner> channelRunnerMap = new ConcurrentHashMap<String, Runner>();
	//频道池
	private static final ConcurrentMap<String, Channel> channelMap = new ConcurrentHashMap<String, Channel>();
	
	private static String path;
	private static String fileName = null;
	private static volatile boolean fileFlag = false;
	private static DeviceLogin deviceLogin;

	/**
	 * @param jsonObject
	 * @param channel
	 * @param paths
	 * @param login
	 * 任务输入到任务进程中
	 */
	public static void input(JSONObject jsonObject, Channel channel, String paths, DeviceLogin login) {
		Message message = new Message(
				jsonObject.getString("id"), 
				jsonObject.getString("type"),
				jsonObject.getString("task"),
				jsonObject.getString("data"),
				jsonObject.getString("time"),
				jsonObject.getString("sender"),
				jsonObject.getString("addressee"));
		File f = new File(path + message.getTask());
		if (!f.exists()) { //文件不存在
			synchronized (f) {
				path = paths;
				fileFlag = true;
				fileName = message.getTask();
				Message msg = new Message("DownloadTask", message.getTask(), "", "", "");
				channel.writeAndFlush(new TextWebSocketFrame(msg.toString()));
				int flag = 0;
				while(fileFlag) {
					try {
						Thread.currentThread().sleep(2000);
						flag ++;
						if (flag > 30) {
							System.out.println("超时啊！");
							return;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			f = new File(path + message.getTask());
		}
		if (message.getAddressee() != null 
				&& message.getAddressee().equals("") == false) {
			channelMap.put(message.getAddressee(), channel);
			Runner runner = null;
			if (!channelRunnerMap.containsKey(message.getAddressee()) 
					|| !channelThreadMap.get(message.getAddressee()).isAlive()) {
				//创建一个Runner，并添加到channelMap
				InputStream inputStream = new ByteArrayInputStream(message.getData().getBytes());
				deviceLogin = login;
				runner = new Runner(path + message.getTask(), inputStream, null, new Message(message));
				Thread thread = new Thread(runner);
				thread.start();
				channelRunnerMap.put(message.getAddressee(), runner);
				channelThreadMap.put(message.getAddressee(), thread);
				return;
			} else {
				try {
					channelRunnerMap
						.get(message.getAddressee())
						.getInputStream()
						.read(message.toString().getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @param message
	 * 将任务进程中返回信息传回频道中
	 */
	public static void output(Message message) {
		if (message.getAddressee() == null 
				|| message.getAddressee().equals(""))
			return;
		message.setSender(deviceLogin.getId());
		message.setType("text");
		channelMap.get(message.getAddressee())
			.writeAndFlush(new TextWebSocketFrame(message.toString()));
	}

	/**
	 * @param byteBuf
	 * 单线程下载任务，下载完解开可重入锁中
	 */
	public synchronized static void download(ByteBuf byteBuf) {
		try {
			if (byteBuf.readableBytes() <= 0 || fileName.equals(""))
				return;
			
			byte[] bytes = new byte[byteBuf.readableBytes()];
			byteBuf.readBytes(bytes);
			OutputStream outputStream = new FileOutputStream(
					new File(path + fileName));
			outputStream.write(bytes);
			fileFlag = false;
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
