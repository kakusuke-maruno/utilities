package utilities.java8.serialization;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.KryoSerializableSerializer;

import utilities.java8.serialization.sample.Address;
import utilities.java8.serialization.sample.Person;

public class ColferObjectTest {
	static {
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
		System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
		System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "yyyy-MM-dd HH:mm:ss:SSS Z");
	}
	private static final Logger LOG = LoggerFactory.getLogger(ColferObjectTest.class);

	private static final Kryo kryo = new Kryo();
	private static final KryoSerializableSerializer serializer = new KryoSerializableSerializer();

	@Test
	public void test_001() {
		Address addressSerialize = new Address();
		addressSerialize.street = "あああああ";
		addressSerialize.zipcode = "123-4567";
		LOG.debug("addressSerialize.street:{}", addressSerialize.street);
		LOG.debug("addressSerialize.zipcode:{}", addressSerialize.zipcode);
		byte[] bytes = addressSerialize.marshal();
		LOG.debug("bytes len:{}", bytes.length);
		Address addressDeserialize = new Address();
		LOG.debug("addressDeserialize(before).street:{}", addressDeserialize.street);
		LOG.debug("addressDeserialize(before).zipcode:{}", addressDeserialize.zipcode);
		addressDeserialize.unmarshal(bytes);
		LOG.debug("addressDeserialize(after).street:{}", addressDeserialize.street);
		LOG.debug("addressDeserialize(after).zipcode:{}", addressDeserialize.zipcode);
		LOG.debug("binary:{}", BinaryUtility.toHexString(bytes));
		int[] i_expect = { 0x00, 0x08, 0x31, 0x32, 0x33, 0x2d, 0x34, 0x35, 0x36, 0x37, 0x01, 0x0f, 0xe3, 0x81, 0x82, 0xe3, 0x81, 0x82, 0xe3, 0x81, 0x82, 0xe3, 0x81, 0x82, 0xe3, 0x81, 0x82, 0x7f };
		byte[] expect = new byte[i_expect.length];
		for (int i = 0; i < i_expect.length; i++) {
			expect[i] = (byte) i_expect[i];
		}

		assertTrue(Arrays.equals(expect, bytes));
	}

	@Test
	public void test_002() {
		Address addressSerialize = new Address();
		addressSerialize.street = "あああああ";
		addressSerialize.zipcode = "123-4567";
		LOG.debug("addressSerialize.street:{}", addressSerialize.street);
		LOG.debug("addressSerialize.zipcode:{}", addressSerialize.zipcode);
		Output output = new Output(30);
		kryo.writeObject(output, addressSerialize, serializer);
		byte[] bytes = output.toBytes();
		LOG.debug("bytes len:{}", bytes.length);
		Input input = new Input(bytes);
		Address addressDeserialize = new Address();
		LOG.debug("addressDeserialize(before).street:{}", addressDeserialize.street);
		LOG.debug("addressDeserialize(before).zipcode:{}", addressDeserialize.zipcode);
		addressDeserialize = kryo.readObject(input, Address.class, serializer);
		LOG.debug("addressDeserialize(after).street:{}", addressDeserialize.street);
		LOG.debug("addressDeserialize(after).zipcode:{}", addressDeserialize.zipcode);
		LOG.debug("binary:{}", BinaryUtility.toHexString(bytes));
		int[] i_expect = { 0x01, 0x00, 0x08, 0x31, 0x32, 0x33, 0x2d, 0x34, 0x35, 0x36, 0x37, 0x01, 0x0f, 0xe3, 0x81, 0x82, 0xe3, 0x81, 0x82, 0xe3, 0x81, 0x82, 0xe3, 0x81, 0x82, 0xe3, 0x81, 0x82, 0x7f };
		byte[] expect = new byte[i_expect.length];
		for (int i = 0; i < i_expect.length; i++) {
			expect[i] = (byte) i_expect[i];
		}

		assertTrue(Arrays.equals(expect, bytes));
	}

	@Test
	public void test_003() {
		Address addressSerialize = new Address();
		addressSerialize.street = "あああああ";
		addressSerialize.zipcode = "123-4567";
		Person personSerialize = new Person();
		personSerialize.age = 1;
		personSerialize.name = "aaaaa";
		personSerialize.sex = true;
		personSerialize.address = new Address[] { addressSerialize };
		LOG.debug("personSerialize.age:{}", personSerialize.age);
		LOG.debug("personSerialize.name:{}", personSerialize.name);
		LOG.debug("personSerialize.sex:{}", personSerialize.sex);
		LOG.debug("personSerialize.address[0].street:{}", personSerialize.address[0].street);
		LOG.debug("personSerialize.address[0].zipcode:{}", personSerialize.address[0].zipcode);
		byte[] bytes = personSerialize.marshal();
		LOG.debug("bytes len:{}", bytes.length);
		Person personDeserialize = new Person();
		LOG.debug("personDeserialize(before).age:{}", personDeserialize.age);
		LOG.debug("personDeserialize(before).name:{}", personDeserialize.name);
		LOG.debug("personDeserialize(before).sex:{}", personDeserialize.sex);
		LOG.debug("personDeserialize(before).address.length:{}", personDeserialize.address.length);
		personDeserialize.unmarshal(bytes);
		LOG.debug("personDeserialize(after).age:{}", personDeserialize.age);
		LOG.debug("personDeserialize(after).name:{}", personDeserialize.name);
		LOG.debug("personDeserialize(after).sex:{}", personDeserialize.sex);
		LOG.debug("personDeserialize(after).address.length:{}", personDeserialize.address.length);
		LOG.debug("personDeserialize(after).address[0].street:{}", personDeserialize.address[0].street);
		LOG.debug("personDeserialize(after).address[0].zipcode:{}", personDeserialize.address[0].zipcode);
		LOG.debug("binary:{}", BinaryUtility.toHexString(bytes));
		//		int[] i_expect = { 0x00, 0x08, 0x31, 0x32, 0x33, 0x2d, 0x34, 0x35, 0x36, 0x37, 0x01, 0x0f, 0xe3, 0x81, 0x82, 0xe3, 0x81, 0x82, 0xe3, 0x81, 0x82, 0xe3, 0x81, 0x82, 0xe3, 0x81, 0x82, 0x7f };
		//		byte[] expect = new byte[i_expect.length];
		//		for (int i = 0; i < i_expect.length; i++) {
		//			expect[i] = (byte) i_expect[i];
		//		}
		//
		//		assertTrue(Arrays.equals(expect, bytes));
	}

	@Test
	public void test_004() {
		Address addressSerialize = new Address();
		addressSerialize.street = "あああああ";
		addressSerialize.zipcode = "123-4567";
		Person personSerialize = new Person();
		personSerialize.age = 1;
		personSerialize.name = "aaaaa";
		personSerialize.sex = true;
		personSerialize.address = new Address[] { addressSerialize };
		LOG.debug("personSerialize.age:{}", personSerialize.age);
		LOG.debug("personSerialize.name:{}", personSerialize.name);
		LOG.debug("personSerialize.sex:{}", personSerialize.sex);
		LOG.debug("personSerialize.address[0].street:{}", personSerialize.address[0].street);
		LOG.debug("personSerialize.address[0].zipcode:{}", personSerialize.address[0].zipcode);
		Output output = new Output(100);
		kryo.writeObject(output, personSerialize, serializer);
		byte[] bytes = output.toBytes();
		LOG.debug("bytes len:{}", bytes.length);
		Input input = new Input(bytes);
		Person personDeserialize = new Person();
		LOG.debug("personDeserialize(before).age:{}", personDeserialize.age);
		LOG.debug("personDeserialize(before).name:{}", personDeserialize.name);
		LOG.debug("personDeserialize(before).sex:{}", personDeserialize.sex);
		LOG.debug("personDeserialize(before).address.length:{}", personDeserialize.address.length);

		personDeserialize = kryo.readObject(input, Person.class, serializer);
		LOG.debug("personDeserialize(after).age:{}", personDeserialize.age);
		LOG.debug("personDeserialize(after).name:{}", personDeserialize.name);
		LOG.debug("personDeserialize(after).sex:{}", personDeserialize.sex);
		LOG.debug("personDeserialize(after).address.length:{}", personDeserialize.address.length);
		LOG.debug("personDeserialize(after).address[0].street:{}", personDeserialize.address[0].street);
		LOG.debug("personDeserialize(after).address[0].zipcode:{}", personDeserialize.address[0].zipcode);
		LOG.debug("binary:{}", BinaryUtility.toHexString(bytes));
		//		int[] i_expect = { 0x01, 0x00, 0x08, 0x31, 0x32, 0x33, 0x2d, 0x34, 0x35, 0x36, 0x37, 0x01, 0x0f, 0xe3, 0x81, 0x82, 0xe3, 0x81, 0x82, 0xe3, 0x81, 0x82, 0xe3, 0x81, 0x82, 0xe3, 0x81, 0x82, 0x7f };
		//		byte[] expect = new byte[i_expect.length];
		//		for (int i = 0; i < i_expect.length; i++) {
		//			expect[i] = (byte) i_expect[i];
		//		}
		//
		//		assertTrue(Arrays.equals(expect, bytes));
	}

	@Test
	public void test_005() {
		Address addressSerialize = new Address();
		addressSerialize.street = "あああああ";
		addressSerialize.zipcode = "123-4567";
		Person personSerialize = new Person();
		personSerialize.age = 1;
		personSerialize.name = "aaaaa";
		personSerialize.sex = true;
		personSerialize.address = new Address[] { addressSerialize };
		long start = System.currentTimeMillis();
		for (int i = 0; i < 10_000_000; i++) {
			byte[] bytes = personSerialize.marshal();
			Person personDeserialize = new Person();
			personDeserialize.unmarshal(bytes);
		}
		long end = System.currentTimeMillis();
		LOG.debug("elapsed(colfer):{}", end - start);
	}

	@Test
	public void test_006() {
		Address addressSerialize = new Address();
		addressSerialize.street = "あああああ";
		addressSerialize.zipcode = "123-4567";
		Person personSerialize = new Person();
		personSerialize.age = 1;
		personSerialize.name = "aaaaa";
		personSerialize.sex = true;
		personSerialize.address = new Address[] { addressSerialize };
		long start = System.currentTimeMillis();
		for (int i = 0; i < 10_000_000; i++) {
			Output output = new Output(100);
			kryo.writeObject(output, personSerialize, serializer);
			byte[] bytes = output.toBytes();
			Input input = new Input(bytes);
			@SuppressWarnings("unused")
			Person personDeserialize = kryo.readObject(input, Person.class, serializer);
		}
		long end = System.currentTimeMillis();
		LOG.debug("elapsed(colfer & kryo):{}", end - start);
	}
}
