package com.jonolds.bigsorter.serializers

import com.jonolds.bigsorter.ReaderBS
import com.jonolds.bigsorter.Util.N
import com.jonolds.bigsorter.internal.Array2
import java.nio.ByteBuffer
import java.nio.channels.FileChannel


abstract class BinaryLineReaderChAbstract<T>(
	val inCh: FileChannel,
	bufferSize: Int,
	delimiter: Byte = N
): ReaderBS<T>  {


	val buf: ByteBuffer = ByteBuffer.allocate(bufferSize)
	val arr: ByteArray = buf.array()
	val chSize = inCh.size()


	protected val lEnds = LineEnds(buf, delimiter)

	protected var bufPos = 0


	init {
		buf.limit(0)
		fillUpBuf()
	}


	protected fun fillUpBuf() {

		buf.position(bufPos)

		buf.compact()
		inCh.read(buf)
		buf.flip()

		lEnds.unpdateLineEnds()
		bufPos = 0
	}

	protected abstract fun elementFactory(start: Int, end: Int): T


	protected fun readLine(): T? {
		if (lEnds.remRecsBuf <= 0)
			return null
		val end = lEnds.lineEnds[lEnds.idx++]
		return elementFactory(bufPos, end).also { bufPos = end+1 }
	}

	override fun read(): T? =
		readLine() ?: let {
			fillUpBuf()
			readLine()
		}




	override fun readBulk(limit: Int, result: MutableList<in T>): Int {

		var remToTake = limit

		while (remToTake > 0) {

			if (lEnds.remRecsBuf == 0) {
				fillUpBuf()
				if (lEnds.remRecsBuf == 0)
					break
			}

			val numToTake = minOf(remToTake, lEnds.remRecsBuf)

			repeat(numToTake) {
				val end = lEnds.lineEnds[lEnds.idx++]
				result.add(elementFactory(bufPos, end))
				bufPos = end+1
			}

			remToTake -= numToTake
		}

		return limit-remToTake
	}


	override fun readBulkArray(limit: Int, result: Array2<T>): Array2<T> {

		var remToTake = limit
		while (remToTake > 0) {


			if (lEnds.remRecsBuf == 0) {
				fillUpBuf()
				if (lEnds.remRecsBuf == 0)
					break
			}

			val numToTake = minOf(remToTake, lEnds.remRecsBuf)


			repeat(numToTake) {
				val end = lEnds.lineEnds[lEnds.idx++]
				result.add(elementFactory(bufPos, end))
				bufPos = end+1
			}

			remToTake -= numToTake
		}


		return result
	}


	override fun <S> readBulkArray(limit: Int, result: Array2<S>, mapper: (T) -> S): Array2<S> {

		var remToTake = limit
		while (remToTake > 0) {


			if (lEnds.remRecsBuf == 0) {
				fillUpBuf()
				if (lEnds.remRecsBuf == 0)
					break
			}

			val numToTake = minOf(remToTake, lEnds.remRecsBuf)


			repeat(numToTake) {
				val end = lEnds.lineEnds[lEnds.idx++]
				result.add(mapper(elementFactory(bufPos, end)))
				bufPos = end+1
			}

			remToTake -= numToTake
		}


		return result
	}




	override fun close() = inCh.close()

}



data class LineEnds private constructor(
	private val buf: ByteBuffer,
	val delimiter: Byte,
	var size: Int,
	var idx: Int = 0,
	private var nextOffset: Int = 0
) {

	private val arr: ByteArray = buf.array()
	var lineEnds: IntArray = IntArray(arr.size/10)


	constructor(buf: ByteBuffer, delim: Byte): this(buf, delim, 0)


	val remRecsBuf: Int get() = size-idx


	fun unpdateLineEnds() {
		size = 0
		idx = 0

		for (i in nextOffset until buf.limit())
			if (arr[i] == delimiter) {
				lineEnds[size++] = i
				if (size == lineEnds.size)
					growLineEnds()
			}

		if (size == 0)
			return

		nextOffset = buf.limit()-lineEnds[size-1]-1
	}


	private fun growLineEnds() {
		println("growLineEnds")
		val newSize = ((size*buf.limit()*1.1)/lineEnds[size-1]).toInt()
		lineEnds = lineEnds.copyInto(IntArray(newSize))
	}


}