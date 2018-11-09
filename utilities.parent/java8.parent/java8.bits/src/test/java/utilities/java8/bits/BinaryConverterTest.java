package utilities.java8.bits;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BinaryConverterTest {
	@Test
	public void test_001_01_putBCD() {
		long val = 1234;
		byte[] b = new byte[100];
		BinaryConverter.putBCD(b, 10, 2, val);
		assertTrue("mistake of conversion from BCD to binary. " + String.format("b[10] = %02X, b[11] = %02X", b[10], b[11]), b[10] == (byte) 0x12 & b[11] == (byte) 0x34);
	}

	@Test
	public void test_001_02_getBCD() {
		byte[] b0 = { (byte) 0x12, (byte) 0x34 };
		byte[] b = new byte[100];
		System.arraycopy(b0, 0, b, 10, 2);
		long val = BinaryConverter.getBCD(b, 10, 2);
		assertTrue("mistake of conversion from binary to BCD. " + String.format("val = %d", val), val == 1234);
	}

	@Test
	public void test_002_01_putAscii() {
		char val = '8';
		byte[] b = new byte[100];
		BinaryConverter.putAscii(b, 10, val);
		assertTrue("mistake of conversion from Ascii to binary. " + String.format("b[10] = %02X", b[10]), b[10] == (byte) 0x38);
	}

	@Test
	public void test_002_02_getAscii() {
		byte[] b0 = { (byte) 0x37 };
		byte[] b = new byte[100];
		System.arraycopy(b0, 0, b, 10, b0.length);
		char val = BinaryConverter.getAscii(b, 10);
		assertTrue("mistake of conversion from byte to Ascii. " + String.format("val = %s", val), val == '7');
	}

	@Test
	public void test_003_01_putAsciiArray() {
		char[] val = { '3', '4' };
		byte[] b = new byte[100];
		BinaryConverter.putAscii(b, 10, val);
		assertTrue("mistake of conversion from Ascii array to binary. " + String.format("b[10] = %02X, b[11] = %02X", b[10], b[11]), b[10] == (byte) 0x33 && b[11] == (byte) 0x34);
	}

	@Test
	public void test_003_02_getAsciiArray() {
		byte[] b0 = { (byte) 0x36, (byte) 0x31 };
		byte[] b = new byte[100];
		System.arraycopy(b0, 0, b, 10, b0.length);
		char[] val = BinaryConverter.getAscii(b, 10, 2);
		assertTrue("mistake of conversion from binary to Ascii array. " + String.format("val[0] = %s, val[1] = %s", val[0], val[1]), val[0] == '6' && val[1] == '1');
	}

	@Test
	public void test_004_01_putHex() {
		byte[] b = new byte[100];
		BinaryConverter.putHex(b, 10, 2, 0x1234L);
		assertTrue("mistake of conversion from long to hex. " + String.format("b[10] = %02X, b[11] = %02X", b[10], b[11]), b[10] == (byte) 0x12 && b[11] == (byte) 0x34);
	}

	@Test
	public void test_004_02_getHex() {
		byte[] b0 = { (byte) 0x12, (byte) 0x34 };
		byte[] b = new byte[100];
		System.arraycopy(b0, 0, b, 10, b0.length);
		long val = BinaryConverter.getHex(b, 10, 2);
		assertTrue("mistake of conversion from hex to long :" + val, val == 0x1234L);
	}
}
