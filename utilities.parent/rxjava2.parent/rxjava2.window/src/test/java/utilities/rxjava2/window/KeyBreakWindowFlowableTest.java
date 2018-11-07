package utilities.rxjava2.window;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import io.reactivex.Flowable;

public class KeyBreakWindowFlowableTest {

	@Test
	public void test001_normal() {
		int count = 101;
		int divide = 10;
		AtomicLong counter_1 = new AtomicLong();
		AtomicLong counter_2 = new AtomicLong();
		Flowable //
				.range(1, count) //
				.to(KeyBreakWindowFlowable.of(value -> (value - 1) / divide)) //
				.doOnNext(window -> {
					counter_1.incrementAndGet();
					counter_2.addAndGet(window.values.size());
				}) //
				.subscribe() //
		;
		assertTrue("grouped keys size is failed. " + 11 + ":" + counter_1.get(), 11 == counter_1.get());
		assertTrue("grouped values size is failed. " + count + ":" + counter_2.get(), count == counter_2.get());
	}

	@Test
	public void test002_cancel() {
		int count = 100;
		int divide = 10;
		AtomicLong counter_1 = new AtomicLong();
		AtomicLong counter_2 = new AtomicLong();
		AtomicLong counter_3 = new AtomicLong();
		Flowable //
				.range(1, count) //
				.to(KeyBreakWindowFlowable.of(value -> (value - 1) / divide)) //
				.doOnNext(window -> {
					counter_1.incrementAndGet();
					counter_2.addAndGet(window.values.size());
				}) //
				.subscribe(window -> {
					if (window.key == 5) {
						throw new Exception();
					}
				}, t -> {
					counter_3.incrementAndGet();
				}) //
		;
		assertTrue("grouped keys size is failed. " + 6 + ":" + counter_1.get(), 6 == counter_1.get());
		assertTrue("grouped values size is failed. " + 60 + ":" + counter_2.get(), 60 == counter_2.get());
		assertTrue("error count is failed. " + 1 + ":" + counter_3.get(), 1 == counter_3.get());
	}

	@Test
	public void test003_error() {
		int count = 100;
		int divide = 10;
		AtomicLong counter_1 = new AtomicLong();
		AtomicLong counter_2 = new AtomicLong();
		AtomicLong counter_3 = new AtomicLong();
		Flowable //
				.range(1, count) //
				.to(KeyBreakWindowFlowable.of(value -> value == 71 ? value / 0 : (value - 1) / divide)) //
				.doOnNext(window -> {
					counter_1.incrementAndGet();
					counter_2.addAndGet(window.values.size());
				}) //
				.subscribe(window -> {
				}, t -> {
					counter_3.incrementAndGet();
				}) //
		;
		assertTrue("grouped keys size is failed. " + 6 + ":" + counter_1.get(), 6 == counter_1.get());
		assertTrue("grouped values size is failed. " + 60 + ":" + counter_2.get(), 60 == counter_2.get());
		assertTrue("error count is failed. " + 1 + ":" + counter_3.get(), 1 == counter_3.get());
	}

}
