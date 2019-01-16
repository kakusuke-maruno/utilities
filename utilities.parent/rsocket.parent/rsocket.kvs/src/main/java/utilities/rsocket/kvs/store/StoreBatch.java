package utilities.rsocket.kvs.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import utilities.rsocket.kvs.ColferObject;

public class StoreBatch<Key extends ColferObject, Value extends ColferObject> {
	private final List<StoreEntry<Key, Value>> list = new ArrayList<>();

	public StoreBatch() {
	}

	public void put(Key key, Value value) {
		list.add(new StoreEntry<>(key, value));
	}

	public void remove(Key key) {
		list.add(new StoreEntry<>(key));
	}

	public List<StoreEntry<Key, Value>> entries() {
		return Collections.unmodifiableList(list);
	}
}
