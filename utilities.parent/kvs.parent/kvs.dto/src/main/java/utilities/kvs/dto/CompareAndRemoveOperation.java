package utilities.kvs.dto;


// Code generated by colf(1); DO NOT EDIT.


import static java.lang.String.format;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.InputMismatchException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;


/**
 * Data bean with built-in serialization support.

 * @author generated by colf(1)
 * @see <a href="https://github.com/pascaldekloe/colfer">Colfer's home</a>
 */
@javax.annotation.Generated(value="colf(1)", comments="Colfer from schema file KvsOperation.colf")
public class CompareAndRemoveOperation extends utilities.kvs.SerializableData implements Serializable {

	/** The upper limit for serial byte sizes. */
	public static int colferSizeMax = 16 * 1024 * 1024;




	public byte dataType;

	public int kvs;

	public long id;

	public byte[] key;

	public byte[] expect;


	/** Default constructor */
	public CompareAndRemoveOperation() {
		init();
	}

	private static final byte[] _zeroBytes = new byte[0];

	/** Colfer zero values. */
	private void init() {
		key = _zeroBytes;
		expect = _zeroBytes;
	}

	/**
	 * {@link #reset(InputStream) Reusable} deserialization of Colfer streams.
	 */
	public static class Unmarshaller {

		/** The data source. */
		protected InputStream in;

		/** The read buffer. */
		public byte[] buf;

		/** The {@link #buf buffer}'s data start index, inclusive. */
		protected int offset;

		/** The {@link #buf buffer}'s data end index, exclusive. */
		protected int i;


		/**
		 * @param in the data source or {@code null}.
		 * @param buf the initial buffer or {@code null}.
		 */
		public Unmarshaller(InputStream in, byte[] buf) {
			// TODO: better size estimation
			if (buf == null || buf.length == 0)
				buf = new byte[Math.min(CompareAndRemoveOperation.colferSizeMax, 2048)];
			this.buf = buf;
			reset(in);
		}

		/**
		 * Reuses the marshaller.
		 * @param in the data source or {@code null}.
		 * @throws IllegalStateException on pending data.
		 */
		public void reset(InputStream in) {
			if (this.i != this.offset) throw new IllegalStateException("colfer: pending data");
			this.in = in;
			this.offset = 0;
			this.i = 0;
		}

		/**
		 * Deserializes the following object.
		 * @return the result or {@code null} when EOF.
		 * @throws IOException from the input stream.
		 * @throws SecurityException on an upper limit breach defined by {@link #colferSizeMax}.
		 * @throws InputMismatchException when the data does not match this object's schema.
		 */
		public CompareAndRemoveOperation next() throws IOException {
			if (in == null) return null;

			while (true) {
				if (this.i > this.offset) {
					try {
						CompareAndRemoveOperation o = new CompareAndRemoveOperation();
						this.offset = o.unmarshal(this.buf, this.offset, this.i);
						return o;
					} catch (BufferUnderflowException e) {
					}
				}
				// not enough data

				if (this.i <= this.offset) {
					this.offset = 0;
					this.i = 0;
				} else if (i == buf.length) {
					byte[] src = this.buf;
					// TODO: better size estimation
					if (offset == 0) this.buf = new byte[Math.min(CompareAndRemoveOperation.colferSizeMax, this.buf.length * 4)];
					System.arraycopy(src, this.offset, this.buf, 0, this.i - this.offset);
					this.i -= this.offset;
					this.offset = 0;
				}
				assert this.i < this.buf.length;

				int n = in.read(buf, i, buf.length - i);
				if (n < 0) {
					if (this.i > this.offset)
						throw new InputMismatchException("colfer: pending data with EOF");
					return null;
				}
				assert n > 0;
				i += n;
			}
		}

	}


	/**
	 * Serializes the object.
	 * @param out the data destination.
	 * @param buf the initial buffer or {@code null}.
	 * @return the final buffer. When the serial fits into {@code buf} then the return is {@code buf}.
	 *  Otherwise the return is a new buffer, large enough to hold the whole serial.
	 * @throws IOException from {@code out}.
	 * @throws IllegalStateException on an upper limit breach defined by {@link #colferSizeMax}.
	 */
	public byte[] marshal(OutputStream out, byte[] buf) throws IOException {
		// TODO: better size estimation
		if (buf == null || buf.length == 0)
			buf = new byte[Math.min(CompareAndRemoveOperation.colferSizeMax, 2048)];

		while (true) {
			int i;
			try {
				i = marshal(buf, 0);
			} catch (BufferOverflowException e) {
				buf = new byte[Math.min(CompareAndRemoveOperation.colferSizeMax, buf.length * 4)];
				continue;
			}

			out.write(buf, 0, i);
			return buf;
		}
	}

	/**
	 * Serializes the object.
	 * @param buf the data destination.
	 * @param offset the initial index for {@code buf}, inclusive.
	 * @return the final index for {@code buf}, exclusive.
	 * @throws BufferOverflowException when {@code buf} is too small.
	 * @throws IllegalStateException on an upper limit breach defined by {@link #colferSizeMax}.
	 */
	public int marshal(byte[] buf, int offset) {
		int i = offset;

		try {
			if (this.dataType != 0) {
				buf[i++] = (byte) 0;
				buf[i++] = this.dataType;
			}

			if (this.kvs != 0) {
				int x = this.kvs;
				if ((x & ~((1 << 21) - 1)) != 0) {
					buf[i++] = (byte) (1 | 0x80);
					buf[i++] = (byte) (x >>> 24);
					buf[i++] = (byte) (x >>> 16);
					buf[i++] = (byte) (x >>> 8);
				} else {
					buf[i++] = (byte) 1;
					while (x > 0x7f) {
						buf[i++] = (byte) (x | 0x80);
						x >>>= 7;
					}
				}
				buf[i++] = (byte) x;
			}

			if (this.id != 0) {
				long x = this.id;
				if ((x & ~((1L << 49) - 1)) != 0) {
					buf[i++] = (byte) (2 | 0x80);
					buf[i++] = (byte) (x >>> 56);
					buf[i++] = (byte) (x >>> 48);
					buf[i++] = (byte) (x >>> 40);
					buf[i++] = (byte) (x >>> 32);
					buf[i++] = (byte) (x >>> 24);
					buf[i++] = (byte) (x >>> 16);
					buf[i++] = (byte) (x >>> 8);
					buf[i++] = (byte) (x);
				} else {
					buf[i++] = (byte) 2;
					while (x > 0x7fL) {
						buf[i++] = (byte) (x | 0x80);
						x >>>= 7;
					}
					buf[i++] = (byte) x;
				}
			}

			if (this.key.length != 0) {
				buf[i++] = (byte) 3;

				int size = this.key.length;
				if (size > CompareAndRemoveOperation.colferSizeMax)
					throw new IllegalStateException(format("colfer: utilities/kvs/dto.CompareAndRemoveOperation.key size %d exceeds %d bytes", size, CompareAndRemoveOperation.colferSizeMax));

				int x = size;
				while (x > 0x7f) {
					buf[i++] = (byte) (x | 0x80);
					x >>>= 7;
				}
				buf[i++] = (byte) x;

				int start = i;
				i += size;
				System.arraycopy(this.key, 0, buf, start, size);
			}

			if (this.expect.length != 0) {
				buf[i++] = (byte) 4;

				int size = this.expect.length;
				if (size > CompareAndRemoveOperation.colferSizeMax)
					throw new IllegalStateException(format("colfer: utilities/kvs/dto.CompareAndRemoveOperation.expect size %d exceeds %d bytes", size, CompareAndRemoveOperation.colferSizeMax));

				int x = size;
				while (x > 0x7f) {
					buf[i++] = (byte) (x | 0x80);
					x >>>= 7;
				}
				buf[i++] = (byte) x;

				int start = i;
				i += size;
				System.arraycopy(this.expect, 0, buf, start, size);
			}

			buf[i++] = (byte) 0x7f;
			return i;
		} catch (ArrayIndexOutOfBoundsException e) {
			if (i - offset > CompareAndRemoveOperation.colferSizeMax)
				throw new IllegalStateException(format("colfer: utilities/kvs/dto.CompareAndRemoveOperation exceeds %d bytes", CompareAndRemoveOperation.colferSizeMax));
			if (i > buf.length) throw new BufferOverflowException();
			throw e;
		}
	}

	/**
	 * Deserializes the object.
	 * @param buf the data source.
	 * @param offset the initial index for {@code buf}, inclusive.
	 * @return the final index for {@code buf}, exclusive.
	 * @throws BufferUnderflowException when {@code buf} is incomplete. (EOF)
	 * @throws SecurityException on an upper limit breach defined by {@link #colferSizeMax}.
	 * @throws InputMismatchException when the data does not match this object's schema.
	 */
	public int unmarshal(byte[] buf, int offset) {
		return unmarshal(buf, offset, buf.length);
	}

	/**
	 * Deserializes the object.
	 * @param buf the data source.
	 * @param offset the initial index for {@code buf}, inclusive.
	 * @param end the index limit for {@code buf}, exclusive.
	 * @return the final index for {@code buf}, exclusive.
	 * @throws BufferUnderflowException when {@code buf} is incomplete. (EOF)
	 * @throws SecurityException on an upper limit breach defined by {@link #colferSizeMax}.
	 * @throws InputMismatchException when the data does not match this object's schema.
	 */
	public int unmarshal(byte[] buf, int offset, int end) {
		if (end > buf.length) end = buf.length;
		int i = offset;

		try {
			byte header = buf[i++];

			if (header == (byte) 0) {
				this.dataType = buf[i++];
				header = buf[i++];
			}

			if (header == (byte) 1) {
				int x = 0;
				for (int shift = 0; true; shift += 7) {
					byte b = buf[i++];
					x |= (b & 0x7f) << shift;
					if (shift == 28 || b >= 0) break;
				}
				this.kvs = x;
				header = buf[i++];
			} else if (header == (byte) (1 | 0x80)) {
				this.kvs = (buf[i++] & 0xff) << 24 | (buf[i++] & 0xff) << 16 | (buf[i++] & 0xff) << 8 | (buf[i++] & 0xff);
				header = buf[i++];
			}

			if (header == (byte) 2) {
				long x = 0;
				for (int shift = 0; true; shift += 7) {
					byte b = buf[i++];
					if (shift == 56 || b >= 0) {
						x |= (b & 0xffL) << shift;
						break;
					}
					x |= (b & 0x7fL) << shift;
				}
				this.id = x;
				header = buf[i++];
			} else if (header == (byte) (2 | 0x80)) {
				this.id = (buf[i++] & 0xffL) << 56 | (buf[i++] & 0xffL) << 48 | (buf[i++] & 0xffL) << 40 | (buf[i++] & 0xffL) << 32
					| (buf[i++] & 0xffL) << 24 | (buf[i++] & 0xffL) << 16 | (buf[i++] & 0xffL) << 8 | (buf[i++] & 0xffL);
				header = buf[i++];
			}

			if (header == (byte) 3) {
				int size = 0;
				for (int shift = 0; true; shift += 7) {
					byte b = buf[i++];
					size |= (b & 0x7f) << shift;
					if (shift == 28 || b >= 0) break;
				}
				if (size < 0 || size > CompareAndRemoveOperation.colferSizeMax)
					throw new SecurityException(format("colfer: utilities/kvs/dto.CompareAndRemoveOperation.key size %d exceeds %d bytes", size, CompareAndRemoveOperation.colferSizeMax));

				this.key = new byte[size];
				int start = i;
				i += size;
				System.arraycopy(buf, start, this.key, 0, size);

				header = buf[i++];
			}

			if (header == (byte) 4) {
				int size = 0;
				for (int shift = 0; true; shift += 7) {
					byte b = buf[i++];
					size |= (b & 0x7f) << shift;
					if (shift == 28 || b >= 0) break;
				}
				if (size < 0 || size > CompareAndRemoveOperation.colferSizeMax)
					throw new SecurityException(format("colfer: utilities/kvs/dto.CompareAndRemoveOperation.expect size %d exceeds %d bytes", size, CompareAndRemoveOperation.colferSizeMax));

				this.expect = new byte[size];
				int start = i;
				i += size;
				System.arraycopy(buf, start, this.expect, 0, size);

				header = buf[i++];
			}

			if (header != (byte) 0x7f)
				throw new InputMismatchException(format("colfer: unknown header at byte %d", i - 1));
		} finally {
			if (i > end && end - offset < CompareAndRemoveOperation.colferSizeMax) throw new BufferUnderflowException();
			if (i < 0 || i - offset > CompareAndRemoveOperation.colferSizeMax)
				throw new SecurityException(format("colfer: utilities/kvs/dto.CompareAndRemoveOperation exceeds %d bytes", CompareAndRemoveOperation.colferSizeMax));
			if (i > end) throw new BufferUnderflowException();
		}

		return i;
	}

	// {@link Serializable} version number.
	private static final long serialVersionUID = 5L;

	// {@link Serializable} Colfer extension.
	private void writeObject(ObjectOutputStream out) throws IOException {
		// TODO: better size estimation
		byte[] buf = new byte[1024];
		int n;
		while (true) try {
			n = marshal(buf, 0);
			break;
		} catch (BufferUnderflowException e) {
			buf = new byte[4 * buf.length];
		}

		out.writeInt(n);
		out.write(buf, 0, n);
	}

	// {@link Serializable} Colfer extension.
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		init();

		int n = in.readInt();
		byte[] buf = new byte[n];
		in.readFully(buf);
		unmarshal(buf, 0);
	}

	// {@link Serializable} Colfer extension.
	private void readObjectNoData() throws ObjectStreamException {
		init();
	}

	/**
	 * Gets utilities/kvs/dto.CompareAndRemoveOperation.dataType.
	 * @return the value.
	 */
	public byte getDataType() {
		return this.dataType;
	}

	/**
	 * Sets utilities/kvs/dto.CompareAndRemoveOperation.dataType.
	 * @param value the replacement.
	 */
	public void setDataType(byte value) {
		this.dataType = value;
	}

	/**
	 * Gets utilities/kvs/dto.CompareAndRemoveOperation.kvs.
	 * @return the value.
	 */
	public int getKvs() {
		return this.kvs;
	}

	/**
	 * Sets utilities/kvs/dto.CompareAndRemoveOperation.kvs.
	 * @param value the replacement.
	 */
	public void setKvs(int value) {
		this.kvs = value;
	}

	/**
	 * Gets utilities/kvs/dto.CompareAndRemoveOperation.id.
	 * @return the value.
	 */
	public long getId() {
		return this.id;
	}

	/**
	 * Sets utilities/kvs/dto.CompareAndRemoveOperation.id.
	 * @param value the replacement.
	 */
	public void setId(long value) {
		this.id = value;
	}

	/**
	 * Gets utilities/kvs/dto.CompareAndRemoveOperation.key.
	 * @return the value.
	 */
	public byte[] getKey() {
		return this.key;
	}

	/**
	 * Sets utilities/kvs/dto.CompareAndRemoveOperation.key.
	 * @param value the replacement.
	 */
	public void setKey(byte[] value) {
		this.key = value;
	}

	/**
	 * Gets utilities/kvs/dto.CompareAndRemoveOperation.expect.
	 * @return the value.
	 */
	public byte[] getExpect() {
		return this.expect;
	}

	/**
	 * Sets utilities/kvs/dto.CompareAndRemoveOperation.expect.
	 * @param value the replacement.
	 */
	public void setExpect(byte[] value) {
		this.expect = value;
	}

	@Override
	public final int hashCode() {
		int h = 1;
		h = 31 * h + (this.dataType & 0xff);
		h = 31 * h + this.kvs;
		h = 31 * h + (int)(this.id ^ this.id >>> 32);
		for (byte b : this.key) h = 31 * h + b;
		for (byte b : this.expect) h = 31 * h + b;
		return h;
	}

	@Override
	public final boolean equals(Object o) {
		return o instanceof CompareAndRemoveOperation && equals((CompareAndRemoveOperation) o);
	}

	public final boolean equals(CompareAndRemoveOperation o) {
		if (o == null) return false;
		if (o == this) return true;
		return o.getClass() == CompareAndRemoveOperation.class
			&& this.dataType == o.dataType
			&& this.kvs == o.kvs
			&& this.id == o.id
			&& java.util.Arrays.equals(this.key, o.key)
			&& java.util.Arrays.equals(this.expect, o.expect);
	}

}
