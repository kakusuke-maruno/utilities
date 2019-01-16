package utilities.rsocket.kvs.store;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import utilities.rsocket.kvs.store.exception.StoreException;

public class StoreFuture<T> {
	private final CompletableFuture<byte[]> future = new CompletableFuture<>();
	private final Deserializer<T> deserializer;

	StoreFuture(Deserializer<T> deserializer) {
		this.deserializer = deserializer;
	}

	public boolean cancel(boolean mayInterruptIfRunning) {
		return future.cancel(mayInterruptIfRunning);
	}

	public boolean isCancelled() {
		return future.isCancelled();
	}

	public boolean isDone() {
		return future.isDone();
	}

	public T get() throws InterruptedException, StoreException {
		try {
			return deserializer.deserialize(future.get());
		} catch (ExecutionException e) {
			if (e.getCause() != null) {
				throw new StoreException(e.getCause());
			}
			throw new StoreException(e);
		}
	}

	public T getUninterruptibly() throws StoreException {
		try {
			return deserializer.deserialize(future.get());
		} catch (ExecutionException e) {
			if (e.getCause() != null) {
				throw new StoreException(e.getCause());
			}
			throw new StoreException(e);
		} catch (InterruptedException e) {
			throw new StoreException(e);
		}
	}

	public T get(long timeout, TimeUnit unit) throws InterruptedException, StoreException, TimeoutException {
		try {
			return deserializer.deserialize(future.get(timeout, unit));
		} catch (ExecutionException e) {
			if (e.getCause() != null) {
				throw new StoreException(e.getCause());
			}
			throw new StoreException(e);
		}
	}

	boolean complete(byte[] value) {
		return future.complete(value);
	}

	boolean completeExceptionally(Throwable ex) {
		return future.completeExceptionally(ex);
	}
}
