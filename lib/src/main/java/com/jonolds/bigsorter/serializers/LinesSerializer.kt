package com.jonolds.bigsorter.serializers

import com.jonolds.bigsorter.ReaderBS
import com.jonolds.bigsorter.WriterBS
import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

internal class LinesSerializer(
	private val charset: Charset,
	private val delimiter: LineDelimiter
) : SerializerBS<String> {


	override fun createReader(inStr: InputStream): ReaderBS<String> = object : ReaderBS<String> {
		var br: BufferedReader = BufferedReader(InputStreamReader(inStr, charset))

		override fun read(): String? = br.readLine()

		override fun close() = br.close()
	}

	override fun createWriter(out: OutputStream): WriterBS<String> = object : WriterBS<String> {

		var bw: BufferedWriter = BufferedWriter(OutputStreamWriter(out, charset))

		override fun write(value: String?) {
			value ?: return
			bw.write(value)
			bw.write(delimiter.value())
		}

		override fun close() = bw.close()

		override fun flush() = bw.flush()
	}

	companion object {
        val LINES_UTF8_LF: SerializerBS<String> = LinesSerializer(StandardCharsets.UTF_8, LineDelimiter.LINE_FEED)
        val LINES_UTF8_CR_LF: SerializerBS<String> =
			LinesSerializer(StandardCharsets.UTF_8, LineDelimiter.CARRIAGE_RETURN_LINE_FEED)
	}
}
