package utilities.rsocket.kvs.store;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.internal.UnboundedProcessor;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Flux;
import utilities.rsocket.kvs.ColferObject;
import utilities.rsocket.kvs.store.exception.StoreException;
import utilities.rsocket.kvs.transport.KvsEntry;
import utilities.rsocket.kvs.transport.KvsEvent;
import utilities.rsocket.kvs.transport.KvsEventMeta;
import utilities.rsocket.kvs.transport.KvsEventResult;

public class StoreManager {
	private final ConcurrentMap<InetSocketAddress, Connection> connections = null;

	public StoreManager() {
	}

	public <Key extends ColferObject, Value extends ColferObject> Store<Key, Value> getStore(InetSocketAddress address, String name, Class<Key> keyClass, Class<Value> valueClass) {
		Connection connection = connections.computeIfAbsent(address, _address -> new Connection(_address));
		return ((Store<Key, Value>) connection.getStore(name, keyClass, valueClass));
	}
}

class Connection implements Closeable {
	private static final AtomicInteger THREAD_NO_GENERATOR = new AtomicInteger();
	private static final ThreadGroup THREAD_GROUP = new ThreadGroup("StoreManager.Connection");
	private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(THREAD_GROUP, r, "StoreManager.Connection#" + THREAD_NO_GENERATOR.incrementAndGet());
		}
	};
	private final ConcurrentMap<String, Store<?, ?>> stores = new ConcurrentHashMap<>();
	private final RSocket rsocket;
	private final ExecutorService connectionThreadPool = Executors.newSingleThreadExecutor(THREAD_FACTORY);
	private final Future<?> awaitClose;
	private final UnboundedProcessor<Event<?>> processor = new UnboundedProcessor<>();
	private final ConcurrentMap<Long, Event<?>> waiting = new ConcurrentHashMap<>();

	public Connection(InetSocketAddress address) {
		rsocket = RSocketFactory.connect().transport(TcpClientTransport.create(address)).start().block();
		final CountDownLatch startup = new CountDownLatch(1);
		awaitClose = connectionThreadPool.submit(() -> {
			startup.countDown();
			rsocket //
					.requestChannel(Flux.from(processor.map(event -> DefaultPayload.create(ColferObject.marshal(event.data), ColferObject.marshal(event.meta))))) //
					.map(payload -> {
						KvsEventMeta meta = new KvsEventMeta();
						meta.unmarshal(payload.getMetadata().array(), 0);
						Long id = meta.getId();
						Event<?> event = waiting.get(id);
						event.future.complete(payload.getData().array());
						return payload;
					}) //
					.then() //
					.block() //
			;
		});
		try {
			startup.await();
		} catch (InterruptedException ignore) {
		}
	}

	@SuppressWarnings("unchecked")
	<Key extends ColferObject, Value extends ColferObject> StoreImpl<Key, Value> getStore(String name, Class<Key> keyClass, Class<Value> valueClass) {
		return (StoreImpl<Key, Value>) stores.computeIfAbsent(name, _name -> {
			return new StoreImpl<>(this, _name, keyClass, valueClass);
		});
	}

	public <T> void send(Event<T> event) {
		processor.onNext(event);
	}

	@Override
	public void close() throws IOException {
		processor.onComplete();
		rsocket.dispose();
		try {
			awaitClose.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
}

class Event<T> {
	private static final AtomicLong ID_GENERATOR = new AtomicLong();
	public final Long id = ID_GENERATOR.incrementAndGet();
	public final KvsEventMeta meta;
	public final KvsEvent data;
	public final StoreFuture<T> future;

	public Event(KvsEventMeta meta, KvsEvent data, Deserializer<T> deserializer) {
		this.meta = meta;
		this.meta.id = this.id;
		this.data = data;
		this.future = new StoreFuture<>(deserializer);
	}
}

class StoreImpl<Key extends ColferObject, Value extends ColferObject> implements Store<Key, Value> {
	private final Class<Key> keyClass;
	private final Class<Value> valueClass;
	private final AtomicBoolean opened = new AtomicBoolean(false);
	private final int kvsId;

	StoreImpl(Connection connection, String name, Class<Key> keyClass, Class<Value> valueClass) {
		this.keyClass = keyClass;
		this.valueClass = valueClass;
		KvsEventMeta meta = new KvsEventMeta();
		meta.setEventType(EventType.OPEN);
		KvsEvent data = new KvsEvent();
		data.setName(name);
		Event<KvsEventResult> openEvent = new Event<>(meta, data, _data -> ColferObject._unmarshal(_data, StoreImpl::newResult));
		connection.send(openEvent);
		int kvsId = -1;
		try {
			KvsEventResult openEventResult = openEvent.future.getUninterruptibly();
			if (openEventResult.error.length() > 0) {
				System.err.println(openEventResult.error);
			} else {
				kvsId = openEventResult.kvs;
				opened.set(true);
			}
		} catch (StoreException e) {
			e.printStackTrace();
		}
		this.kvsId = kvsId;
	}

	Void nullDesirialize(byte[] data) {
		return null;
	}

	static KvsEventResult newResult() {
		return new KvsEventResult();
	}

	Key newKey() {
		return ColferObject.newInstance(keyClass);
	}

	Value newValue() {
		return ColferObject.newInstance(valueClass);
	}

	@Override
	public StoreFuture<Value> get(Key key) {
		if (!opened.get()) {
			Event<Value> errorResult = new Event<>(null, null, null);
			errorResult.future.completeExceptionally(new IllegalStateException("not opened."));
			return errorResult.future;
		}
		KvsEventMeta meta = new KvsEventMeta();
		meta.setEventType(EventType.GET);
		KvsEvent data = new KvsEvent();
		data.setKvs(kvsId);
		data.setKey(ColferObject.marshal(key));
		Event<Value> event = new Event<>(meta, data, _data -> {
			KvsEventResult result = ColferObject._unmarshal(_data, StoreImpl::newResult);
			if (result.getError().length() > 0) {
				throw new StoreException(result.getError());
			}
			Value value = newValue();
			value.unmarshal(result.getValue(), 0);
			return value;
		});
		return event.future;
	}

	@Override
	public StoreFuture<Void> put(Key key, Value value) {
		if (!opened.get()) {
			Event<Void> errorResult = new Event<>(null, null, null);
			errorResult.future.completeExceptionally(new IllegalStateException("not opened."));
			return errorResult.future;
		}
		KvsEventMeta meta = new KvsEventMeta();
		meta.setEventType(EventType.PUT);
		KvsEvent data = new KvsEvent();
		data.setKvs(kvsId);
		data.setKey(ColferObject.marshal(key));
		data.setValue(ColferObject.marshal(value));
		Event<Void> event = new Event<>(meta, data, _data -> {
			KvsEventResult result = ColferObject._unmarshal(_data, StoreImpl::newResult);
			if (result.getError().length() > 0) {
				throw new StoreException(result.getError());
			}
			return null;
		});
		return event.future;
	}

	@Override
	public StoreFuture<Boolean> putIfAbsent(Key key, Value value) {
		if (!opened.get()) {
			Event<Boolean> errorResult = new Event<>(null, null, null);
			errorResult.future.completeExceptionally(new IllegalStateException("not opened."));
			return errorResult.future;
		}
		KvsEventMeta meta = new KvsEventMeta();
		meta.setEventType(EventType.P_I_A);
		KvsEvent data = new KvsEvent();
		data.setKvs(kvsId);
		data.setKey(ColferObject.marshal(key));
		data.setValue(ColferObject.marshal(value));
		Event<Boolean> event = new Event<>(meta, data, _data -> {
			KvsEventResult result = ColferObject._unmarshal(_data, StoreImpl::newResult);
			if (result.getError().length() > 0) {
				throw new StoreException(result.getError());
			}
			return result.getResult();
		});
		return event.future;
	}

	@Override
	public StoreFuture<Void> remove(Key key) {
		if (!opened.get()) {
			Event<Void> errorResult = new Event<>(null, null, null);
			errorResult.future.completeExceptionally(new IllegalStateException("not opened."));
			return errorResult.future;
		}
		KvsEventMeta meta = new KvsEventMeta();
		meta.setEventType(EventType.REMOVE);
		KvsEvent data = new KvsEvent();
		data.setKvs(kvsId);
		data.setKey(ColferObject.marshal(key));
		Event<Void> event = new Event<>(meta, data, _data -> {
			KvsEventResult result = ColferObject._unmarshal(_data, StoreImpl::newResult);
			if (result.getError().length() > 0) {
				throw new StoreException(result.getError());
			}
			return null;
		});
		return event.future;
	}

	@Override
	public StoreFuture<Boolean> compareAndPut(Key key, Value value, Value expect) {
		if (!opened.get()) {
			Event<Boolean> errorResult = new Event<>(null, null, null);
			errorResult.future.completeExceptionally(new IllegalStateException("not opened."));
			return errorResult.future;
		}
		KvsEventMeta meta = new KvsEventMeta();
		meta.setEventType(EventType.C_A_P);
		KvsEvent data = new KvsEvent();
		data.setKvs(kvsId);
		data.setKey(ColferObject.marshal(key));
		data.setValue(ColferObject.marshal(value));
		data.setExpected(ColferObject.marshal(expect));
		Event<Boolean> event = new Event<>(meta, data, _data -> {
			KvsEventResult result = ColferObject._unmarshal(_data, StoreImpl::newResult);
			if (result.getError().length() > 0) {
				throw new StoreException(result.getError());
			}
			return result.getResult();
		});
		return event.future;
	}

	@Override
	public StoreFuture<Boolean> compareAndRemove(Key key, Value expect) {
		if (!opened.get()) {
			Event<Boolean> errorResult = new Event<>(null, null, null);
			errorResult.future.completeExceptionally(new IllegalStateException("not opened."));
			return errorResult.future;
		}
		KvsEventMeta meta = new KvsEventMeta();
		meta.setEventType(EventType.C_A_R);
		KvsEvent data = new KvsEvent();
		data.setKvs(kvsId);
		data.setKey(ColferObject.marshal(key));
		data.setExpected(ColferObject.marshal(expect));
		Event<Boolean> event = new Event<>(meta, data, _data -> {
			KvsEventResult result = ColferObject._unmarshal(_data, StoreImpl::newResult);
			if (result.getError().length() > 0) {
				throw new StoreException(result.getError());
			}
			return result.getResult();
		});
		return event.future;
	}

	@Override
	public StoreFuture<Void> write(StoreBatch<Key, Value> updates) {
		if (!opened.get()) {
			Event<Void> errorResult = new Event<>(null, null, null);
			errorResult.future.completeExceptionally(new IllegalStateException("not opened."));
			return errorResult.future;
		}
		KvsEventMeta meta = new KvsEventMeta();
		meta.setEventType(EventType.WRITE);
		KvsEvent data = new KvsEvent();
		data.setKvs(kvsId);
		data.setUpdates( //
				updates.entries().stream().map(entry -> {
					KvsEntry kvsEntry = new KvsEntry();
					kvsEntry.setKey(ColferObject.marshal(entry.getKey()));
					kvsEntry.setValue(ColferObject.marshal(entry.getValue()));
					return kvsEntry;
				}).collect(Collectors.toList()).toArray(new KvsEntry[updates.entries().size()]) //
		);
		Event<Void> event = new Event<>(meta, data, _data -> {
			KvsEventResult result = ColferObject._unmarshal(_data, StoreImpl::newResult);
			if (result.getError().length() > 0) {
				throw new StoreException(result.getError());
			}
			return null;
		});
		return event.future;
	}

	@Override
	public StoreFuture<List<StoreEntry<Key, Value>>> bulkGet(List<Key> keys) {
		if (!opened.get()) {
			Event<List<StoreEntry<Key, Value>>> errorResult = new Event<>(null, null, null);
			errorResult.future.completeExceptionally(new IllegalStateException("not opened."));
			return errorResult.future;
		}
		KvsEventMeta meta = new KvsEventMeta();
		meta.setEventType(EventType.WRITE);
		KvsEvent data = new KvsEvent();
		data.setKvs(kvsId);
		data.setKeys( //
				keys.stream().map(entry -> ColferObject.marshal(entry)).collect(Collectors.toList()).toArray(new byte[keys.size()][]) //
		);
		Event<List<StoreEntry<Key, Value>>> event = new Event<>(meta, data, _data -> {
			KvsEventResult result = ColferObject._unmarshal(_data, StoreImpl::newResult);
			if (result.getError().length() > 0) {
				throw new StoreException(result.getError());
			}
			return Arrays.asList(result.getEntries()).stream().map(kvsEntry -> {
				StoreEntry<Key, Value> entry = new StoreEntry<>();
				Key key = newKey();
				Value value = newValue();
				key.unmarshal(kvsEntry.getKey(), 0);
				value.unmarshal(kvsEntry.getValue(), 0);
				return entry;
			}).collect(Collectors.toList());
		});
		return event.future;
	}

}

class EventType {
	public static final byte OPEN = 1;
	public static final byte CLOSE = OPEN + 1;
	public static final byte GET = CLOSE + 1;
	public static final byte PUT = GET + 1;
	public static final byte REMOVE = PUT + 1;
	public static final byte P_I_A = REMOVE + 1;
	public static final byte C_A_P = P_I_A + 1;
	public static final byte C_A_R = C_A_P + 1;
	public static final byte WRITE = C_A_R + 1;
	public static final byte BULKGET = WRITE + 1;
}

interface Deserializer<R> {
	R deserialize(byte[] binary) throws StoreException;
}
