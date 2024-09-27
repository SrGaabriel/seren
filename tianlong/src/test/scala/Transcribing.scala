package me.gabriel.tianlong

import struct.{ConstantReference, DragonType}
import transcriber.TianlongIrTranscriber

import me.gabriel.tianlong.struct.DragonType.Int32
import org.scalatest.*
import org.scalatest.flatspec.AnyFlatSpec

class Transcribing extends AnyFlatSpec {

  "A simple function" should "be properly transcribed" in {
    val module = new TianlongModule()
    module.createFunction("main", List(DragonType.Int8, DragonType.Int8), DragonType.Int8)
    val transcriber = new TianlongIrTranscriber()
    val result = transcriber.transcribe(module)
    assertContentEquals("define i8 @main(i8 %0, i8 %1) {}", result)
  }

  "An add operation" should "be properly transcribed" in {
    val module = new TianlongModule()
    val function = module.createFunction("main", List(DragonType.Int8, DragonType.Int8), DragonType.Int8)
    function.assign(function.add(ConstantReference.Number("2", Int32), ConstantReference.Number("1", Int32)))
    val transcriber = new TianlongIrTranscriber()
    val result = transcriber.transcribe(module)
    assertContentEquals("define i8 @main(i8 %0, i8 %1) {" +
      "%2 = add i32 2, 1" +
      "}", result)
  }

  private def assertContentEquals(expected: String, actual: String): Unit = {
    assert(expected.trim.replace("\n", "") == actual.trim.replace("\n", ""))
  }
}
