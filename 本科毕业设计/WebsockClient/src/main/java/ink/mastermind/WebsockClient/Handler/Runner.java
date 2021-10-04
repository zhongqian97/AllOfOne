package ink.mastermind.WebsockClient.Handler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ink.mastermind.WebsockClient.pojo.Message;

// 处理外部程序的错误输出流
class ErrorIOHandler implements Runnable {

	private InputStream error;
	private OutputStream out = null;
	private Message message;

	public ErrorIOHandler(InputStream error, OutputStream out, Message message) {
		this.error = error;
		this.out = out;
		this.message =message;
	}
	
	public ErrorIOHandler(InputStream error, Message message) {
		this.error = error;
		this.message = message;
	}

	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(error));
			String line = "";
			while ((line = br.readLine()) != null) {
				if (out == null) {
					message.setData(line);
					MessageHandler.output(message);
				} else {
					out.write((line + "\n").getBytes());
					out.flush();
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

// 处理外部程序的正常输出流
class NormalIOHandler implements Runnable {

	private InputStream normal;
	private OutputStream out = null;
	private Message message;

	public NormalIOHandler(InputStream normal, OutputStream out, Message message) {
		this.normal = normal;
		this.out = out;
		this.message = message;
	}
	
	public NormalIOHandler(InputStream normal, Message message) {
		this.normal = normal;
		this.message = message;
	}


	public void run() {
		BufferedReader br = new BufferedReader(new InputStreamReader(normal));
		String line = "";
		try {
			while ((line = br.readLine()) != null) {
				if (out == null) {
					message.setData(line);
					MessageHandler.output(message);
				} else {
					out.write((line + "\n").getBytes());
					out.flush();
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

// 给外部程序发送命令
class CmdIOHandler implements Runnable {

	private OutputStream out;
	private InputStream inputStream;
	private Message message;

	public CmdIOHandler(OutputStream out, InputStream inputStream, Message message) {
		this.out = out;
		this.inputStream = inputStream;
		this.message = message;
	}

	public void run() {
		BufferedReader br = null;
		try {
			String line = "";
			br = new BufferedReader(new InputStreamReader(inputStream));// 控制台读取流
			while (true) {
				try {
					line = br.readLine();// 从控制台读取一行
					System.out.println();
					System.out.println(line);
					if (!line.equals("")) {
						out.write((line + "\n").getBytes());// 发送命令给外部程序
					}
					out.flush();
				} catch (Exception e) {
					break;
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

public class Runner implements Runnable {
	private InputStream inputStream;
	private OutputStream outputStream;
	private Message message;
	private String exePath;
	
	public void run() {
		try {
			//线程池
			ExecutorService ex = Executors.newCachedThreadPool();
			Process process = Runtime.getRuntime().exec("bash " + exePath);
			if (outputStream == null) {
				ex.execute(new ErrorIOHandler(process.getErrorStream(), new Message(message)));
				ex.execute(new NormalIOHandler(process.getInputStream(), new Message(message)));
			} else {
				ex.execute(new ErrorIOHandler(process.getErrorStream(), outputStream, new Message(message)));
				ex.execute(new NormalIOHandler(process.getInputStream(), outputStream, new Message(message)));
			}
			ex.execute(new CmdIOHandler(process.getOutputStream(), inputStream, new Message(message)));
			process.waitFor(); // 等待process进程的输出流都被处理
			if (process.exitValue() == 0) {
				System.out.println("进程正常退出");
			} else {
				System.out.println("进程异常退出");
			}
			ex.shutdownNow(); //关闭其他三个线程
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Runner(String exePath, InputStream inputStream, OutputStream outputStream, Message message) {
		super();
		this.exePath = exePath;
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.message = message;
	}

	public Runner(String exePath, Message message) {
		super();
		this.exePath = exePath;
		this.outputStream = null;
		this.inputStream = null;
		this.message = message;
	}
	
	public Runner(String exePath, InputStream inputStream, Message message) {
		super();
		this.exePath = exePath;
		this.outputStream = null;
		this.inputStream = inputStream;
		this.message = message;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public static void main(String[] args) {
		InputStream inputStream = new ByteArrayInputStream("ls -l \n exit \n".getBytes());
		OutputStream outputStream = System.out;
		Message message = new Message();
		Runner runner = new Runner("zsh", inputStream, outputStream, message);
		Thread thread = new Thread(runner);
		thread.start();
		while(true) {
			System.out.println(thread.isAlive());
			if (!thread.isAlive()) break;
		}
	}
}