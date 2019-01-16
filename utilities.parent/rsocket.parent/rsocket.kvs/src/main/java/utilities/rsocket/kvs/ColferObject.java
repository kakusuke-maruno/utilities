package utilities.rsocket.kvs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class ColferObject {
	public abstract byte[] marshal(OutputStream out, byte[] buf) throws IOException;

	public abstract int marshal(byte[] buf, int offset);

	public abstract int unmarshal(byte[] buf, int offset);

	public abstract int unmarshal(byte[] buf, int offset, int end);

	public static final ConcurrentMap<Class<?>, Constructor<?>> constructors = new ConcurrentHashMap<>();

	@SuppressWarnings("unchecked")
	public static <X extends ColferObject> X newInstance(Class<X> clazz) {
		try {
			return (X) constructors.computeIfAbsent(clazz, clazzz -> {
				try {
					return clazzz.getConstructor();
				} catch (Exception e) {
					return clazzz.getConstructors()[0];
				}
			}).newInstance();
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		} catch (IllegalArgumentException e) {
			return null;
		} catch (InvocationTargetException e) {
			return null;
		}
	}

	public static byte[] marshal(ColferObject object) {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			object.marshal(bos, null);
			return bos.toByteArray();
		} catch (IOException e) {
			return null;
		}
	}

	public static <T extends ColferObject> T _unmarshal(byte[] data, Callable<T> instanceFactory) {
		try {
			T result = instanceFactory.call();
			result.unmarshal(data, 0);
			return result;
		} catch (Exception ignore) {
		}
		return null;
	}
}
