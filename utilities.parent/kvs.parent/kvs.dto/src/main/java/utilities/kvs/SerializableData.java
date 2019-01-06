package utilities.kvs;

import java.io.IOException;
import java.io.OutputStream;

public abstract class SerializableData {
	public SerializableData() {
		String className = this.getClass().getSimpleName();
		setDataType((byte) getTypeNo(className));
	}

	private static int getTypeNo(String dataType) {
		switch (dataType) {
		case "DefaultOperationResult":
			return 1;
		case "BooleanOperationResult":
			return 2;
		case "PutOperation":
			return 3;
		case "GetOperation":
			return 4;
		case "GetOperationResult":
			return 5;
		case "RemoveOperation":
			return 6;
		case "PutIfAbsentOperation":
			return 7;
		case "CompareAndPutOperation":
			return 8;
		case "CompareAndRemoveOperation":
			return 9;
		case "BatchEntry":
			return 10;
		case "WriteOperation":
			return 11;
		default:
			return 0;
		}

	}

	abstract public void setDataType(byte value);

	abstract public int unmarshal(byte[] buf, int offset);

	abstract public int unmarshal(byte[] buf, int offset, int end);

	abstract public byte[] marshal(OutputStream out, byte[] buf) throws IOException;

	abstract public int marshal(byte[] buf, int offset);
}
