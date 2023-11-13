package network;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Admin {

	private ServerSocket my;

	public void Begin(int port) {
		
		//首先需要创建一个服务器
		try {
			my = new ServerSocket(port);
			
			//测试语句
			System.out.println("本地服务器已建立");
			
			//等待与客户机连接
			Socket admin = my.accept();
			System.out.println("已收到客户机请求");
			
			//输入流，读取数据用的
			java.io.InputStream read = admin.getInputStream();
			BufferedReader tr = new BufferedReader(new InputStreamReader(read));
			
			//输出流，发送数据用的
			OutputStream out = admin.getOutputStream();
			PrintWriter th = new PrintWriter(out);
			
			//读取客户机发送来的消息，设置判断，只有客户机有发送消息才做输出语句
			String adm;
			while(!((adm = tr.readLine()) == null)) {
				System.out.println("收到客户机消息："+adm);
			}
			
			//给客户机发送一段消息
			String word = "Welcome to my world!";
			
			//输出这段消息
			th.write(word);
			th.flush();
			
			//关闭输入输出流
			read.close();
			tr.close();
			out.close();
			th.close();
			
			//结束连接
			admin.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Admin ds = new Admin();
		ds.Begin(8547);
	}
}

