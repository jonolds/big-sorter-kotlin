package com.jonolds.bigsorter

import java.io.IOException
import java.io.InputStream

class NonClosingInputStream(private val inStr: InputStream) : InputStream() {

	override fun read(): Int = inStr.read()

	override fun read(b: ByteArray): Int = inStr.read(b)

	override fun read(b: ByteArray, off: Int, len: Int): Int = inStr.read(b, off, len)

	override fun skip(n: Long): Long = inStr.skip(n)

	override fun available(): Int = inStr.available()

	override fun close() { }

	@Synchronized
	override fun mark(readlimit: Int) = inStr.mark(readlimit)

	@Synchronized
	override fun reset() = inStr.reset()

	override fun markSupported(): Boolean = inStr.markSupported()
}