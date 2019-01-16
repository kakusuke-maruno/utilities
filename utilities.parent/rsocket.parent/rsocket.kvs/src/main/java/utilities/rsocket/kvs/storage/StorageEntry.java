package utilities.rsocket.kvs.storage;

public class StorageEntry {
	public final byte[] key;
	public final byte[] value;

	public StorageEntry(byte[] key, byte[] value) {
		this.key = key;
		this.value = value;
	}
}
