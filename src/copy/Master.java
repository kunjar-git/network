//Master
package copy;

import java.io.IOException;

/**
 * @author Shk
 */

public class Master {

	public static void main(String[] args) throws IOException {
		System.out.println("---------------This is Master---------------");

		// 创建接收信息的对象
		new Thread(new MsgReceive(configuration.Master_Receive_Port)).start();

		// 创建发送信息的对象
		new Thread(new MsgSend(configuration.Master_Send_Port, 30002, "Master")).start();
	}
}
