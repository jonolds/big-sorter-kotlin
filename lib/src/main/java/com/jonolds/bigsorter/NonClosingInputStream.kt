package com.jonolds.bigsorter

import java.io.IOException
import java.io.InputStream

class NonClosingInputStream(private val inStr: InputStream) : InputStream() {

	@Throws(IOException::class)
	override fun read(): Int {
		return inStr.read()
	}

	@Throws(IOException::class)
	override fun read(b: ByteArray): Int {
		return inStr.read(b)
	}

	@Throws(IOException::class)
	override fun read(b: ByteArray, off: Int, len: Int): Int {
		return inStr.read(b, off, len)
	}

	@Throws(IOException::class)
	override fun skip(n: Long): Long {
		return inStr.skip(n)
	}

	@Throws(IOException::class)
	override fun available(): Int {
		return inStr.available()
	}

	@Throws(IOException::class)
	override fun close() {
		// don't close in
	}

	@Synchronized
	override fun mark(readlimit: Int) {
		inStr.mark(readlimit)
	}

	@Synchronized
	@Throws(IOException::class)
	override fun reset() {
		inStr.reset()
	}

	override fun markSupported(): Boolean {
		return inStr.markSupported()
	}
}