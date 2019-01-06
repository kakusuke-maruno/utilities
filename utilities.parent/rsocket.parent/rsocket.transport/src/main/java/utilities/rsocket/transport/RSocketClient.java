package utilities.rsocket.transport;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.internal.UnboundedProcessor;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;

public class RSocketClient {
	private final RSocket rsocket;
	private final AtomicLong seq = new AtomicLong();
	private final ConcurrentMap<Long, FutureHolder> waiting = new ConcurrentHashMap<>();
	private final UnboundedProcessor<FutureHolder> processor = new UnboundedProcessor<>();

	public RSocketClient(InetSocketAddress address) {
		rsocket = RSocketFactory.connect().transport(TcpClientTransport.create(address)).start().block();

		final CountDownLatch startup = new CountDownLatch(1);
		new Thread(() -> {
			startup.countDown();
			try {
				rsocket.requestChannel(processor.map(holder -> {
					waiting.put(holder.id, holder);
					int pos = 0;
					byte[] meta = new byte[Long.BYTES + (holder.meta != null ? holder.meta.length : 0)];
					Bits.putLong(meta, pos, holder.id.longValue());
					pos += Long.BYTES;
					if (holder.meta != null && holder.meta.length > 0) {
						System.arraycopy(holder.meta, 0, meta, pos, holder.meta.length);
					}
					return DefaultPayload.create(holder.msg, meta);
				})).map(payload -> {
					byte[] meta = payload.getMetadata().array();
					Long id = Bits.getLong(meta, 0);
					byte[] data = payload.getData().array();
					FutureHolder holder = waiting.remove(id);
					if (holder != null && !holder.future.isDone()) {
						holder.future.complete(data);
					}
					return payload;
				}).then().block();
			} catch (RuntimeException ignore) {
			}
		}).start();
		try {
			startup.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public Future<byte[]> send(byte[] message) {
		return send(null, message);
	}

	public Future<byte[]> send(byte[] meta, byte[] message) {
		FutureHolder holder = new FutureHolder(seq.incrementAndGet(), meta, message);
		synchronized (processor) {
			processor.onNext(holder);
		}
		return holder.future;
	}

	public void close() {
		while (waiting.size() > 0) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			rsocket.dispose();
		} catch (Exception e) {
		}
		while (!rsocket.isDisposed()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static class FutureHolder {
		Long id;
		CompletableFuture<byte[]> future;
		byte[] meta;
		byte[] msg;

		public FutureHolder(Long id, byte[] meta, byte[] msg) {
			this.id = id;
			this.future = new CompletableFuture<>();
			this.meta = meta;
			this.msg = msg;
		}
	}
}
