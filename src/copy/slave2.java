//slave1
package copy;

import java.io.IOException;

import copy.configuration;

/**
 * @author Shk
 *
 */
@SuppressWarnings(value = { "all" })
public class slave2 {

	public static void main(String[] args) throws IOException {
		System.out.println("---------------This is Slave2---------------");
//		System.err.println("当前未获得主机允许，从机不能主动发送消息！");
//		System.out.println( "Master:01010101_10666_30002_245_111111111_00100101\r\n" + 
//				"来自主机的信息校验正确，您已获得发送权限，请尽快回复应答信息！\r\n" + 
//				"应答内容: \r\n" + 
//				"从机应答完毕，被主机撤回权限...");
		// 创建接收信息的对象(接收信息端口30002)
		new Thread(new MsgReceive(configuration.Slave2_Reveive_Port)).start();

		// 创建发送信息的对象（发送信息端口20002，要发送到的端口10888）
		new Thread(new MsgSend(configuration.Slave2_Send_Port, configuration.Master_Receive_Port, "Slave2")).start();
	}
}
