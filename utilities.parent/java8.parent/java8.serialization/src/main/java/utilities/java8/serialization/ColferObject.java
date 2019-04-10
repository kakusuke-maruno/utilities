package utilities.java8.serialization;

import java.io.IOException;
import java.io.OutputStream;

public interface ColferObject {
	abstract byte[] marshal(OutputStream out, byte[] buf) throws IOException;

	abstract int marshal(byte[] buf, int offset);

	abstract int unmarshal(byte[] buf, int offset);

	abstract int unmarshal(byte[] buf, int offset, int end);

	byte[] marshal();

	void unmarshal(byte[] data);
}
