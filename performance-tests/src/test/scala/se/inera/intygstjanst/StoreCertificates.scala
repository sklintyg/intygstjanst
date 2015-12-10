package se.inera.intygstjanst
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.concurrent.duration._
import java.util.UUID

class StoreCertificates extends Simulation {

  val testpersonnummer = csv("data/testpersonnummer_skatteverket.cvs").circular

  val scn = scenario("Store Certificates")
    .feed(testpersonnummer)
      .repeat(10) {
        exec(session=>session.set("utlatandeId", UUID.randomUUID().toString()))
        .exec(http("Store certificate ${personNr}")
          .post("/register-certificate/v3.0")
            .headers(Headers.store_certificate)
            .body(ELFileBody("request-bodies/register-medical-certificate.xml"))
            .check(status.is(200)))
      }
   .pause(2 seconds)
  setUp(scn.inject(rampUsers(100) over (120 seconds)).protocols(Conf.httpConf))

}
