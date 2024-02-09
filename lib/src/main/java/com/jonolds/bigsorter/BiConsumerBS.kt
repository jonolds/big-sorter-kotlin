package com.jonolds.bigsorter

fun interface BiConsumerBS<S, T> {
	fun accept(s: S, t: T)
}
