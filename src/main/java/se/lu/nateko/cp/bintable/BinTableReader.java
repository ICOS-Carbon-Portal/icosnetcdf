package se.lu.nateko.cp.bintable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;

public class BinTableReader extends BinTableFile {

	public BinTableReader(File file, Schema schema) throws FileNotFoundException {
		super(file, schema, "r");
	}

	public Buffer read(int column) throws IOException{
		if(schema.size > Integer.MAX_VALUE)
			throw new IOException("The table is too large, cannot read whole column into a buffer");
		return read(column, 0, (int)schema.size);
	}

	public Buffer read(int column, long offset, int size) throws IOException{
		DataType dt = schema.columns[column];

		int valueSize = Utils.getDataTypeSize(dt);
		long byteOffset = columnOffsets[column] + offset * valueSize;
		int byteSize = valueSize * size;

		ByteBuffer bytes = file.getChannel().map(MapMode.READ_ONLY, byteOffset, byteSize);

		switch(dt){
			case INT: return bytes.asIntBuffer();
			case LONG: return bytes.asLongBuffer();
			case FLOAT: return bytes.asFloatBuffer();
			case DOUBLE: return bytes.asDoubleBuffer();
			case SHORT: return bytes.asShortBuffer();
			case CHAR: return bytes.asCharBuffer();
			case BYTE: return bytes;
			case STRING: return bytes.asIntBuffer();
			default: throw Utils.unsupportedDatatypeException(dt);
		}
	}

	@Override
	public void close() throws IOException {
		file.close();
	}
}
