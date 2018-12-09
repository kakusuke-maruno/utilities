package utilities.scalecubeio.kvs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import io.scalecube.transport.Address;
import io.scalecube.transport.Message;
import utilities.scalecubeio.kvs.internal.Bits;

public class KvsOperation {
	private String name = null;
	private Address sender = null;
	private String requestId;
	private KvsOperationType action = null;
	private byte[] key = null;
	private byte[] value = null;
	private byte[] expectedValue = null;
	private KvsBatch batch = null;

	public Message toMessage() {
		return Message.builder().header("name", name).header("requestId", "" + requestId).data(toBinary()).build();
	}

	public byte[] toBinary() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		append(bos, action.id);
		switch (action) {
		case WRITE:
			List<KvsOperation> list = batch.getOperations();
			append(bos, list.size());
			for (KvsOperation operation : list) {
				append(bos, operation.toBinary());
			}
			break;
		default:
			appendWithLen(bos, key);
			break;
		}
		switch (action) {
		case PUT:
		case PUTIFABSENT:
		case COMPAREANDPUT:
			appendWithLen(bos, value);
			break;
		default:
			break;
		}
		switch (action) {
		case COMPAREANDPUT:
		case COMPAREANDREMOVE:
			appendWithLen(bos, expectedValue);
			break;
		default:
			break;
		}
		try {
			bos.close();
		} catch (IOException e) {
		}
		return bos.toByteArray();

	}

	private void append(ByteArrayOutputStream bos, byte data) {
		bos.write(data);
	}

	private void append(ByteArrayOutputStream bos, int size) {
		byte[] dataLen = new byte[Integer.BYTES];
		Bits.putInt(dataLen, 0, size);
		try {
			bos.write(dataLen);
		} catch (IOException e) {
		}
	}

	private void append(ByteArrayOutputStream bos, byte[] data) {
		try {
			bos.write(data);
		} catch (IOException e) {
		}
	}

	private void appendWithLen(ByteArrayOutputStream bos, byte[] data) {
		append(bos, data.length);
		append(bos, data);
	}

	public void fromMessage(Message message) {
		name = message.header("name");
		fromBinary(message.data(), 0);
	}

	public int fromBinary(byte[] data, int off) {
		int pos = off;
		action = KvsOperationType.from(data[pos]);
		pos += Byte.BYTES;

		switch (action) {
		case WRITE:
			int size = Bits.getInt(data, pos);
			pos += Integer.BYTES;
			batch = new KvsBatch();
			for (int i = 0; i < size; i++) {
				KvsOperation operation = new KvsOperation();
				pos = operation.fromBinary(data, pos);
				batch.getOperations().add(operation);
			}
			break;
		default:
			key = getWithLen(data, pos);
			pos += Integer.BYTES + key.length;
			break;
		}
		switch (action) {
		case PUT:
		case PUTIFABSENT:
		case COMPAREANDPUT:
			value = getWithLen(data, pos);
			pos += Integer.BYTES + value.length;
			break;
		default:
			break;
		}
		switch (action) {
		case COMPAREANDPUT:
		case COMPAREANDREMOVE:
			expectedValue = getWithLen(data, pos);
			pos += Integer.BYTES + expectedValue.length;
			break;
		default:
			break;
		}
		return pos;
	}

	private byte[] getWithLen(byte[] data, int off) {
		int len = Bits.getInt(data, off);
		return Arrays.copyOfRange(data, off, off + len);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Address getSender() {
		return sender;
	}

	public void setSender(Address sender) {
		this.sender = sender;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public KvsOperationType getAction() {
		return action;
	}

	public void setAction(KvsOperationType action) {
		this.action = action;
	}

	public byte[] getKey() {
		return key;
	}

	public void setKey(byte[] key) {
		this.key = key;
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	public byte[] getExpectedValue() {
		return expectedValue;
	}

	public void setExpectedValue(byte[] expectedValue) {
		this.expectedValue = expectedValue;
	}

	public KvsBatch getBatch() {
		return batch;
	}

	public void setBatch(KvsBatch batch) {
		this.batch = batch;
	}
}
