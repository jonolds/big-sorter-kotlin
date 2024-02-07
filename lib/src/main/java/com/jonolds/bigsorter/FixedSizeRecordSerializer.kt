package com.jonolds.bigsorter

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

internal class FixedSizeRecordSerializer(private val size: Int) : DataSerializer<ByteArray>() {


	@Throws(IOException::class)
	override fun read(dis: DataInputStream): ByteArray {
		val bytes = ByteArray(size)
		dis.readFully(bytes)
		return bytes
	}

	@Throws(IOException::class)
	override fun write(dos: DataOutputStream, value: ByteArray?) {
		value ?: return
		dos.write(value)
	}
}
