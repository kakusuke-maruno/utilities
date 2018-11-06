package utilities.rxjava2.window;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.plugins.RxJavaPlugins;

public abstract class AbstractSubscriberWrapper<T, R> implements Subscriber<T>, Subscription {
	protected Subscription upstream;
	protected final Subscriber<? super R> downstream;
	protected boolean done = false;

	protected AbstractSubscriberWrapper(Subscriber<? super R> downstream) {
		this.downstream = downstream;
	}

	@Override
	public void request(long n) {
		upstream.request(n);
	}

	@Override
	public void cancel() {
		upstream.cancel();
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
			onNext0(t);
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
		onComplete0();
		done = true;
		downstream.onComplete();
	}

	protected abstract void onNext0(T t) throws Exception;

	protected void onComplete0() {
	}
}
