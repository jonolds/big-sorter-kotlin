package com.jonolds.bigsorter.serializers

import com.jonolds.bigsorter.ReaderBS
import com.jonolds.bigsorter.WriterBS
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import java.io.*
import java.nio.charset.Charset
import java.util.function.Function


internal class CsvSerializer(private val format: CSVFormat, private val charset: Charset) : SerializerBS<CSVRecord> {


	override fun createReader(inStr: InputStream): ReaderBS<CSVRecord> = object : ReaderBS<CSVRecord> {
		var iter: Iterator<CSVRecord>? = null
		var isr: InputStreamReader? = null

		override fun read(): CSVRecord? {
			if (iter == null) {
				isr = InputStreamReader(inStr, charset)
				iter = format.parse(isr).iterator()
			}
			return if (iter!!.hasNext()) iter!!.next() else null
		}

		override fun close() = isr?.close() ?: Unit
	}

	override fun createWriter(out: OutputStream): WriterBS<CSVRecord> = object : WriterBS<CSVRecord> {

		var printer: CSVPrinter? = null
		private var ps: PrintStream? = null


		override fun write(value: CSVRecord?) {
			value ?: return
			if (printer == null) {
				ps = PrintStream(out, false, charset)
				printer = format.print(ps)
				// print header line
				val h = value.parser.headerNames
				if (h.isNotEmpty())
					printer!!.printRecord(h)
			}
			printer!!.printRecord(value)
		}


		override fun <S> map(mapper: Function<in S?, out CSVRecord?>): WriterBS<S> = this.let { tWriter ->
			object : WriterBS<S> {
				override fun close() = tWriter.close()

				override fun write(value: S?) = tWriter.write(mapper.apply(value))

				override fun flush() = tWriter.flush()
			}
		}

		override fun close() = flush()

		override fun flush() = ps?.flush() ?: Unit
	}
}