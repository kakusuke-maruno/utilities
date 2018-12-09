package utilities.scalecubeio.kvs.spi;

import java.io.IOException;
import java.util.Properties;

import utilities.scalecubeio.kvs.Kvs;
import utilities.scalecubeio.kvs.server.KvsServer;

public interface KvsProvider {
	String providerName();

	Kvs provide(KvsServer kvsServer, Integer serverId, String name, Properties serverConfig, Properties config) throws IOException;
}
