package utilities.java8.bits;

import static org.junit.Assert.*;

import org.junit.Test;

public class BinaryConverterTest {

	@Test
	public void test_001_01_putBCD() {
		long val = 9998;
		byte[] b = new byte[2];
		BinaryConverter.putBCD(b, 0, b.length, val);
		assertTrue("mistake of conversion from BCD", b[0] == (byte) 0x99 & b[1] == (byte) 0x98);
	}

	@Test
	public void test_001_02_getBCD() {
		byte[] b = { (byte) 0x12, (byte) 0x34 };
		long val = BinaryConverter.getBCD(b, 0, b.length);
		assertTrue("mistake of conversion to BCD", val == 1234);
	}

	@Test
	public void test_002_01_putAscii() {
		char val = '8';
		byte[] b = new byte[1];
		BinaryConverter.putAscii(b, 0, val);
		assertTrue("mistake of conversion from Ascii", b[0] == (byte) 0x38);
	}

	@Test
	public void test_002_02_getAscii() {
		byte[] b = { (byte) 0x37 };
		char val = BinaryConverter.getAscii(b, 0);
		assertTrue("mistake of conversion to Ascii", val == '7');
	}

	@Test
	public void test_003_01_putAsciiArray() {
		char[] val = { '3', '4' };
		byte[] b = new byte[val.length];
		BinaryConverter.putAscii(b, 0, val);
		assertTrue("mistake of conversion from Ascii array", b[0] == (byte) 0x33 && b[1] == (byte) 0x34);
	}

	@Test
	public void test_003_02_getAsciiArray() {
		byte[] b = { (byte) 0x36, 0x31 };
		char[] val = BinaryConverter.getAscii(b, 0, b.length);
		assertTrue("mistake of conversion to Ascii array", val[0] == '6' && val[1] == '1');
	}

}
