package utilities.rxjava2.window;

import java.util.Collection;

public class PartitionWindow<T> {
	public final int partition;
	public final Collection<T> windowValues;

	public PartitionWindow(int partition, Collection<T> windowValues) {
		this.partition = partition;
		this.windowValues = windowValues;
	}
}
