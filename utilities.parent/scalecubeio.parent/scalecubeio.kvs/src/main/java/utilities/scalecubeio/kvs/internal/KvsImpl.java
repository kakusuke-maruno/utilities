package utilities.scalecubeio.kvs.internal;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import utilities.scalecubeio.kvs.Kvs;
import utilities.scalecubeio.kvs.KvsBatch;
import utilities.scalecubeio.kvs.KvsOperation;
import utilities.scalecubeio.kvs.KvsOperationType;

public class KvsImpl implements Kvs {
	private final Connection connection;
	private final String name;
	private final Properties config;

	public KvsImpl(Connection connection, String name, Properties config) throws IOException {
		this.connection = connection;
		this.name = name;
		this.config = config;
		open(this.name, this.config);
	}

	private void open(String name, Properties config) throws IOException {
		connection.openKvs(name, config);
	}

	private byte[] send(KvsOperation operation) throws IOException {
		try {
			return connection.sendOperationRequest(operation).get();
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e.getCause());
		}
	}

	@Override
	public byte[] get(byte[] key) throws IOException {
		KvsOperation operation = new KvsOperation();
		operation.setAction(KvsOperationType.GET);
		operation.setKey(key);
		return send(operation);
	}

	@Override
	public void put(byte[] key, byte[] value) throws IOException {
		KvsOperation operation = new KvsOperation();
		operation.setAction(KvsOperationType.PUT);
		operation.setKey(key);
		operation.setValue(value);
		send(operation);
	}

	@Override
	public void remove(byte[] key) throws IOException {
		KvsOperation operation = new KvsOperation();
		operation.setAction(KvsOperationType.REMOVE);
		operation.setKey(key);
		send(operation);
	}

	@Override
	public boolean putIfAbsent(byte[] key, byte[] value) throws IOException {
		KvsOperation operation = new KvsOperation();
		operation.setAction(KvsOperationType.PUTIFABSENT);
		operation.setKey(key);
		operation.setValue(value);
		byte[] result = send(operation);
		return result != null && result[0] != 1;
	}

	@Override
	public boolean compareAndPut(byte[] key, byte[] value, byte[] expectedValue) throws IOException {
		KvsOperation operation = new KvsOperation();
		operation.setAction(KvsOperationType.COMPAREANDPUT);
		operation.setKey(key);
		operation.setValue(value);
		operation.setExpectedValue(expectedValue);
		byte[] result = send(operation);
		return result != null && result[0] != 1;
	}

	@Override
	public boolean compareAndRemove(byte[] key, byte[] expectedValue) throws IOException {
		KvsOperation operation = new KvsOperation();
		operation.setAction(KvsOperationType.COMPAREANDREMOVE);
		operation.setKey(key);
		operation.setExpectedValue(expectedValue);
		byte[] result = send(operation);
		return result != null && result[0] != 1;
	}

	@Override
	public void write(KvsBatch batch) throws IOException {
		KvsOperation operation = new KvsOperation();
		operation.setAction(KvsOperationType.WRITE);
		operation.setBatch(batch);
		send(operation);
	}

}
