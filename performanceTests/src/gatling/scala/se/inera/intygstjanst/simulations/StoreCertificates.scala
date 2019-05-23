package se.inera.intygstjanst.simulations

import java.util.UUID

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.ELFileBody
import se.inera.intygstjanst.util.{Conf, Headers}

import scala.concurrent.duration._

class StoreCertificates extends Simulation {

  val testpersonnummer = csv("testpersonnummerSkatteverket.csv").circular

  val scn = scenario("Store Certificates")
    .feed(testpersonnummer)
    .repeat(10) {
      exec(session => session.set("intygsId", UUID.randomUUID().toString()))
        .exec(http("Register Certificate 3.0")
          .post("/register-certificate/v3.0")
          .headers(Headers.store_certificate)
          .body(ELFileBody("register-medical-certificate.xml"))
          .check(status.is(200)))
    }
    .pause(1 seconds)

  setUp(scn.inject(rampUsers(100) over (120 seconds)).protocols(Conf.httpConf))
}
