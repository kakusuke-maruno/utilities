package utilities.rxjava2.window;

import java.util.Collection;

public class KeyBreakWindow<K, V> {
	public final K key;
	public final Collection<V> values;

	public KeyBreakWindow(K key, Collection<V> values) {
		this.key = key;
		this.values = values;
	}
}
