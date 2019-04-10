package utilities.java8.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public abstract class AbstractColferObject implements ColferObject, KryoSerializable {
	@Override
	public byte[] marshal() {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			marshal(bos, null);
			return bos.toByteArray();
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public void unmarshal(byte[] data) {
		unmarshal(data, 0);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		try {
			int len = input.available();
			byte[] data = new byte[len];
			input.read(data);
			unmarshal(data);
		} catch (IOException e) {
		}
	}

	@Override
	public void write(Kryo kryo, Output output) {
		byte[] data = marshal();
		output.write(data);
	}
}
