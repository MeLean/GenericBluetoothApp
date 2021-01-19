package com.milen.bluetoothapp

import org.mockito.Mockito
import org.mockito.stubbing.OngoingStubbing

// To avoid having to use backticks for "when"
fun <T> whenever(methodCall: T): OngoingStubbing<T> = Mockito.`when`(methodCall)
