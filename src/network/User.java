package network;
import java.io.IOException;
import java.net.Socket;
import java.io.*;

public class User {

	public void Server() {
		
		//首先需要建立一个客户端（客户机）,同样需要捕获异常
		try {
		//连接到服务器
		Socket client = new Socket("127.0.0.1",8547);
		System.out.println("客户端连入服务器");
		
		//创建输入流
		InputStream read = (InputStream)client.getInputStream();
		BufferedReader tr = new BufferedReader(new InputStreamReader(read));
		
		//创建输出流
		OutputStream out = client.getOutputStream();
		PrintWriter th = new PrintWriter(out);
			
		//让客户机输出一段字符
		String wd = "申请进入服务器";
		
		//输出这段话
		th.write(wd);
		th.flush();

        //关闭输出，不再输出更多数据，如果不关闭，就会出现服务器无法判断客户机何时会结束发送消息，从而无法进行回应。就和一个人和你打电话的时候一直在说话，你无法在这段时间如回复他。
		client.shutdownOutput();
		
		//创建一个字符串用来储存服务器发送来的消息
		String str ;
		
		//当服务器有消息发送过来才执行输出
		while(!((str = tr.readLine()) == null)) {
			System.out.println("服务器发送了消息："+str);
		}
		
		//以上的输入输出I/O流开启后都需要我们执行关闭代码
		out.close();
		th.close();
		read.close();
		tr.close();
		
		//最后关闭网络连接，就和打完电话之后要挂断电话一样
		client.close();
		
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public static void main(String[] args) {
		User God=new User();
		God.Server();
    }
	
}
