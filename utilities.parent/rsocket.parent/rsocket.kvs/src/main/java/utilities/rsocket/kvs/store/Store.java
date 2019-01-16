package utilities.rsocket.kvs.store;

import java.util.List;

import utilities.rsocket.kvs.ColferObject;

public interface Store<Key extends ColferObject, Value extends ColferObject> {
	StoreFuture<Value> get(Key key);

	StoreFuture<Void> put(Key key, Value value);

	StoreFuture<Boolean> putIfAbsent(Key key, Value value);

	StoreFuture<Void> remove(Key key);

	StoreFuture<Boolean> compareAndPut(Key key, Value value, Value expect);

	StoreFuture<Boolean> compareAndRemove(Key key, Value expect);

	StoreFuture<Void> write(StoreBatch<Key, Value> updates);

	StoreFuture<List<StoreEntry<Key, Value>>> bulkGet(List<Key> keys);
}
