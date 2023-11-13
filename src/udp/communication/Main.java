package udp.communication;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class Main {
	public static String getTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        System.out.println(currentDateTime);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:");
        String formattedCurrentDateTime = currentDateTime.format(formatter);
        return formattedCurrentDateTime;
	}
	public static void main(String[] args) {
		System.out.println(getTime());
		HammingCode hc = new HammingCode();
		String src = "10000001";
		String codestr = hc.code(src);
		String decodestr = hc.decode(codestr);
		System.out.println("src = " + src);
		System.out.println("codestr = " + codestr);
		System.out.println("decodestr = " + decodestr);
		System.out.println("====出错1位（last）");
		String newcodestr = "1111000100011";
		String newdecodestr = hc.decode(newcodestr);
		System.out.println("src = " + src);
		System.out.println("newcodestr = " + newcodestr);
		System.out.println("newdecodestr = " + newdecodestr);
		System.out.println("====出错2位（last 1 2）");
		String newcodestr2 = "1111000100001";
		String newdecodestr2 = hc.decode(newcodestr2);
		System.out.println("src = " + src);
		System.out.println("newcodestr2 = " + newcodestr2);
		System.out.println("newdecodestr2 = " + newdecodestr2);
		String tmp = "1010111101010000111111110000000010101001";
		System.out.println(tmp);
		System.out.println(hc.code(tmp));
		System.out.println(hc.decode(hc.code(tmp)));
//		10001010
//		11010100
//	   101011110
		System.out.println(addBinary(tmp));
		tmp += addBinary(tmp);
		System.out.println(addBinary(tmp));
		System.out.println(checkChecksum(addBinary(tmp)));
		System.out.println(generate8str("1111"));
	}

//	接收方的校验和计算，true：没错误
	public static boolean checkChecksum(String str) {
		return !str.contains("0");
	}

//	8位对齐，汉明码不用，校验和用
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

	// 发送方的校验和计算
	public static String addBinary(String str) {
		int n = str.length();
		if (n % 8 != 0) {
			return str;
		}
		int carry = 0;
		int pre = 0;
		int sum = 0;
		for (int i = 0; i < n; i += 8) {
			sum = 0;
			for (int j = i; j < i + 8; j++) {
				int digit = str.charAt(j) - '0';
				sum += digit << (7 - j % 8);
			}
			sum += (pre + carry);
			if (sum > 255) {
				carry = 1;
			} else {
				carry = 0;
			}
			pre = sum & 255;
		}
		String zheng_str = Integer.toBinaryString(pre);
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < 8 - zheng_str.length(); i++) {
			// 原本是0， 反码改为1
			result.append("1");
		}
		for (int i = 0; i < zheng_str.length(); i++) {
			if (zheng_str.charAt(i) == '1') {
				result.append("0");
			} else {
				result.append("1");
			}
		}
		return result.toString();
	}
}
