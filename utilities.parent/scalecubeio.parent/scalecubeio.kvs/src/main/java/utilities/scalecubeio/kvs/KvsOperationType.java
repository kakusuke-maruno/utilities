package utilities.scalecubeio.kvs;

public enum KvsOperationType {
	GET(1), //
	PUT(2), //
	REMOVE(3), //
	PUTIFABSENT(4), //
	COMPAREANDPUT(5), //
	COMPAREANDREMOVE(6), //
	WRITE(7);

	public final byte id;

	private KvsOperationType(int id) {
		this.id = (byte) id;
	}

	private static final KvsOperationType[] arr = { //
			null, //
			GET, //
			PUT, //
			REMOVE, //
			PUTIFABSENT, //
			COMPAREANDPUT, //
			COMPAREANDREMOVE, //
			WRITE };

	public static KvsOperationType from(byte id) {
		return arr[id];
	}
}
