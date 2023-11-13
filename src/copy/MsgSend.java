//封装发送行为信息的类
/**
* UDP发送信息的步骤：
* 1.创建DatagramSocket对象，代表Udp的一个套接字，指定该发送所占用的接口
* 2.创建需要发送的信息，并将其转换成字节数组
* 3.创建DatagramPacket对象，封装字节数组，并指定要发送到的主机+端口
* 4.调用DatagramSocket的send()方法发送数据报包
* 5.关闭资源
*/

package copy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import udp.communication.Main;

@SuppressWarnings(value = { "all" })
/**
 * @author Shk
 *
 */
public class MsgSend implements Runnable {

	private int port; // 该发送方所占用的接口
	private int toPort; // 发送数据报时接收方用于接收的端口
	private String name; // 发送方姓名
	private DatagramSocket datagramSocket; // 该端口号为发送方占用的端口号
	private Scanner sc = null; // 控制台输入
	private boolean isSlaveThread = false;
	public static boolean isAllowed = true; // 是否允许发送信息
	public static boolean isGetRes = false; // 是否获得响应，用来标明是否需要重传的
	public static HashSet<Integer> set = new HashSet<>(); // 所有在线的从设备地址

	private byte sumCheck(byte[] b, int len) {
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

	public static String generateHammingCheckCode(byte[] data, int n) {
		int count = 0;
		for (int i = 0; i < n; i++) {
			if (data[i] == (data[i] ^ (1 << (n - 1)))) {
				count++;
			}
		}
		return count + "";
	}

	public static int calculateHammingCodeLength(int n) {
		int length = 1;
		while (n / (1 << length) > 0) {
			length++;
		}
		return length;
	}

	// 构造方法
	public MsgSend(int port, int toPort, String name) {
		// 表明这个线程是由slave创建的，因为slave只能给主机发送应答信息
		if (toPort == configuration.Master_Receive_Port) {
			this.isSlaveThread = true;
			// 从线程不能主动发送消息
			MsgSend.isAllowed = false;
		}
		this.setPort(port);
		this.setToPort(toPort);
		this.setName(name);
		set.add(configuration.Slave1_Reveive_Port);
		set.add(configuration.Slave2_Reveive_Port);
		try {
			this.datagramSocket = new DatagramSocket(this.port);
			sc = new Scanner(System.in);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void publicSend() {
		for (int toPort : set) {
			String data = "01010101_10666_30001_245_111111111_00100101";
			// 转换成字节数组
			byte[] datas = data.getBytes();

			// 封装成DatagramPacket数据报包，同时指定接收方地址
			DatagramPacket dp = new DatagramPacket(datas, datas.length);
			dp.setSocketAddress(new InetSocketAddress("localhost", toPort));
		}

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			while (true) {
//				从机发送应答信息
				if (isSlaveThread) {

//						格式暂时还没输入
					if (!isAllowed) {
						System.err.println("当前未获得主机允许，从机不能主动发送消息！");
					}
					if (isAllowed) {
						System.out.println("请输入要响应的正文: ");
					}
					// 从控制台读取信息
					/* String data = sc.next(); */ // 该种方式时发送信息不能有空格，不然接收方会分好几行显示
					String content = sc.nextLine();
					if (!isAllowed) {
						System.err.println("从机不可主动发送信息！");
						continue;
					}
					// 8字节对齐，从而进行校验和计算。注：只有非汉明时应用。
					content = generate8str(content);
					// 报文头固定, 1字节
					String head = configuration.HEAD_STRING;
					// 发送地址
//					String fromAddress = configuration.Master_Send_Port + "";
					// 发送地址，主机固定，2字节
					String fromAddress = generateIIbyte(Integer.toBinaryString(this.port));
					// 接收地址,需要对齐，2字节。
					String toAddress = configuration.Master_Receive_Port_STR;
//					String checksum = "10101011";
					// 报文头
//					String head = "01010101";
					// 发送地址
//					String fromAddress = this.port + "";
					// 接收地址
//					String toAddress = toPort + "";
					// 长度
					String length = "";
					// 当数据包的长度字段为241Bytes时，表示从设备收到了总线控制器的命令，且回复了不带真实数据的应答信号；
					if (content == null || content.length() == 0) {
						// 不交互，length字段的“241”表示特殊值
						length = generateIbyte(Integer.toBinaryString(241));
					} else {
						// 243 交互，有内容,正文的第一个字节表示正文的实际长度
						// 设备地址请输入2进制的，长度为16的
						// 第二个字节和第三个字节表示从机Slave1想要进行交互的设备的地址
						// 正文的第一个字节表示正文长度，第二个字节表示想要交互的设备的地址。
						length = generateIbyte(Integer.toBinaryString(243));
//						TODO 还用做吗？怎么切割出正文呢？
						content = (generateIbyte(Integer.toBinaryString(content.length() - 16))) + content;
					}
//					String data = head + "_" + fromAddress + "_" + toAddress + "_" + length + "_" + content + "_";
					String nocheckString = head + fromAddress + toAddress + length + content;
					// 校验和
					String checkSum = Main.addBinary(nocheckString);
					// 全部数据
					String data = nocheckString + checkSum;

					// 拼接上发送方的姓名
//					data = this.name + ": " + data;
					// 转换成字节数组

					byte[] datas = data.getBytes();

					// 封装成DatagramPacket数据报包，同时指定接收方地址
					DatagramPacket dp = new DatagramPacket(datas, datas.length);
					dp.setSocketAddress(new InetSocketAddress("localhost", toPort)); // 这里的端口号为接收方接收数据的端口号
					System.out.println("从机发送数据：" + head + "_" + fromAddress + "_" + toAddress + "_" + length + "_"
							+ content + "_" + checkSum);
					System.out.println("head: " + head + "\nfromAddress(十进制): " + this.port + "\ntoAddress(十进制): "
							+ configuration.Master_Receive_Port + "\nlength(十进制): " + Integer.parseInt(length, 2)
							+ "\ncontent: " + content + "\ncheckSum: " + checkSum);
					// 调用send方法发送数据报包
					int rand = (int) (Math.random() * 10000) + 1;
					// 模拟0.02% 的丢包率
					if (rand <= 2) {
						// 如果命中了丢包
						System.out.println("从机不发数据。直接重新监听");
						MsgSend.isAllowed = false;
						continue;
					}
					datagramSocket.send(dp);
					// 根据数据包长度，考虑一定比率延迟，模拟真实电力线传输场景
					System.out.println("从机应答完毕，被主机撤回权限...");
					// 既然能走到这里，说明isAllowed 是true·
					// 发送信息完毕，撤回权限
					MsgSend.isAllowed = false;
					// 发送bye时结束对话
					if (data == "bye") {
						break;
					}
				} else {
					// 主机发送信息 的 逻辑
//					轮询信息 / 转发的请求消息
//				现在主动发送轮询，实际可以自动轮询？
					while (true) {
						System.out.println("----------------------");
						System.out.print("是否有需要登记的从设备（Y/N）: ");
						if (sc.nextLine().toLowerCase().equals("y")) {
							System.out.print("请输入登记的设备的地址: ");
							set.add(Integer.parseInt(sc.nextLine()));
						}
						System.out.print("当前的从设备登记表内容如下: ");
						System.out.println(set);
						System.out.print("请输入轮询或转发的目标从机的端口号: ");
						toPort = sc.nextInt();
						this.setToPort(toPort);
						// 从控制台读取信息
						/* String data = sc.next(); */ // 该种方式时发送信息不能有空格，不然接收方会分好几行显示
						sc.nextLine();
						System.out.print("请输入要发送的正文(二进制): ");
						String content = sc.nextLine();
						// 8字节对齐，从而进行校验和计算。注：只有非汉明时应用。
						content = generate8str(content);
						// 报文头固定, 1字节
						String head = configuration.HEAD_STRING;
						// 发送地址
//						String fromAddress = configuration.Master_Send_Port + "";
						// 发送地址，主机固定，2字节
						String fromAddress = configuration.Master_Send_Port_STR;
						// 接收地址,需要对齐，2字节。
						String toAddress = generateIIbyte(Integer.toBinaryString(toPort));

						// 长度 1 字节 需对齐
						String length = "";
						// 当数据包的长度字段为242Bytes时，代表的是总线控制器的轮询命令，并不携带真实数据；
						if (content == null || content.length() == 0) {
//							length = "242";
							length = generateIbyte(Integer.toBinaryString(242));
						} else {
							// 代替从机的转发
							length = generateIbyte(Integer.toBinaryString(245));
							content = generateIbyte(Integer.toBinaryString(content.length())) + content;
						}
						// 数据包正文字段的第一个字节表示正文长度
						String nocheckString = head + fromAddress + toAddress + length + content;
						// 校验和
//						String checkSum = "00010101";
						String checkSum = Main.addBinary(nocheckString);
//						String data = head + "_" + fromAddress + "_" + toAddress + "_" + length + "_" + content + "_"
//								+ checkSum;
						String data = nocheckString + checkSum;
						// 拼接上发送方的姓名
//						data = this.name + ": " + data;
//						data = "01010101_10666_20001_245_111111111_00100101";
						// 转换成字节数组
						byte[] datas = data.getBytes();
						// 封装成DatagramPacket数据报包，同时指定接收方地址
						DatagramPacket dp = new DatagramPacket(datas, datas.length);
						// 广播发送
						for (int port : set) {
							if (port != toPort) {
								dp = new DatagramPacket(datas, datas.length);
								dp.setSocketAddress(new InetSocketAddress("localhost", port)); // 这里的端口号为接收方接收数据的端口号
								datagramSocket.send(dp);
							}
						}
						dp.setSocketAddress(new InetSocketAddress("localhost", toPort)); // 这里的端口号为接收方接收数据的端口号
						System.out.println("主机发送的数据：" + head + "_" + fromAddress + "_" + toAddress + "_" + length + "_"
								+ content + "_" + checkSum);
						System.out.println("head：" + head + "\nfromAddress(十进制):" + configuration.Master_Send_Port
								+ "\ntoAddress(十进制):" + toPort + "\nlength(十进制): " + Integer.parseInt(length, 2)
								+ "\ncontent: " + content + "\ncheckSum: " + checkSum);
						// 调用send方法发送数据报包
						datagramSocket.send(dp);
//						// 令从机获得发送权限！
//						isAllowed = true;
						// 根据数据包长度，考虑一定比率延迟，模拟真实电力线传输场景
						System.out.println("轮询/转发 发送完毕，等待响应中......");
						try {
							// 接收方发送应答消息也需要时间，这里考虑一个固定的等待时间
							// 休眠 2.573 秒 等待isGetRes的标志位置为真
							Thread.sleep(configuration.T_for_REALITY);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// 从机响应超时，主机重发报文,重置count
						configuration.count = 1;
						// 改变报文类型为244: 主机轮询从机X，并请求其重发上次的消息！
						if (!isGetRes) {
//							data = this.name + ": " + head + "_" + fromAddress + "_" + toAddress + "_" + "244" + "_"
//									+ content + "_" + checkSum;
							nocheckString = head + fromAddress + toAddress + generateIbyte(Integer.toBinaryString(244))
									+ content;
							checkSum = Main.addBinary(nocheckString);
//							String data = head + "_" + fromAddress + "_" + toAddress + "_" + length + "_" + content + "_"
//									+ checkSum;
							System.out.println("主机重发：" + head + "_" + fromAddress + "_" + toAddress + "_" + length + "_"
									+ content + "_" + checkSum);
							data = nocheckString + checkSum;
							// 转换成字节数组
							datas = data.getBytes();
							// 封装成DatagramPacket数据报包，同时指定接收方地址
							dp = new DatagramPacket(datas, datas.length);
							// 广播发送
							for (int port : set) {
								if (port != toPort) {
									dp = new DatagramPacket(datas, datas.length);
									dp.setSocketAddress(new InetSocketAddress("localhost", port)); // 这里的端口号为接收方接收数据的端口号
									datagramSocket.send(dp);
								}
							}
							dp.setSocketAddress(new InetSocketAddress("localhost", toPort)); // 这里的端口号为接收方接收数据的端口号
						}
						while (!isGetRes && configuration.count <= 3) {
							try {
								// 发送方的误码率模拟
								int rand = (int) (Math.random() * 10000) + 1;
								// 模拟0.02% 的丢包率
								if (rand <= 2) {
									Thread.sleep(configuration.T_for_REALITY);
									System.out.println("经过校验，数据错误，接收方丢弃报文...");
									continue;
//									System.out.println("经过校验，数据错误，正在利用汉明码技术回...");
								}
								datagramSocket.send(dp);
								System.err.println("未收到请求，进行第" + configuration.count + "次重发中...");
								// 接收方发送应答消息也需要时间，这里考虑一个固定的等待时间，不一定是5s 2.573s
								Thread.sleep(configuration.T_for_REALITY);
								if (isGetRes) {
									break;
								}
								configuration.count++;
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							// 最多重复上述请求3次！
						}
						// 无论是否收到消息，这时都要撤回权限了。
						isAllowed = false;
						if (configuration.count > 3) {
							isAllowed = false;
							// 收回超时未响应从机的权限
							System.out.println(toPort + "端口的从机 已经超过 3 次 未响应，收回权限，已从设备登记表中删除设备！");
							set.remove(toPort);
						}
						// 重置它
						isGetRes = false;
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (sc != null) {
				sc.close();
			}
			if (datagramSocket != null) {
				datagramSocket.close();
			}
		}
	}

	// 8位正文对齐，汉明码不用，校验和用
	public static String generate8str(String str) {
		int n = str.length();
		if (n % 8 == 0) {
			return str;
		}
		for (int i = 0; i < 8 - n % 8; i++) {
			str += "0";
		}
		return str;
	}

	public String generateIbyte(String str) {
		if (str.length() == 8) {
			return str;
		}
		int length = str.length();
		for (int i = 0; i < 8 - length; i++) {
			str = "0" + str;
		}
		return str;
	}

	public String generateIIbyte(String str) {
		if (str.length() == 16) {
			return str;
		}
		int length = str.length();
		for (int i = 0; i < 16 - length; i++) {
			str = "0" + str;
		}
		return str;
	}

	public boolean isAllowed() {
		return isAllowed;
	}

	public void setAllowed(boolean isAllowed) {
		MsgSend.isAllowed = isAllowed;
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
