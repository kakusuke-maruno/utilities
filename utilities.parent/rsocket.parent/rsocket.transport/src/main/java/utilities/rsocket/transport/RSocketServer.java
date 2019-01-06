package utilities.rsocket.transport;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.reactivestreams.Publisher;

import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.TcpServerTransport;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class RSocketServer {
	public static class Builder {
		private int port = 7000;
		private int parallelizm = -1;
		private Function<Payload, Payload> execution = null;

		Builder() {
		}

		public Builder port(int port) {
			this.port = port;
			return this;
		}

		public Builder parallelizm(int parallelizm) {
			this.parallelizm = parallelizm;
			return this;
		}

		public Builder execution(Function<Payload, Payload> execution) {
			this.execution = execution;
			return this;
		}

		public RSocketServer build() {
			return new RSocketServer( //
					port, //
					parallelizm, //
					execution //
			);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	private final int port;
	private final int parallelizm;
	private final Function<Payload, Payload> execution;
	private final AtomicBoolean started = new AtomicBoolean(false);
	private final ExecutorService tp;
	private CloseableChannel channel;

	RSocketServer(int port, int parallelizm, Function<Payload, Payload> execution) {
		this.port = port;
		this.parallelizm = parallelizm;
		this.execution = execution;
		if (parallelizm > 1) {
			tp = Executors.newFixedThreadPool(parallelizm);
		} else {
			tp = null;
		}
	}

	public void start() {
		if (started.get())
			return;
		synchronized (started) {
			if (started.get())
				return;

			channel = RSocketFactory //
					.receive() //
					.acceptor((setupPayload, reactiveSocket) -> Mono.just(new AbstractRSocket() {
						@Override
						public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
							if (parallelizm > 1) {
								return Flux.from(payloads).parallel(parallelizm).runOn(Schedulers.fromExecutor(tp)).map(execution).sequential();
							} else {
								return Flux.from(payloads).map(execution);
							}
						}
					})) //
					.transport(TcpServerTransport.create(new InetSocketAddress("localhost", port))) //
					.start() //
					.block();

			started.set(true);
		}
	}

	public void stop() {
		if (!started.get())
			return;
		synchronized (started) {
			if (!started.get())
				return;
			channel.dispose();
			if (parallelizm > 1) {
				tp.shutdownNow();
			}
			started.set(false);
		}
	}
}
