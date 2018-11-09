package utilities.java8.bits;

public class BinaryConverter {
	private BinaryConverter() {
	}

	public static long getBCD(byte[] b, int off, int len) {
		long result = 0;
		for (int i = 0; i < len; i++) {
			result *= 10;
			result += (b[off + i] >> 4) & 0x0f;
			result *= 10;
			result += b[off + i] & 0x0f;
		}
		return result;
	}

	public static char getAscii(byte[] b, int off) {
		return (char) b[off];
	}

	public static char[] getAscii(byte[] b, int off, int len) {
		char[] result = new char[len];
		for (int i = 0; i < len; i++) {
			result[i] = (char) (b[off + i] & 0xff);
		}
		return result;
	}

	public static long getHex(byte[] b, int off, int len) {
		long result = 0L;
		for (int i = 0; i < len; i++) {
			result = result << Byte.SIZE;
			result = result | (b[off + i] & 0xffL);
		}
		return result;
	}

	public static void putBCD(byte[] b, int off, int len, long val) {
		long buf = val;
		int pos = off + len - 1;
		for (int i = 0; i < len; i++) {
			b[pos] = (byte) (((((buf / 10) % 10) & 0xf) << 4) | ((buf % 10) & 0xf));
			buf /= 100;
			pos--;
		}
	}

	public static void putAscii(byte[] b, int off, char val) {
		b[off] = (byte) (val & 0xff);
	}

	public static void putAscii(byte[] b, int off, char[] val) {
		int pos = off;
		for (int i = 0; i < val.length; i++) {
			b[pos++] = (byte) (val[i] & 0xff);
		}
	}

	public static void putHex(byte[] b, int off, int len, long val) {
		long buf = val;
		int pos = off + len - 1;
		for (int i = 0; i < len; i++) {
			b[pos] = (byte) (buf & 0xffL);
			buf >>>= Byte.SIZE;
			pos--;
		}
	}
}
