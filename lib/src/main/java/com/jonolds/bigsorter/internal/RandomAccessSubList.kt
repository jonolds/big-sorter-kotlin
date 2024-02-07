package com.jonolds.bigsorter.internal

import java.util.RandomAccess


internal class RandomAccessSubList<E>(
	list: AbstractList<E>, fromIndex: Int, toIndex: Int
) : SubList<E>(list, fromIndex, toIndex), RandomAccess {

	override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
		return RandomAccessSubList(this, fromIndex, toIndex)
	}
}




