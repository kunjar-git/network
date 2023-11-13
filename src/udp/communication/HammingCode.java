package udp.communication;

@SuppressWarnings(value = { "all" })
public class HammingCode {
	/**
	 * 偶校验
	 */
	public static final int EVEN_PARITY = 0;
	/**
	 * 奇校验
	 */
	public static final int ODD_PARITY = 1;
	/**
	 * 编码译码时使用的校验模式，默认为偶校验
	 */
	private int checkMode = EVEN_PARITY;

	/**
	 * 译码是否成功
	 */
	private boolean decodeSuccess = false;

	/**
	 * 奇偶译码是否成功
	 */
	private boolean decodetotalSuccess = false;
	/**
	 * 译码的时候，是否检测到输入错误
	 */
	private boolean errorSrc = false;
	/**
	 * 译码的时候，检测到输入错误在第几位
	 */
	private Integer errorIndex;

	// 无参构造
	public HammingCode() {
	}

	// 有参构造， 用与选择是奇校验还是偶校验
	public HammingCode(int checkMode) {
		if (checkMode == ODD_PARITY) {
			this.checkMode = ODD_PARITY;
		} else {
			this.checkMode = EVEN_PARITY;
		}
	}

	// 编码
	public String code(String src) {
		// 校验输入数据
		if (src == null || src.equals("")) {
			return "输入为空";
		}
		String[] srcArray = src.trim().split("");
		for (String srcChar : srcArray) {
			if (!srcChar.equals("0") && !srcChar.equals("1")) {
				return "输入格式不正确";
			}
		}

		int n = src.length();
		int x = 0;
		while ((1 << x) - 1 < n + x)
			x++;

		// 将原数据填充到扩增的数组中的响应位置
		int[] resultArray = new int[n + x];
		for (int i = 0, j = 0, checkIndex = 1; i < resultArray.length; i++) {
			if (checkIndex == i + 1) {
				checkIndex = checkIndex << 1;
			} else {
				resultArray[i] = Integer.parseInt(srcArray[j++]);
			}
		}

		// 填充数组中校验位的数据
		for (int i = 1; i < resultArray.length; i = i << 1) {
			int verification = checkMode;
			for (int j = i + 1; j <= resultArray.length; j++) {
				if ((i & j) != 0) {
					verification = verification ^ resultArray[j - 1];
				}
			}
			resultArray[i - 1] = verification;
		}

		// 获取返回字符串
		StringBuilder result = new StringBuilder();
		int count = 0;
		for (int i : resultArray) {
			if (i == 1) {
				count++;
			}
			result.append(i);
		}
		if (count % 2 == 0) {
			return checkMode == EVEN_PARITY ? result.toString() + "0" : result.toString() + "1";
		}
		return checkMode == EVEN_PARITY ? result.toString() + "1" : result.toString() + "0";
	}

	public boolean gettotal(String src) {
		int count = 0;
		String[] srcArrayStrings = src.trim().split("");
		for (String srcCharString : srcArrayStrings) {
			if (srcCharString.equals("1")) {
				count++;
			}
		}
		return (count % 2 == 0 && checkMode == EVEN_PARITY) || (count % 2 == 1 && checkMode == ODD_PARITY) ? true
				: false;
	}

	// 译码
	public String decode(String src) {
		// 校验输入数据
		if (src == null || src.equals("")) {
			return "输入为空";
		}
		String[] srcArray = src.trim().split("");
		for (String srcChar : srcArray) {
			if (!srcChar.equals("0") && !srcChar.equals("1")) {
				return "输入格式不正确";
			}
		}
		decodetotalSuccess = gettotal(src);
		// 初始化译码相关的结果数据
		initParam();

		int n = src.length() - 1;
		int x = 0;
		while ((1 << x) - 1 < n)
			x++;

		// 从左到右，获取通过校验位校验后的结果
		int[] checkInts = new int[x];
		for (int i = 1, index = 0; i < n; i = i << 1) {
			int verification = Integer.parseInt(srcArray[i - 1]);
			for (int j = i + 1; j <= n; j++) {
				if ((i & j) != 0) {
					verification = verification ^ Integer.parseInt(srcArray[j - 1]);
				}
			}
			// 奇偶校验对应不同的值
			checkInts[index++] = verification == checkMode ? 0 : 1;
		}

		// 通过校验位，获取出错在第几位
		int errorIndexNum = 0;
//       System.out.println(Arrays.toString(checkInts));
		for (int i = 0; i < x; i++) {
			if (checkInts[i] != 0) {
				errorIndexNum += (1 << i);
			}
		}

		// 如果errorIndex大于输入的长度，代表输入数据错误肯定不止一位
		if (errorIndexNum - 1 > n) {
			decodeSuccess = false;
			errorSrc = true;
			return "输入数据有误，可能包含不止一位错误，无法解码";
		}
		// 如果出错，纠正错误
		if (errorIndexNum > 0) {
			String indexStr = srcArray[errorIndexNum - 1];
			if (indexStr.equals("0")) {
				srcArray[errorIndexNum - 1] = "1";
			} else {
				srcArray[errorIndexNum - 1] = "0";
			}
			errorSrc = true;
			errorIndex = errorIndexNum;
		}
		if (decodetotalSuccess && errorSrc) {
			// 译码失败
			decodeSuccess = false;
			return "存在两位错误，无法纠正";
		}
		// 译码成功，获取并返回原数据
		decodeSuccess = true;
		StringBuilder result = new StringBuilder();
		for (int i = 0, checkIndex = 1; i < n; i++) {
			if (checkIndex == i + 1) {
				checkIndex = checkIndex << 1;
			} else {
				result.append(srcArray[i]);
			}
		}
		return result.toString();
	}

	/**
	 * 初始化译码相关的结果数据
	 */
	private void initParam() {
		decodeSuccess = false;
		errorSrc = false;
		errorIndex = null;
	}

	public boolean isDecodeSuccess() {
		return decodeSuccess;
	}

	public boolean isErrorSrc() {
		return errorSrc;
	}

	public Integer getErrorIndex() {
		return errorIndex;
	}
}
