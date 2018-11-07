package utilities.rxjava2.flow.file;

import java.io.IOException;

public interface FileParser<T, Meta> {
	void initialize(FileInfo<Meta> fileInfo) throws IOException;

	T next() throws IOException;

	void terminate(FileInfo<Meta> fileInfo) throws IOException;
}
