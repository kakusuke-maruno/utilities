package utilities.rxjava2.flow.file;

import java.io.File;

public class FileInfo<Meta> {
	public final File file;
	public final Meta meta;

	public FileInfo(File file, Meta meta) {
		this.file = file;
		this.meta = meta;
	}
}
