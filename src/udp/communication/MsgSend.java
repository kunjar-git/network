//封装发送行为信息的类
/**
* UDP发送信息的步骤：
* 1.创建DatagramSocket对象，代表Udp的一个套接字，指定该发送所占用的接口
* 2.创建需要发送的信息，并将其转换成字节数组
* 3.创建DatagramPacket对象，封装字节数组，并指定要发送到的主机+端口
* 4.调用DatagramSocket的send()方法发送数据报包
* 5.关闭资源
*/

package udp.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Scanner;

/**
* @author Shk
*
*/
public class MsgSend implements Runnable {
	private int port; 						// 该发送方所占用的接口
	private int toPort; 					// 发送数据报时接收方用于接收的端口
	private String name; 					// 发送方姓名
	private DatagramSocket datagramSocket; 	// 该端口号为发送方占用的端口号
	private Scanner sc = null;				//控制台输入

	// 构造方法
	public MsgSend(int port, int toPort, String name) {
		this.setPort(port);
		this.setToPort(toPort);
		this.setName(name);
		try {
			this.datagramSocket = new DatagramSocket(this.port);
			sc = new Scanner(System.in);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			while (true) {

				// 从控制台读取信息
				/* String data = sc.next(); */ // 该种方式时发送信息不能有空格，不然接收方会分好几行显示
				String data = sc.nextLine();

				// 拼接上发送方的姓名
				data = this.name + ": " + data;

				// 转换成字节数组
				byte[] datas = data.getBytes();

				// 封装成DatagramPacket数据报包，同时指定接收方地址
				DatagramPacket dp = new DatagramPacket(datas, datas.length);
				dp.setSocketAddress(new InetSocketAddress("localhost", toPort)); // 这里的端口号为接收方接收数据的端口号

				// 调用send方法发送数据报包
				datagramSocket.send(dp);

				// 发送bye时结束对话
				if (data == "bye") {
					break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(sc!=null){
				sc.close();
			}
			if(datagramSocket!=null){
				datagramSocket.close();
			}	
		}
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getToPort() {
		return toPort;
	}

	public void setToPort(int toPort) {
		this.toPort = toPort;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

