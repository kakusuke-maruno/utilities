package utilities.scalecubeio.kvs;

import java.util.ArrayList;
import java.util.List;

public class KvsBatch {
	private List<KvsOperation> operations = new ArrayList<>();

	public List<KvsOperation> getOperations() {
		return operations;
	}

	public void put(byte[] key, byte[] value) {
		KvsOperation operation = new KvsOperation();
		operation.setAction(KvsOperationType.PUT);
		operation.setKey(key);
		operation.setValue(value);
		operations.add(operation);
	}

	public void remove(byte[] key) {
		KvsOperation operation = new KvsOperation();
		operation.setAction(KvsOperationType.REMOVE);
		operation.setKey(key);
		operations.add(operation);
	}
}
