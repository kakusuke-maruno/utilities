package utilities.rsocket.kvs.storage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.reactivestreams.Publisher;

import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import utilities.rsocket.kvs.ColferObject;
import utilities.rsocket.kvs.storage.exception.StorageException;
import utilities.rsocket.kvs.storage.spi.StorageProvider;
import utilities.rsocket.kvs.transport.KvsEntry;
import utilities.rsocket.kvs.transport.KvsEvent;
import utilities.rsocket.kvs.transport.KvsEventMeta;
import utilities.rsocket.kvs.transport.KvsEventResult;

public class StorageManager {
	private static final AtomicInteger THREAD_NO_GENERATOR = new AtomicInteger();
	private static final ThreadGroup THREAD_GROUP = new ThreadGroup("StorageManager.Server");
	private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(THREAD_GROUP, r, "StorageManager.Server#" + THREAD_NO_GENERATOR.incrementAndGet());
		}
	};
	private final ExecutorService serverThreadPool = Executors.newSingleThreadExecutor(THREAD_FACTORY);
	private final Future<?> awaitClose;
	private final StorageProvider provider;
	private final AtomicInteger storageIdGenerator = new AtomicInteger();
	private final ConcurrentMap<String, Integer> storageNames = new ConcurrentHashMap<>();
	private final ConcurrentMap<Integer, Storage> storages = new ConcurrentHashMap<>();

	public StorageManager(int port, String providerName) {
		final Map<String, StorageProvider> providers = new HashMap<>();
		ServiceLoader.load(StorageProvider.class).forEach(_provider -> providers.put(_provider.providerName(), _provider));
		if (providerName == null && providers.size() == 1) {
			provider = providers.values().stream().findFirst().get();
		} else {
			provider = providers.get(providerName);
		}
		if (provider == null) {
			throw new IllegalStateException("provider is not found.");
		}
		final CountDownLatch latch = new CountDownLatch(1);
		awaitClose = serverThreadPool.submit(() -> {
			latch.countDown();
			RSocketFactory //
					.receive() //
					.acceptor((setupPayload, reactiveSocket) -> Mono.just(new AbstractRSocket() {
						@Override
						public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
							return new Server() {
								@Override
								Flux<ResponseMessage> request(Flux<RequestMessage> stream) {
									return stream.parallel().runOn(Schedulers.elastic()).map(request -> {
										KvsEvent event = request.data;
										KvsEventResult data = new KvsEventResult();
										try {
											switch (request.meta.eventType) {
											case EventType.OPEN: {
												if (storageNames.containsKey(event.name)) {
													data.kvs = storageNames.get(event.name);
												} else {
													final Storage storage = provider.provide();
													storage.open(event.name, event.properties);
													data.kvs = storageIdGenerator.incrementAndGet();
													storageNames.put(event.name, data.kvs);
													storages.put(data.kvs, storage);
												}
												break;
											}
											case EventType.GET: {
												final Storage storage = storages.get(event.kvs);
												if (storage == null)
													throw new StorageException(new NullPointerException("kvs id:[" + event.kvs + "] is not found."));
												data.value = storage.get(event.key);
												break;
											}
											case EventType.PUT: {
												final Storage storage = storages.get(event.kvs);
												if (storage == null)
													throw new StorageException(new NullPointerException("kvs id:[" + event.kvs + "] is not found."));
												storage.put(event.key, event.value);
												break;
											}
											case EventType.P_I_A: {
												final Storage storage = storages.get(event.kvs);
												if (storage == null)
													throw new StorageException(new NullPointerException("kvs id:[" + event.kvs + "] is not found."));
												data.result = storage.putIfAbsent(event.key, event.value);
												break;
											}
											case EventType.REMOVE: {
												final Storage storage = storages.get(event.kvs);
												if (storage == null)
													throw new StorageException(new NullPointerException("kvs id:[" + event.kvs + "] is not found."));
												storage.remove(event.key);
												break;
											}
											case EventType.C_A_P: {
												final Storage storage = storages.get(event.kvs);
												if (storage == null)
													throw new StorageException(new NullPointerException("kvs id:[" + event.kvs + "] is not found."));
												data.result = storage.compareAndPut(event.key, event.value, event.expected);
												break;
											}
											case EventType.C_A_R: {
												final Storage storage = storages.get(event.kvs);
												if (storage == null)
													throw new StorageException(new NullPointerException("kvs id:[" + event.kvs + "] is not found."));
												data.result = storage.compareAndRemove(event.key, event.expected);
												break;
											}
											case EventType.WRITE: {
												final Storage storage = storages.get(event.kvs);
												if (storage == null)
													throw new StorageException(new NullPointerException("kvs id:[" + event.kvs + "] is not found."));
												StorageBatch batch = new StorageBatch();
												batch.updates.addAll(Arrays.asList(event.updates).stream().map(entry -> new StorageEntry(entry.key, entry.value)).collect(Collectors.toList()));
												storage.write(batch);
												break;
											}
											case EventType.BULKGET: {
												final Storage storage = storages.get(event.kvs);
												if (storage == null)
													throw new StorageException(new NullPointerException("kvs id:[" + event.kvs + "] is not found."));
												List<byte[]> keys = Arrays.asList(event.keys);
												List<KvsEntry> entries = storage.bulkGet(keys).stream().map(entry -> {
													KvsEntry kvsEntry = new KvsEntry();
													kvsEntry.key = entry.key;
													kvsEntry.value = entry.value;
													return kvsEntry;
												}).collect(Collectors.toList());
												data.entries = entries.toArray(new KvsEntry[entries.size()]);
												break;
											}
											case EventType.CLOSE: {
												final Storage storage = storages.get(event.kvs);
												if (storage == null)
													throw new StorageException(new NullPointerException("kvs id:[" + event.kvs + "] is not found."));
												try {
													storage.close();
												} catch (IOException e) {
													throw new StorageException(e);
												}
												break;
											}
											default:
												break;
											}
										} catch (StorageException e) {
											data.error = e.getMessage();
										}
										return new ResponseMessage(data, request.meta);
									}).sequential();
								}
							}.request(Flux.from(payloads).map(payload -> new RequestMessage(payload))).map(response -> response.payload());
						}
					})) //
					.transport(TcpServerTransport.create(new InetSocketAddress("localhost", port))) //
					.start() //
					.block() //
					.onClose() //
					.block() //
			;
		});
		try {
			latch.await();
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	public void shutdown() {
		try {
			awaitClose.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
}

abstract class Server {
	abstract Flux<ResponseMessage> request(Flux<RequestMessage> stream);
}

class RequestMessage {
	final KvsEvent data;
	final KvsEventMeta meta;

	public RequestMessage(Payload payload) {
		this.data = ColferObject._unmarshal(payload.getData().array(), () -> new KvsEvent());
		this.meta = ColferObject._unmarshal(payload.getMetadata().array(), () -> new KvsEventMeta());
	}
}

class ResponseMessage {
	private KvsEventResult data;
	private KvsEventMeta meta;

	public ResponseMessage(KvsEventResult data, KvsEventMeta meta) {
		this.data = data;
		this.meta = meta;
	}

	Payload payload() {
		return DefaultPayload.create(ColferObject.marshal(data), ColferObject.marshal(meta));
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
