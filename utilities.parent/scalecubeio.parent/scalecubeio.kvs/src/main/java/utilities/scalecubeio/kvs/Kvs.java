package utilities.scalecubeio.kvs;

import java.io.IOException;

public interface Kvs {
	byte[] get(byte[] key) throws IOException;

	void put(byte[] key, byte[] value) throws IOException;

	void remove(byte[] key) throws IOException;

	boolean putIfAbsent(byte[] key, byte[] value) throws IOException;

	boolean compareAndPut(byte[] key, byte[] value, byte[] expectedValue) throws IOException;

	boolean compareAndRemove(byte[] key, byte[] expectedValue) throws IOException;

	void write(KvsBatch batch) throws IOException;
}
