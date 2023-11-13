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
package copy;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import udp.communication.Main;

@SuppressWarnings(value = { "all" })
/**
 * @author Shk
 */
public class MsgReceive implements Runnable {
	private int inPort; // 指定该接收端的接收信息的端口号
	private DatagramSocket datagramSocket = null;
//	是否为从机的线程，默认为从机
	private boolean isSlaveThread = true;

	// 构造方法
	public byte sumCheck(byte[] b, int len) {
		int sum = 0;
		for (int i = 0; i < len; i++) {
			sum = sum + b[i];
		}
		if (sum > 0xff) { // 超过了255，使用补码（补码 = 原码取反 + 1）
			sum = ~sum;
			sum = sum + 1;
		}
		return (byte) (sum & 0xff);
	}

	public MsgReceive(int inPort) {
		// 表明这是主机的接收 线程
		if (inPort == configuration.Master_Receive_Port) {
			isSlaveThread = false;
		}
		this.setInPort(inPort);
		try {
			// 1.创建DatagramSocket套接字对象，指定接收数据的接口
			this.setDatagramSocket(new DatagramSocket(inPort));
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			boolean flag = true;
			while (true) {
				// 创建接收数据的容器（字节数组）
				byte[] datas = new byte[1024];
				// 创建DatagramPacket对象，封装容器
				DatagramPacket dp = new DatagramPacket(datas, datas.length);
				// 调用receive方法接收数据报
				datagramSocket.receive(dp);
				// 对接收到的数据进行处理
				String data = new String(datas).trim();
//此用丢包率来假装模拟数据传输是否失误，即假的校验，应考虑错误位数与处理时间（汉明码纠错）的关系，加入一定的延迟
				int rand = (int) (Math.random() * 10000) + 1;
				// 模拟接收方0.02% 的误码率
				if (rand <= 2) {
					System.out.println("经过校验，数据错误，丢弃报文...");
					flag = false;
					continue;
				} else {
					MsgSend.isAllowed = true;
				}
				// 输出数据
				String head = data.substring(0, 8);

				String fromAdress = data.substring(8, 24);
				String toAddress = data.substring(24, 40);
				String length = data.substring(40, 48);
//				TODO 根据length来看，如果大于240，少截取8个,56
//当数据包的长度字段为243时，表示从机收到了主机的轮询命令，且回复了携带真实数据的应答数据包，其中数据包正文字段的第一个字节表示正文长度，第二个字节和第三个字节表示该从机想要进行交互的设备的地址
				String content = "";
				if (data.length() - 8 == 48) {
					// 轮询命令242、请求重传命令244
					content = "";
				} else {
					content = data.substring(48, data.length() - 8);
				}
//				String[] messages = data.split("_");
				// System.out.println(messages);
				System.out
						.println("接收到数据: " + head + "_" + fromAdress + "_" + toAddress + "_" + length + "_" + content);
				System.out.println("正在进行数据校验...");
				System.out.println("head：" + head + "\nfromAddress(十进制):" + Integer.parseInt(fromAdress, 2)
						+ "\ntoAddress(十进制):" + Integer.parseInt(toAddress, 2) + "\nlength(十进制): "
						+ Integer.parseInt(length, 2) + "\ncontent: " + content);
				// 如果信息校验正确，且接收方是从机，表明发送方一定是主机。
				// 从而使得：从机获得发送消息的权限，可以发送信息，发送完毕之后，权限会被撤回
				if (!head.equals(configuration.HEAD_STRING)) {
					System.err.println("头部校验失败，丢弃。");
					// 跳过发送数据，因为校验失败。
					continue;
				}
				if (!Main.checkChecksum(Main.addBinary(data))) {
					System.err.println("校验和校验失败，丢弃。");
					// 跳过发送数据，因为校验失败。
					continue;
				}
				if (getInPort() != Integer.parseInt(toAddress, 2)) {
					System.err.println("非目标接收对象，丢弃。");
					// 跳过发送数据，因为接收对象不是自己
					continue;
				}
				if (isSlaveThread) {
					// 这是从机的接收线程，获得权限
					if (length.equals(Integer.toBinaryString(244))) {
						System.out.println("主机未收到您的响应，导致重发，请尽快重发刚才的应答信息！！！");
					} else if (content.length() == 0) {
						// 242
						System.out.println("来自主机的信息校验正确，轮询报文，您已获得发送权限，请尽快回复应答信息！");
					} else if (length.equals(Integer.toBinaryString(245))) {
						System.out.println("从机收到转发数据，来自主机的信息校验正确，已获得发送权限，请尽快回复应答信息！");
						System.out.println("接收到的转发数据为：" + content.substring(8));
					}
					System.out.print("应答内容: ");
				} else {
					// 表明这是主机的接收线程
					// 根据消息格式，判断是否要进行转发响应从机
//					byte[] b = data.getBytes();
//					if (messages[messages.length - 1].equals(sumCheck(b, b.length) + "") || true) {
//						System.out.println("来自从机的信息校验正确！");
//					} else {
//						System.out.println("来自从机的信息校验错误！");
//						break;
//					}
					System.out.println("来自从机的信息校验正确！");
					// 默认是241
					boolean isNeedForward = false;
// 这里的message字段做好判断！
//					if (messages[3].equals("241")) {
//						isNeedForward = false;
//					}
					if (length.equals(Integer.toBinaryString(243))) {
						isNeedForward = true;
					}
					if (isNeedForward) {
//						int length = messages[4].length()
//						长度 + 目标地址 + 正文

//						String destination = messages[4];
//						System.out.println("收到来自从机的应答信息！请将:'" + messages[4].substring(6) + 
//								"'消息立即进行转发！转发的目标从机端口号是:" + messages[4].substring(1, 6));
						String newToAddress = data.substring(56, 72);
						String newcontent = data.substring(72, data.length() - 8);
						System.out.println("收到来自从机的应答信息！请将:\"" + newcontent + "\" 二进制数据立即进行转发！转发的目标从机端口号是（十进制）:"
								+ Integer.parseInt(newToAddress, 2));
						// 输入校验
						if (!MsgSend.set.contains(Integer.parseInt(newToAddress, 2))) {
							System.err.println("目标从机并未在主机的设备登记表上进行注册，无法转发！");
						}

					} else {
						System.out.println("收到来自从机的应答信息！无需转发，请进行下一次轮询!");
					}
					// 表示主机收到响应消息，然后主机可以继续发送消息，跳出那个三次循环！
					MsgSend.isGetRes = true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (datagramSocket != null) {
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
