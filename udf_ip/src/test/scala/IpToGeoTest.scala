import org.scalatest.FunSuite
import com.nykytenko._

class IpToGeoTest extends FunSuite {
  test("IpToGeo works for some simple positive cases") {
    val ipToGeo = new IpToGeo
    assert(ipToGeo.isInRange("127.128.129.130", "127.128.129.0/24"))
  }
}
