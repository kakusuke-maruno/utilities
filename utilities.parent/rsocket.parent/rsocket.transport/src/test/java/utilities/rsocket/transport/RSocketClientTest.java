package utilities.rsocket.transport;

import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.rsocket.util.DefaultPayload;

public class RSocketClientTest {
	private static final int SERVERS = 8;
	private static final int COUNT = 10_000;
	final AtomicReferenceArray<RSocketServer> serverRef = new AtomicReferenceArray<>(SERVERS);

	@Before
	public void setUp() throws Exception {
		for (int i = 0; i < SERVERS; i++) {
			serverRef.set(i, new RSocketServer(7000 + i, 1, request -> {
				byte[] data = request.getData().array();
				byte[] metadata = request.getMetadata().array();
				return DefaultPayload.create(data, metadata);
			}));
			serverRef.get(i).start();
		}
	}

	@After
	public void tearDown() throws Exception {
		for (int i = 0; i < SERVERS; i++) {
			serverRef.get(i).stop();
			serverRef.set(i, null);
		}
	}

	@Test
	public void test() {
		RSocketClient[] clients = new RSocketClient[SERVERS];
		CountDownLatch latch = new CountDownLatch(SERVERS);
		final AtomicLong total = new AtomicLong();
		Thread[] th = new Thread[SERVERS];
		for (int i = 0; i < SERVERS; i++) {
			final int server = i;
			clients[i] = new RSocketClient(new InetSocketAddress("localhost", 7000 + i));
			th[i] = new Thread(() -> {
				List<Future<byte[]>> list = new ArrayList<>();
				for (int j = 0; j < COUNT; j++) {
					list.add(clients[server].send("hello".getBytes()));
				}
				for (Future<byte[]> future : list) {
					try {
						future.get();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
				total.addAndGet(list.size());
				latch.countDown();
			});
		}
		long start = System.currentTimeMillis();
		for (int i = 0; i < SERVERS; i++) {
			th[i].start();
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		for (int i = 0; i < SERVERS; i++) {
			clients[i].close();
		}
		assertTrue(String.format("this must be [%d]!!", SERVERS * COUNT), total.get() == SERVERS * COUNT);
		System.out.format("elapsed:[%d], count:[%,d]\n", end - start, total.get());
	}

}
