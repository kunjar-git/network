package copy;

/*
 都是长度为5的数字
Master接收端口：10666
Master发送端口应该要来回切换才行！

slavex发送端口：2000x
slavex接收端口：3000x
*/
@SuppressWarnings(value = { "all" })
public class configuration {
	public final static int Master_Send_Port = 10666;
	public final static int Master_Receive_Port = 10888;
	public final static int Slave1_Send_Port = 20001;
	public final static int Slave1_Reveive_Port = 30001;
	public final static int Slave2_Send_Port = 20002;
	public final static int Slave2_Reveive_Port = 30002;
	public final static int Slave3_Send_Port = 20003;
	public final static int Slave3_Reveive_Port = 30003;
	public final static int T_LIMIT = 2573;
	public final static int T_for_REALITY = 5000;
	// 双字节 16位，不足前置补0。
	public final static String HEAD_STRING = "01010101";
	public final static String Master_Send_Port_STR = "0010100110101010";
	public final static String Master_Receive_Port_STR = "0010101010001000";
	public final static String Slave1_Send_Port_STR = "0100111000100001";
	public final static String Slave1_Reveive_Port_STR = "0111010100110001";
	public final static String Slave2_Send_Port_STR = "0100111000100010";
	public final static String Slave2_Reveive_Port_STR = "0111010100110010";
	public final static String Slave3_Send_Port_STR = "0100111000100011";
	public final static String Slave3_Reveive_Port_STR = "0111010100110011";

	public static int count = 1;
}
