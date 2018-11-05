package utilities.rxjava2.window;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;

public class KeyBreakWindowFlowable<K, V> extends Flowable<KeyBreakWindow<K, V>> {
	public static <K, V> Function<Flowable<V>, Flowable<KeyBreakWindow<K, V>>> of(Function<V, K> keyCalcurator) {
		return flowable -> new KeyBreakWindowFlowable<>(flowable, keyCalcurator);
	}

	private final Publisher<V> source;
	private final Function<V, K> keyCalcurator;

	public KeyBreakWindowFlowable(Publisher<V> source, Function<V, K> keyCalcurator) {
		this.source = source;
		this.keyCalcurator = keyCalcurator;
	}

	@Override
	protected void subscribeActual(Subscriber<? super KeyBreakWindow<K, V>> s) {
		source.subscribe(new SubscriberWapper<>(s, keyCalcurator));
	}

	static class SubscriberWapper<K, V> implements Subscriber<V>, Subscription {
		Subscription upstream;
		final Subscriber<? super KeyBreakWindow<K, V>> downstream;
		final Collection<V> buffer = new ArrayList<>();
		final AtomicReference<K> lastKey = new AtomicReference<>();
		final Function<V, K> keyCalcurator;
		boolean done;

		public SubscriberWapper(Subscriber<? super KeyBreakWindow<K, V>> downstream, Function<V, K> keyCalcurator) {
			this.downstream = downstream;
			this.keyCalcurator = keyCalcurator;
		}

		@Override
		public void onSubscribe(Subscription s) {
			upstream = s;
			downstream.onSubscribe(this);
		}

		@Override
		public void onNext(V t) {
			if (done) {
				return;
			}
			K key;
			try {
				key = keyCalcurator.apply(t);
			} catch (Exception e) {
				onError(e);
				return;
			}
			if (lastKey.get() != null && !lastKey.get().equals(key)) {
				downstream.onNext(new KeyBreakWindow<>(lastKey.get(), new ArrayList<>(buffer)));
				buffer.clear();
			}
			buffer.add(t);
			lastKey.set(key);
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
			if (buffer.size() > 0) {
				downstream.onNext(new KeyBreakWindow<>(lastKey.get(), new ArrayList<>(buffer)));
				buffer.clear();
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
