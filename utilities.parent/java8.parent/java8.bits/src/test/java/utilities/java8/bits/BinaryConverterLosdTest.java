package utilities.java8.bits;

import org.junit.Test;

public class BinaryConverterLosdTest {
	private static long LOOP = 1_000_000_000L;

	@Test
	public void test_001_11_putBCD_LoadTest() {
		long val = 1234;
		byte[] b = new byte[100];
		long start = System.currentTimeMillis();
		long loopCount = 0;
		while (loopCount++ < LOOP) {
			BinaryConverter.putBCD(b, 10, 2, val);
		}
		System.out.format("putBCD elapsed:%d\n", System.currentTimeMillis() - start);
	}

	@Test
	public void test_001_12_getBCD_LoadTest() {
		byte[] b0 = { (byte) 0x12, (byte) 0x34 };
		byte[] b = new byte[100];
		System.arraycopy(b0, 0, b, 10, 2);
		long start = System.currentTimeMillis();
		long loopCount = 0;
		while (loopCount++ < LOOP) {
			BinaryConverter.getBCD(b, 10, 2);
		}
		System.out.format("getBCD elapsed:%d\n", System.currentTimeMillis() - start);
	}

	@Test
	public void test_002_11_putAscii_LoadTest() {
		char val = '8';
		byte[] b = new byte[100];
		long start = System.currentTimeMillis();
		long loopCount = 0;
		while (loopCount++ < LOOP) {
			BinaryConverter.putAscii(b, 10, val);
		}
		System.out.format("putAscii elapsed:%d\n", System.currentTimeMillis() - start);
	}

	@Test
	public void test_002_12_getAscii_LoadTest() {
		byte[] b0 = { (byte) 0x37 };
		byte[] b = new byte[100];
		System.arraycopy(b0, 0, b, 10, b0.length);
		long start = System.currentTimeMillis();
		long loopCount = 0;
		while (loopCount++ < LOOP) {
			BinaryConverter.getAscii(b, 10);
		}
		System.out.format("getAscii elapsed:%d\n", System.currentTimeMillis() - start);
	}

	@Test
	public void test_003_11_putAsciiArray_LoadTest() {
		char[] val = { '3', '4' };
		byte[] b = new byte[100];
		long start = System.currentTimeMillis();
		long loopCount = 0;
		while (loopCount++ < LOOP) {
			BinaryConverter.putAscii(b, 10, val);
		}
		System.out.format("putAsciiArray elapsed:%d\n", System.currentTimeMillis() - start);
	}

	@Test
	public void test_003_12_getAsciiArray_LoadTest() {
		byte[] b0 = { (byte) 0x36, (byte) 0x31 };
		byte[] b = new byte[100];
		System.arraycopy(b0, 0, b, 10, 2);
		long start = System.currentTimeMillis();
		long loopCount = 0;
		while (loopCount++ < LOOP) {
			BinaryConverter.getAscii(b, 10, 2);
		}
		System.out.format("getAsciiArray elapsed:%d\n", System.currentTimeMillis() - start);
	}

	@Test
	public void test_004_11_putHex_LoadTest() {
		byte[] b = new byte[100];
		long start = System.currentTimeMillis();
		long loopCount = 0;
		while (loopCount++ < LOOP) {
			BinaryConverter.putHex(b, 10, 2, 0x1234L);
		}
		System.out.format("putHex elapsed:%d\n", System.currentTimeMillis() - start);
	}

	@Test
	public void test_004_12_getHex_LoadTest() {
		byte[] b0 = { (byte) 0x12, (byte) 0x34 };
		byte[] b = new byte[100];
		System.arraycopy(b0, 0, b, 10, b0.length);
		long start = System.currentTimeMillis();
		long loopCount = 0;
		while (loopCount++ < LOOP) {
			BinaryConverter.getHex(b, 10, 2);
		}
		System.out.format("getHex elapsed:%d\n", System.currentTimeMillis() - start);
	}
}
