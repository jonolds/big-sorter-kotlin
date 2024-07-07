package com.jonolds.bigsorter.serializers

import com.jonolds.bigsorter.ReaderBS
import com.jonolds.bigsorter.Util.N
import com.jonolds.bigsorter.WriterBS
import com.jonolds.bigsorter.internal.Array2
import java.nio.ByteBuffer
import java.nio.channels.FileChannel



class BinLineTempSerializer(
    val inBufferSize: Int = DEFAULT_BUFFER_SIZE/10,
    val outBufferSize: Int = DEFAULT_BUFFER_SIZE
): ChannelSerializer<ByteArray> {

	override val clazz: Class<ByteArray> = ByteArray::class.java

    override fun createChannelReader(inCh: FileChannel, bufferSize: Int?): ReaderBS<ByteArray> =
        BinLineReaderCh(inCh, bufferSize ?: inBufferSize)

    override fun createChannelWriter(outCh: FileChannel, bufferSize: Int?): WriterBS<ByteArray> =
        TempBinLineWriterCh(outCh, bufferSize ?: outBufferSize)



}



abstract class BinLineReaderChAbstract<T>(
	val inCh: FileChannel,
	val bufferSize: Int
): Reader2<T>  {

	val buf: ByteBuffer = ByteBuffer.allocate(bufferSize)
	val arr: ByteArray = buf.array()


	var idxArr = IntArray(bufferSize/6) { -1 }
	var curIdx = 1
	var idxLimit = 1


	init {
		buf.limit(0)
		fillUpBuf2()
	}

	val chSize = inCh.size()



	protected fun fillUpBuf(): Int {
		buf.compact()
		val bytesRead = inCh.read(buf)
		buf.flip()
		return bytesRead
	}

	protected fun fillUpBuf2() {
		buf.compact()
		inCh.read(buf)
		buf.flip()

		curIdx = 1
		idxLimit = 1
		for (i in 0 until buf.limit()){
			if (arr[i] == N) {
				if (idxLimit == idxArr.size) {
					//TODO Expand Array
				}
				idxArr[idxLimit++] = i
			}
		}

		buf.position(idxArr[idxLimit-1]+1)
	}

	protected abstract fun elementFactory(start: Int, end: Int): T




	private fun readLine(): T? = buf.run {
		val len = array().indexOfFirst(position(), limit(), N)?.minus(position()) ?: return null
		return@run elementFactory(position(), position()+len)
			.also { position(position()+1+len) }
	}

	override fun read(): T? =
		readLine() ?: let {
			val newlyRead = fillUpBuf()
			if (newlyRead > 0) readLine() else null
		}




	override fun readBulk(limit: Int, result: MutableList<in T>): Int {
		val origSize = result.size


		while (result.size - origSize < limit && curIdx < idxLimit) {

			val numToTake = minOf(limit - (result.size-origSize), idxLimit-curIdx)

			repeat(numToTake) {
				val elem = elementFactory(idxArr[curIdx-1]+1, idxArr[curIdx++])
				result.add(elem)
			}

			if(curIdx == idxLimit && inCh.position() < chSize)
				fillUpBuf2()
		}

		return result.size-origSize
	}


	override fun readBulkArray(limit: Int, result: Array2<T>): Array2<T> {
		val origSize = result.size

		while (result.size - origSize < limit && curIdx < idxLimit) {

			val numToTake = minOf(limit - (result.size-origSize), idxLimit-curIdx)

			repeat(numToTake) {
				val elem = elementFactory(idxArr[curIdx-1]+1, idxArr[curIdx++])
				result.add(elem)
			}

			if(curIdx == idxLimit && inCh.position() < chSize)
				fillUpBuf2()
		}


		return result
	}


	override fun <S> readBulkArray(limit: Int, result: Array2<S>, mapper: (T) -> S): Array2<S> {

		val origSize = result.size

		while (result.size - origSize < limit && curIdx < idxLimit) {

			val numToTake = minOf(limit - (result.size-origSize), idxLimit-curIdx)

			for (i in 0 until numToTake)
				result.add(mapper(elementFactory(idxArr[curIdx-1]+1, idxArr[curIdx++])))

			if(curIdx == idxLimit && inCh.position() < chSize)
				fillUpBuf2()

		}


		return result
	}




	override fun close() = inCh.close()
}

class BinLineReaderCh(
	inCh: FileChannel,
	bufferSize: Int
): BinLineReaderChAbstract<ByteArray>(inCh, bufferSize) {

	override fun elementFactory(start: Int, end: Int): ByteArray =
		arr.copyOfRange(start, end)
}



class TempBinLineWriterCh(
	val outCh: FileChannel,
	val bufferSize: Int
): Writer2<ByteArray> {


	private val outBuf: ByteBuffer = ByteBuffer.allocate(bufferSize)


	override fun flush() {
		outBuf.flip()
		outCh.write(outBuf)
		outBuf.compact()
	}

	override fun write(value: ByteArray?) {
		value ?: return

		if (outBuf.remaining() < value.size+1)
			flush()
		outBuf.put(value).put(N)
	}

	override fun close() {
		flush()
		outCh.close()
	}

}



fun ByteArray.indexOfFirst(start: Int, limit: Int, b: Byte): Int? {
    for (index in start until limit) {
        if (this[index] == b)
            return index
    }
    return null
}



typealias Writer2<T> = WriterBS<T>
typealias Reader2<T> = ReaderBS<T>
typealias Serializer<T> = SerializerBS<T>