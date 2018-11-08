package utilities.java8.bits;

public class BinaryConverter {
	private BinaryConverter() {
	}

	public static long getBCD(byte[] b, int off, int len) {
		long result = 0;
		for (int i = 0; i < len; i++) {
			result *= 10;
			result += (b[i] >> 4) & 0x0f;
			result *= 10;
			result += b[i] & 0x0f;
		}
		return result;
	}

	public static char getAscii(byte[] b, int off) {
		return (char) b[off];
	}

	public static char[] getAscii(byte[] b, int off, int len) {
		char[] result = new char[len];
		for (int i = 0; i < len; i++) {
			result[i] = (char) (b[i] & 0xff);
		}
		return result;
	}

	public static void putBCD(byte[] b, int off, int len, long val) {
		long buf = val;
		for (int i = 0; i < len; i++) {
			b[len - i - 1] = (byte) (((((buf / 10) % 10) & 0xf) << 4) | ((buf % 10) & 0xf));
			buf /= 100;
		}
	}

	public static void putAscii(byte[] b, int off, char val) {
		b[off] = (byte) (val & 0xff);
	}

	public static void putAscii(byte[] b, int off, char[] val) {
		for (int i = 0; i < val.length; i++) {
			b[off + i] = (byte) (val[i] & 0xff);
		}
	}
}
