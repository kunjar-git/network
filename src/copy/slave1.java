//slave1
package copy;

import java.io.IOException;

/**
 * @author Shk
 *
 */
//slave2端口，和待转发的信息0111010100110010_11111111
// 011101010011001011111111
@SuppressWarnings(value = { "all" })
public class slave1 {

	public static void main(String[] args) throws IOException {
		System.out.println("---------------This is Slave1----------------");

		// 创建接收信息的对象(接收信息端口30001)
		new Thread(new MsgReceive(configuration.Slave1_Reveive_Port)).start();

		// 创建发送信息的对象（发送信息端口20001，要发送到的端口10888）
		new Thread(new MsgSend(configuration.Slave1_Send_Port, configuration.Master_Receive_Port, "Slave1")).start();
//		System.out.println("Master: 01010101_10666_30001_242__00010101\r\n" + 
//				"来自主机的信息校验正确，您已获得发送权限，请尽快回复应答信息！\r\n" + 
//				"应答内容: 01010101_10666_30001_244__00010101\r\n" + 
//				"主机未收到您的响应，请尽快重发刚才的应答信息！！！\r\n" + 
//				"应答内容: 01010101_10666_30001_244__00010101\r\n" + 
//				"主机未收到您的响应，请尽快重发刚才的应答信息！！！\r\n" + 
//				"应答内容: 01010101_10666_30001_244__00010101\r\n" + 
//				"主机未收到您的响应，请尽快重发刚才的应答信息！！！\r\n" + 
//				"应答内容: ");
	}
}
