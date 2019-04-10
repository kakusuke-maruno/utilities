package utilities.java8.serialization;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class BinaryUtility {
	private BinaryUtility() {
	}

	public static void write(Writer writer, byte[] data) {
		try {
			for (byte b : data) {
				writer.write(String.format("%02x", b));
			}
		} catch (IOException e) {
		}
	}

	public static String toHexString(byte[] data) {
		StringWriter writer = new StringWriter(data.length * 2);
		write(writer, data);
		writer.flush();
		return writer.toString();
	}
}
