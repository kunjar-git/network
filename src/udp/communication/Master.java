//Master
package udp.communication;

import java.io.IOException;
/**
 * @author Shk
 */
public class Master {

	public static void main(String[] args) throws IOException {
		System.out.println("----我是Master----");

		// 创建接收信息的对象
		new Thread(new MsgReceive(10666)).start();

		// 创建发送信息的对象
		new Thread(new MsgSend(10888, 20666, "Master")).start();
	}
}