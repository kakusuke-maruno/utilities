package utilities.rsocket.kvs.storage.spi;

import utilities.rsocket.kvs.storage.Storage;

public interface StorageProvider {
	String providerName();

	Storage provide();
}
