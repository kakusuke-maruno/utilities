package utilities.rxjava2.window;

import java.util.ArrayList;
import java.util.Collection;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;

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

	static class SubscriberWapper<T> implements Subscriber<T>, Subscription {
		Subscription upstream;
		final Subscriber<? super PartitionWindow<T>> downstream;
		final Collection<T>[] buffer;
		final Function<T, Integer> partitionCalcurator;
		final int partitionCount;
		final int bufferSize;
		boolean done;

		@SuppressWarnings("unchecked")
		public SubscriberWapper(Subscriber<? super PartitionWindow<T>> downstream, int partitionCount, Function<T, Integer> partitionCalcurator, int bufferSize) {
			this.downstream = downstream;
			this.partitionCalcurator = partitionCalcurator;
			this.partitionCount = partitionCount;
			this.bufferSize = bufferSize;
			this.buffer = new Collection[partitionCount];
			for (int partition = 0; partition < partitionCount; partition++) {
				this.buffer[partition] = new ArrayList<>(bufferSize);
			}
		}

		@Override
		public void onSubscribe(Subscription s) {
			upstream = s;
			downstream.onSubscribe(this);
		}

		@Override
		public void onNext(T t) {
			if (done) {
				return;
			}
			try {
				int partition = partitionCalcurator.apply(t);
				buffer[partition].add(t);
				if (buffer[partition].size() >= bufferSize) {
					downstream.onNext(new PartitionWindow<>(partition, new ArrayList<>(buffer[partition])));
					buffer[partition].clear();
				}
			} catch (Exception e) {
				onError(e);
			}
		}

		@Override
		public void onError(Throwable t) {
			if (done) {
				RxJavaPlugins.onError(t);
				return;
			}
			done = true;
			downstream.onError(t);
		}

		@Override
		public void onComplete() {
			if (done) {
				return;
			}
			for (int partition = 0; partition < buffer.length; partition++) {
				if (buffer[partition].size() > 0) {
					downstream.onNext(new PartitionWindow<>(partition, new ArrayList<>(buffer[partition])));
					buffer[partition].clear();
				}
			}
			done = true;
			downstream.onComplete();
		}

		@Override
		public void request(long n) {
			upstream.request(n);
		}

		@Override
		public void cancel() {
			upstream.cancel();
		}
	}
}
