package utilities.rxjava2.window;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import utilities.rxjava2.flow.AbstractSubscriberWrapper;

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

	static class SubscriberWapper<K, V> extends AbstractSubscriberWrapper<V, KeyBreakWindow<K, V>> {
		final Collection<V> buffer = new ArrayList<>();
		final AtomicReference<K> lastKey = new AtomicReference<>();
		final Function<V, K> keyCalcurator;
		boolean done;

		public SubscriberWapper(Subscriber<? super KeyBreakWindow<K, V>> downstream, Function<V, K> keyCalcurator) {
			super(downstream);
			this.keyCalcurator = keyCalcurator;
		}

		@Override
		public void onNext0(V t) throws Exception {
			K key;
			key = keyCalcurator.apply(t);
			if (lastKey.get() != null && !lastKey.get().equals(key)) {
				downstream.onNext(new KeyBreakWindow<>(lastKey.get(), new ArrayList<>(buffer)));
				buffer.clear();
			}
			buffer.add(t);
			lastKey.set(key);
		}

		@Override
		public void onComplete0() {
			if (buffer.size() > 0) {
				downstream.onNext(new KeyBreakWindow<>(lastKey.get(), new ArrayList<>(buffer)));
				buffer.clear();
			}
		}
	}
}
