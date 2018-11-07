package utilities.rxjava2.flow.file;

import java.util.concurrent.Callable;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import io.reactivex.Flowable;
import utilities.rxjava2.flow.AbstractSubscriberWrapper;

public class FileParseFlowable<T, Meta> extends Flowable<T> {
	final Publisher<FileInfo<Meta>> source;
	final Callable<FileParser<T, Meta>> fileParserFactory;

	public FileParseFlowable(Publisher<FileInfo<Meta>> source, Callable<FileParser<T, Meta>> fileParserFactory) {
		this.source = source;
		this.fileParserFactory = fileParserFactory;
	}

	@Override
	protected void subscribeActual(Subscriber<? super T> s) {
		source.subscribe(new SubscriberWrapper<>(s, fileParserFactory));
	}

	static class SubscriberWrapper<T, Meta> extends AbstractSubscriberWrapper<FileInfo<Meta>, T> {
		final Callable<FileParser<T, Meta>> fileParserFactory;

		protected SubscriberWrapper(Subscriber<? super T> downstream, Callable<FileParser<T, Meta>> fileParserFactory) {
			super(downstream);
			this.fileParserFactory = fileParserFactory;
		}

		@Override
		protected void onNext0(FileInfo<Meta> fileInfo) throws Exception {
			FileParser<T, Meta> fileParser = fileParserFactory.call();
			fileParser.initialize(fileInfo);
			T t;
			while ((t = fileParser.next()) != null) {
				downstream.onNext(t);
			}
			fileParser.terminate(fileInfo);
		}

	}
}
