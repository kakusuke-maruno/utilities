package utilities.rsocket.kvs.storage;

import java.io.Closeable;
import java.util.List;

import utilities.rsocket.kvs.storage.exception.StorageException;

public interface Storage extends Closeable {
	void open(String name, String properties) throws StorageException;

	byte[] get(byte[] key) throws StorageException;

	void put(byte[] key, byte[] value) throws StorageException;

	void remove(byte[] key) throws StorageException;

	boolean putIfAbsent(byte[] key, byte[] value) throws StorageException;

	boolean compareAndPut(byte[] key, byte[] value, byte[] expect) throws StorageException;

	boolean compareAndRemove(byte[] key, byte[] expect) throws StorageException;

	void write(StorageBatch updates) throws StorageException;

	List<StorageEntry> bulkGet(List<byte[]> keys) throws StorageException;
}
