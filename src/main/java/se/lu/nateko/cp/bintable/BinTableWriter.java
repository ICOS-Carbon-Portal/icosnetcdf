package se.lu.nateko.cp.bintable;

import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.FloatBuffer;
import java.nio.DoubleBuffer;
import java.nio.ShortBuffer;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

public class BinTableWriter extends BinTableFile{

	private final MappedByteBuffer[] buffers;
	private final Buffer[] typedBuffers;

	public BinTableWriter(File file, Schema schema) throws IOException {
		super(file, schema, "rw");

		int nCols = this.columnSizes.length;

		this.file.setLength(columnOffsets[nCols]); //offset AFTER the last column

		buffers = new MappedByteBuffer[nCols];
		typedBuffers = new Buffer[nCols];

		for(int i = 0; i < nCols; i++){

			buffers[i] = this.file.getChannel().map(MapMode.READ_WRITE, columnOffsets[i], columnSizes[i]);

			DataType dt = schema.columns[i];

			switch(dt){
				case INT:
					typedBuffers[i] = buffers[i].asIntBuffer(); break;
				case LONG:
					typedBuffers[i] = buffers[i].asLongBuffer(); break;
				case FLOAT:
					typedBuffers[i] = buffers[i].asFloatBuffer(); break;
				case DOUBLE:
					typedBuffers[i] = buffers[i].asDoubleBuffer(); break;
				case SHORT:
					typedBuffers[i] = buffers[i].asShortBuffer(); break;
				case CHAR:
					typedBuffers[i] = buffers[i].asCharBuffer(); break;
				case BYTE:
					typedBuffers[i] = buffers[i]; break;
				default: throw Utils.unsupportedDatatypeException(dt);
			}

		}
	}

	public void writeRow(Object[] row) throws IOException {

		for(int i = 0; i < schema.columns.length; i++){

			DataType dt = schema.columns[i];

			switch(dt){
				case INT:
					((IntBuffer)typedBuffers[i]).put((int)row[i]); break;
				case LONG:
					((LongBuffer)typedBuffers[i]).put((long)row[i]); break;
				case FLOAT:
					((FloatBuffer)typedBuffers[i]).put((float)row[i]); break;
				case DOUBLE:
					((DoubleBuffer)typedBuffers[i]).put((double)row[i]); break;
				case SHORT:
					((ShortBuffer)typedBuffers[i]).put((short)row[i]); break;
				case CHAR:
					((CharBuffer)typedBuffers[i]).put((char)row[i]); break;
				case BYTE:
					((ByteBuffer)typedBuffers[i]).put((byte)row[i]); break;

				default: throw Utils.unsupportedDatatypeException(dt);
			}
		}

	}

	@Override
	public void close() throws IOException {
		for(MappedByteBuffer buffer: buffers){
			buffer.force();
		}
		file.close();
	}
	
}

