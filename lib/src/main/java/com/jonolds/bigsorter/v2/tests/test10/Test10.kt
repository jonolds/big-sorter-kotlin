package com.jonolds.bigsorter.v2.tests.test10

import com.jonolds.bigsorter.v2.phase.FileInputPhase
import com.jonolds.bigsorter.v2.phase.Sender


typealias Pred = () -> Boolean


fun main() {

}


interface IFace<T> {

    fun test(list: Sender<T>)
}

class A: IFace<Int> {


    override fun test(list: Sender<Int>) {
        TODO("Not yet implemented")
    }



}




