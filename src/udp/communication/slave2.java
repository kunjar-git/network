//slave1
package udp.communication;

import java.io.IOException;
/**
* @author Shk
*
*/
public class slave2 {

	public static void main(String[] args) throws IOException {
		System.out.println("-----我是Slave2------");
		
		//创建接收信息的对象(接收信息端口20666)
		new Thread(new MsgReceive(20667)).start();
		
		//创建发送信息的对象（发送信息端口20888，要发送到的端口10666）
		new Thread(new	MsgSend(20889, 10666, "Slave2")).start();
	}
}