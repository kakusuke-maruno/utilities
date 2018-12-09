package utilities.scalecubeio.kvs;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import utilities.scalecubeio.kvs.internal.Connection;

public class KvsManager {
	private final Properties config;
	private final ConcurrentMap<Integer, Connection> connectionMap = new ConcurrentHashMap<>();

	public KvsManager(Properties config) {
		this.config = config;
	}

	public Connection open(Integer serverId) {
		return connectionMap.computeIfAbsent(serverId, this::open0);
	}

	private Connection open0(Integer serverId) {
		return new Connection(serverId, config);
	}
}
