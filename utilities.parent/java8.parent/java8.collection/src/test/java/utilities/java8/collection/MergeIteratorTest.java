package utilities.java8.collection;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class MergeIteratorTest {

	@SuppressWarnings("unchecked")
	@Test
	public void test() {
		Iterator<String>[] iterators = new Iterator[10];
		for (int i = 0; i < 10; i++) {
			String[] s = new String[10];
			for (int j = 0; j < 10; j++) {
				s[j] = "j" + j + "i" + (9 - i);
			}
			iterators[9 - i] = Arrays.asList(s).iterator();
		}
		MergeIterator<String> ite = new MergeIterator<>(COMPARATOR, iterators);
		String last = null;
		for (String current : ite) {
			if (last != null) {
				assertTrue("not sorted..", last.compareTo(current) < 0);
			}
			last = current;
		}
	}

	private static final Comparator<String> COMPARATOR = new Comparator<String>() {

		@Override
		public int compare(String o1, String o2) {
			return o1.compareTo(o2);
		}
	};

}
