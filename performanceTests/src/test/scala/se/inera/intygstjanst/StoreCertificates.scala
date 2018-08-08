package se.inera.intygstjanst

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.concurrent.duration._
import java.util.UUID

class StoreCertificates extends Simulation {

  val testpersonnummer = csv("data/testpersonnummerSkatteverket.csv").circular

  val scn = scenario("Store Certificates")
    .feed(testpersonnummer)
    .repeat(10) {
      exec(session => session.set("intygsId", UUID.randomUUID().toString()))
        .exec(http("Register Certificate 3.0")
          .post("/register-certificate/v3.0")
          .headers(Headers.store_certificate)
          .body(ELFileBody("request-bodies/register-medical-certificate.xml"))
          .check(status.is(200)))
    }
    .pause(1 seconds)
    .exec(Utils.purgeQueue)

  //setUp(scn.inject(rampUsers(100) over (30 seconds)).throttle(reachRps(20) in (20 seconds), holdFor(10 minutes)).protocols(Conf.httpConf))

  setUp(scn.inject(rampUsers(100) over (120 seconds)).protocols(Conf.httpConf))

}
