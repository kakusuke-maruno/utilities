package utilities.java8.collection;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;

public class MergeIterator<T> implements Iterator<T>, Iterable<T> {
	private Queue<Node<T>> q;
	private Iterator<T>[] arrayIterators;
	private final Comparator<T> comparator;

	@SafeVarargs
	public MergeIterator(Comparator<T> comparator, Iterator<T>... iterators) {
		assert (iterators != null && iterators.length > 0);
		this.arrayIterators = iterators;
		this.comparator = comparator;
		q = new PriorityQueue<>(iterators.length);
		int count = 0;
		for (Iterator<T> it : iterators) {
			if (it != null && it.hasNext()) {
				q.offer(new Node<>(comparator, it.next(), count));
			}
			count++;
		}
	}

	@Override
	public boolean hasNext() {
		return !q.isEmpty();
	}

	@Override
	public T next() {
		if (q.isEmpty()) {
			throw new NoSuchElementException("No more element...");
		}
		Node<T> n = q.poll();
		T data = n.getData();
		int index = n.getIndex();
		Iterator<T> it = arrayIterators[index];
		if (it != null && it.hasNext()) {
			q.offer(new Node<>(comparator, it.next(), index));
		}
		return data;
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}

	private class Node<E> implements Comparable<Node<E>> {
		private E data;
		private int index;
		private final Comparator<E> comparator;

		private Node(Comparator<E> comparator, E data, int index) {
			this.data = data;
			this.index = index;
			this.comparator = comparator;
		}

		public E getData() {
			return data;
		}

		public int getIndex() {
			return index;
		}

		@Override
		public int compareTo(Node<E> tNode) {
			if (tNode.data == null && this.data == null) {
				return 0;
			} else {
				return comparator.compare(this.data, tNode.data);
			}
		}
	}
}