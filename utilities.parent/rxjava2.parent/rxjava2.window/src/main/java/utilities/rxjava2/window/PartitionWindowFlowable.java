package utilities.rxjava2.window;

import java.util.ArrayList;
import java.util.Collection;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;

public class PartitionWindowFlowable<T> extends Flowable<PartitionWindow<T>> {
	public static <T> Function<Publisher<T>, PartitionWindowFlowable<T>> of(Function<T, Integer> partitionCalcurator, int partitionCount, int bufferSize) {
		return publisher -> new PartitionWindowFlowable<>(publisher, partitionCalcurator, partitionCount, bufferSize);
	}

	private final Publisher<T> source;
	private final Function<T, Integer> partitionCalcurator;
	private final int partitionCount;
	private final int bufferSize;

	public PartitionWindowFlowable(Publisher<T> source, Function<T, Integer> partitionCalcurator, int partitionCount, int bufferSize) {
		this.source = source;
		this.partitionCalcurator = partitionCalcurator;
		this.partitionCount = partitionCount;
		this.bufferSize = bufferSize;
	}

	@Override
	protected void subscribeActual(Subscriber<? super PartitionWindow<T>> s) {
		Subscriber<T> wrappedSubscriber = new SubscriberWapper<>(s, partitionCount, partitionCalcurator, bufferSize);
		source.subscribe(wrappedSubscriber);
	}

	static class SubscriberWapper<T> extends AbstractSubscriberWrapper<T, PartitionWindow<T>> {
		final Collection<T>[] buffer;
		final Function<T, Integer> partitionCalcurator;
		final int partitionCount;
		final int bufferSize;
		boolean done;

		@SuppressWarnings("unchecked")
		public SubscriberWapper(Subscriber<? super PartitionWindow<T>> downstream, int partitionCount, Function<T, Integer> partitionCalcurator, int bufferSize) {
			super(downstream);
			this.partitionCalcurator = partitionCalcurator;
			this.partitionCount = partitionCount;
			this.bufferSize = bufferSize;
			this.buffer = new Collection[partitionCount];
			for (int partition = 0; partition < partitionCount; partition++) {
				this.buffer[partition] = new ArrayList<>(bufferSize);
			}
		}

		@Override
		public void onNext0(T t) throws Exception {
			int partition = partitionCalcurator.apply(t);
			buffer[partition].add(t);
			if (buffer[partition].size() >= bufferSize) {
				downstream.onNext(new PartitionWindow<>(partition, new ArrayList<>(buffer[partition])));
				buffer[partition].clear();
			}
		}

		@Override
		public void onComplete0() {
			for (int partition = 0; partition < buffer.length; partition++) {
				if (buffer[partition].size() > 0) {
					downstream.onNext(new PartitionWindow<>(partition, new ArrayList<>(buffer[partition])));
					buffer[partition].clear();
				}
			}
		}
	}
}
