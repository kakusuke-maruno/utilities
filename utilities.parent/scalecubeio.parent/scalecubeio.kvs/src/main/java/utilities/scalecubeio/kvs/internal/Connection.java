package utilities.scalecubeio.kvs.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import io.scalecube.cluster.Cluster;
import io.scalecube.cluster.Member;
import io.scalecube.transport.Address;
import io.scalecube.transport.Message;
import utilities.scalecubeio.kvs.Kvs;
import utilities.scalecubeio.kvs.KvsOperation;

public class Connection {
	private final Properties config;
	private final Cluster cluster;
	private final Member member;
	private final AtomicLong requestIdGenerator = new AtomicLong();
	private final ConcurrentMap<String, Kvs> kvsMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<Long, CompletableFuture<byte[]>> replyFutureMap = new ConcurrentHashMap<>();

	public Connection(Integer serverId, Properties config) {
		this.config = config;
		String serversStr = config.getProperty("servers");
		Address[] addresses = (Address[]) Arrays.asList(serversStr.split(",")).stream().map(serverStr -> Address.from(serverStr)).collect(Collectors.toList()).toArray();
		cluster = Cluster.join(addresses).block();
		String serveIdStr = serverId.toString();
		member = cluster.members().stream().filter(m -> serveIdStr.equals(m.metadata().get("serverId"))).findFirst().get();
		cluster.listen().filter(m -> m.headers().containsKey("requestId")).subscribe(this::receiveOperationReply, e -> {
		}, () -> {
		});
	}

	public Kvs getKvs(String name) {
		return kvsMap.computeIfAbsent(name, this::getKvs0);
	}

	private Kvs getKvs0(String name) {
		try {
			return new KvsImpl(this, name, config);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	void openKvs(String name, Properties config) throws IOException {
		try {
			sendOpenRequest(name, config).get();
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e.getCause());
		}
	}

	private void receiveOperationReply(Message message) {
		Long requestId = Long.valueOf(message.header("requestId"));
		CompletableFuture<byte[]> replyFuture = replyFutureMap.remove(requestId);
		if (replyFuture != null && !replyFuture.isDone()) {
			if (message.headers().containsKey("error")) {
				replyFuture.completeExceptionally(new Exception(message.header("error")));
			} else {
				replyFuture.complete(message.data());
			}
		}
	}

	public Future<byte[]> sendOperationRequest(KvsOperation operation) {
		CompletableFuture<byte[]> replyFuture = new CompletableFuture<>();
		Long reauestId = requestIdGenerator.incrementAndGet();
		String requestIdStr = String.valueOf(reauestId);
		operation.setRequestId(requestIdStr);
		replyFutureMap.put(reauestId, replyFuture);
		cluster.send(member, operation.toMessage()).block();
		return replyFuture;
	}

	public Future<byte[]> sendOpenRequest(String name, Properties config) {
		CompletableFuture<byte[]> replyFuture = new CompletableFuture<>();
		Long reauestId = requestIdGenerator.incrementAndGet();
		String requestIdStr = String.valueOf(reauestId);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			config.store(bos, "");
			bos.close();
		} catch (IOException e) {
			replyFuture.completeExceptionally(e);
			return replyFuture;
		}
		replyFutureMap.put(reauestId, replyFuture);
		cluster.send(member, Message.builder().header("name", name).header("open", "open").header("requestId", "" + requestIdStr).data(bos.toByteArray()).build()).block();
		return replyFuture;
	}
}
