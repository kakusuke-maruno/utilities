package utilities.rsocket.kvs.store;

import utilities.rsocket.kvs.ColferObject;

public class StoreEntry<Key extends ColferObject, Value extends ColferObject> {
	private Key key;
	private Value value;

	public StoreEntry() {
		this.key = null;
		this.value = null;
	}

	public StoreEntry(Key key) {
		this.key = key;
		this.value = null;
	}

	public StoreEntry(Key key, Value value) {
		this.key = key;
		this.value = value;
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}
}
