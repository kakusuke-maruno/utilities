package utilities.rsocket.kvs.storage;

import java.util.ArrayList;
import java.util.List;

public class StorageBatch {
	public final List<StorageEntry> updates = new ArrayList<>();

	public void put(byte[] key, byte[] value) {
		updates.add(new StorageEntry(key, value));
	}

	public void remove(byte[] key) {
		updates.add(new StorageEntry(key, null));
	}
}
