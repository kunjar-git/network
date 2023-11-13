//slave1
package udp.communication;

import java.io.IOException;

/**
* @author Shk
*
*/
public class slave1 {

	public static void main(String[] args) throws IOException {
		System.out.println("-----我是Slave1------");
		
		//创建接收信息的对象(接收信息端口20666)
		new Thread(new MsgReceive(20666)).start();
		
		//创建发送信息的对象（发送信息端口20888，要发送到的端口10666）
		new Thread(new	MsgSend(20888, 10666, "Slave1")).start();
	}
}
