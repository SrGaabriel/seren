import me.gabriel.tianlong.TianlongModule
import me.gabriel.tianlong.struct.DragonType
import me.gabriel.tianlong.transcriber.TianlongIrTranscriber
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.*

class Basic extends AnyFlatSpec {

  "A simple function" should "be properly transcribed" in {
    val module = new TianlongModule()
    module.createFunction("main", List(DragonType.Int8, DragonType.Int8), DragonType.Int8)
    val transcriber = new TianlongIrTranscriber()
    val result = transcriber.transcribe(module)
    assertContentEquals("define i8 @main(i8 %0, i8 %1) {}", result)
  }

  private def assertContentEquals(expected: String, actual: String): Unit = {
    assert(expected.trim.replace("\n", "") == actual.trim.replace("\n", ""))
  }
}
