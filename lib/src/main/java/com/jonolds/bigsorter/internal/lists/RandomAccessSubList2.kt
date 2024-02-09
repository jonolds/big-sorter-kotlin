package com.jonolds.bigsorter.internal.lists
//
//import java.util.RandomAccess
//
//class RandomAccessSubList2<E>(
//	list: AbstractList2<E>, fromIndex: Int, toIndex: Int
//) : SubList2<E>(list, fromIndex, toIndex), RandomAccess {
//
//	override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
//		return RandomAccessSubList2(this, fromIndex, toIndex)
//	}
//}