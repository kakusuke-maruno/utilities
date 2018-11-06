package utilities.rxjava2.window;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import io.reactivex.Flowable;

public class PartitionWindowFlowableTest {

	@Test
	public void test001_normal() {
		int count = 101;
		int partitionCount = 10;
		int bufferSize = 10;
		AtomicLong counter_1 = new AtomicLong();
		AtomicLong counter_2 = new AtomicLong();
		Flowable //
				.range(1, count) //
				.to(PartitionWindowFlowable.of(value -> value % partitionCount, partitionCount, bufferSize)) //
				.doOnNext(window -> {
					counter_1.incrementAndGet();
					counter_2.addAndGet(window.windowValues.size());
				}) //
				.subscribe() //
		;
		assertTrue("partitioned keys size is failed. " + 11 + ":" + counter_1.get(), 11 == counter_1.get());
		assertTrue("partitioned values size is failed. " + count + ":" + counter_2.get(), count == counter_2.get());
	}

	@Test
	public void test002_cancel() {
		int count = 101;
		int partitionCount = 10;
		int bufferSize = 10;
		AtomicLong counter_1 = new AtomicLong();
		AtomicLong counter_2 = new AtomicLong();
		Flowable //
				.range(1, count) //
				.to(PartitionWindowFlowable.of(value -> value % partitionCount, partitionCount, bufferSize)) //
				.doOnNext(window -> {
					counter_1.incrementAndGet();
					counter_2.addAndGet(window.windowValues.size());
				}) //
				.subscribe(window -> {
					if (window.partition == 5) {
						throw new Exception("error raise!");
					}
				}, t -> {
				}) //
		;
		assertTrue("partitioned keys size is failed. " + 5 + ":" + counter_1.get(), 5 == counter_1.get());
		assertTrue("partitioned values size is failed. " + 50 + ":" + counter_2.get(), 50 == counter_2.get());
	}

	@Test
	public void test003_error() {
		int count = 101;
		int partitionCount = 10;
		int bufferSize = 10;
		AtomicLong counter_1 = new AtomicLong();
		AtomicLong counter_2 = new AtomicLong();
		Flowable //
				.range(1, count) //
				.to(PartitionWindowFlowable.of(value -> value == 101 ? value % 0 : value % partitionCount, partitionCount, bufferSize)) //
				.doOnNext(window -> {
					counter_1.incrementAndGet();
					counter_2.addAndGet(window.windowValues.size());
				}) //
				.subscribe(window -> {
				}, t -> {
				}) //
		;
		assertTrue("partitioned keys size is failed. " + 10 + ":" + counter_1.get(), 10 == counter_1.get());
		assertTrue("partitioned values size is failed. " + 100 + ":" + counter_2.get(), 100 == counter_2.get());
	}

}
