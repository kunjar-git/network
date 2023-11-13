//接收信息的类
/*
 * 接收UDP数据报的步骤：
 * 1.创建DatagramSocket对象，指定用于接收数据报的接口
 * 2.定义容器
 * 3.定义DatagramPacket对象，封装容器
 * 4.调用receive方法接收数据报包
 * 5.对接收到的数据报包进行处理
 * 6.关闭资源
 */
package udp.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * @author Shk
 */
public class MsgReceive implements Runnable {
	private int inPort; // 指定该接收端的接收信息的端口号
	private DatagramSocket datagramSocket = null;

	// 构造方法
	public MsgReceive(int inPort) {
		this.setInPort(inPort);
		try {
			// 1.创建DatagramSocket套接字对象，指定接收数据的接口
			this.setDatagramSocket(new DatagramSocket(inPort));
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
				// 创建接收数据的容器（字节数组）
				byte[] datas = new byte[1024];
				// 创建DatagramPacket对象，封装容器
				DatagramPacket dp = new DatagramPacket(datas, datas.length);
				// 调用receive方法接收数据报
				datagramSocket.receive(dp);
				// 对接收到的数据进行处理
				String data = new String(datas).trim();
				// 输出数据
				System.out.println(data);
				// 结束对话的标志：bye
				if (data == "bye")
					break;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(datagramSocket!=null){
				datagramSocket.close();
			}	
		}
	}

	public int getInPort() {
		return inPort;
	}

	public void setInPort(int inPort) {
		this.inPort = inPort;
	}

	public DatagramSocket getDatagramSocket() {
		return datagramSocket;
	}

	public void setDatagramSocket(DatagramSocket datagramSocket) {
		this.datagramSocket = datagramSocket;
	}
}

