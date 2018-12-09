package utilities.scalecubeio.kvs.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import io.scalecube.cluster.Cluster;
import io.scalecube.cluster.ClusterConfig;
import io.scalecube.transport.Address;
import io.scalecube.transport.Message;
import reactor.core.publisher.Flux;
import utilities.scalecubeio.kvs.Kvs;
import utilities.scalecubeio.kvs.KvsOperation;
import utilities.scalecubeio.kvs.spi.KvsProvider;

public class KvsServer {

	public static void main(String[] args) {

	}

	private final Cluster cluster;
	private final Integer serverId;
	private final Properties serverConfig;
	private final ConcurrentMap<String, KvsProvider> kvsProviderMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, Kvs> kvsMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, Flux<KvsOperation>> fluxMap = new ConcurrentHashMap<>();
	private static final byte[] NO_DATA = { (byte) 0 };
	private static final byte[] TRUE = { (byte) 1 };
	private static final byte[] FALSE = { (byte) 0 };

	private KvsServer(Integer serverId, Properties serverConfig) {
		this.serverId = serverId;
		this.serverConfig = serverConfig;
		String serversStr = this.serverConfig.getProperty("servers");
		List<Address> list = Arrays.asList(serversStr.split(",")).stream().map(seedStr -> Address.from(seedStr)).collect(Collectors.toList());
		list.remove(serverId.intValue());
		if (list.size() > 0) {
			ClusterConfig clusterConfig = ClusterConfig.builder() //
					.addMetadata("serverId", serverId.toString()) //
					.seedMembers(list) //
					.build();
			cluster = Cluster.join(clusterConfig).block();
		} else {
			ClusterConfig clusterConfig = ClusterConfig.builder() //
					.addMetadata("serverId", serverId.toString()) //
					.build();
			cluster = Cluster.join(clusterConfig).block();
		}
		cluster.listen().filter(m -> m.headers().containsKey("open")).subscribe(this::receiveOpenMessage, t -> {
		});
		cluster.listen().filter(m -> !m.headers().containsKey("open")).subscribe(this::receiveOperationMessage, t -> {
		});
		final AtomicReference<KvsProvider> defaultProvider = new AtomicReference<KvsProvider>(null);
		ServiceLoader.load(KvsProvider.class).forEach(provider -> {
			kvsProviderMap.put(provider.providerName(), provider);
			defaultProvider.set(provider);
		});
		if (serverConfig.containsKey("kvs.provider.default")) {
			String def = serverConfig.getProperty("kvs.provider.default");
			kvsProviderMap.put("default", kvsProviderMap.get(def));
		} else {
			if (kvsProviderMap.size() == 1) {
				kvsProviderMap.put("default", defaultProvider.get());
			}
		}
	}

	private void receiveOpenMessage(Message message) {
		if (kvsMap.containsKey(message.header("name"))) {
			sendReplyMessage(message.sender(), Message.withHeaders(message.headers()).data(NO_DATA).build());
			return;
		}
		synchronized (kvsMap) {
			Properties config = new Properties();
			try (ByteArrayInputStream bis = new ByteArrayInputStream(message.data())) {
				config.load(bis);
			} catch (IOException e) {
				sendReplyMessage(message.sender(), Message.withHeaders(message.headers()).header("error", e.getMessage()).data(NO_DATA).build());
			}
			// TODO open kvs, create event bus by flux.
		}
	}

	private void receiveOperationMessage(Message message) {
		// TODO fire event on event bus.
	}

	public void sendReplyMessage(Address replyTo, Message message) {
		cluster.send(replyTo, message);
	}
}
