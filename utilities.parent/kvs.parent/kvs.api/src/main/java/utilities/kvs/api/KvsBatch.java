package utilities.kvs.api;

import java.util.ArrayList;
import java.util.List;

import utilities.kvs.dto.BatchEntry;
import utilities.kvs.dto.WriteOperation;

public class KvsBatch {
	private List<BatchEntry> operations = new ArrayList<>();

	public List<BatchEntry> getOperations() {
		return operations;
	}

	public void put(byte[] key, byte[] value) {
		BatchEntry entry = new BatchEntry();
		entry.setKey(key);
		entry.setValue(value);
		operations.add(entry);
	}

	public void remove(byte[] key) {
		BatchEntry entry = new BatchEntry();
		entry.setKey(key);
		operations.add(entry);
	}
}
