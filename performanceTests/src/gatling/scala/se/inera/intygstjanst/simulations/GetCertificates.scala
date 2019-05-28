package se.inera.intygstjanst.simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.ELFileBody
import se.inera.intygstjanst.util.{Conf, Headers, Utils}

import scala.concurrent.duration._

class GetCertificates extends Simulation {

  val numberOfUsers = 100

  val testpersonnummer = csv("intyg.csv").circular

  val scn = scenario("Get Certificates")
    .feed(testpersonnummer)
    .exec(http("Get Certificate 1.0")
      .post("/get-certificate/v1.0")
      .headers(Headers.get_certificate)
      .body(ELFileBody("get-certificate.xml"))
      .check(
        status.is(200),
        substring("<ns2:certificateId>")))
    .pause(50 milliseconds)

  before {
    println("Setting up database before execution.")
    Utils.clean()
    Utils.preloadDatabase()
  }

  setUp(scn.inject(rampUsers(numberOfUsers) over (120 seconds)).protocols(Conf.httpConf))

  after {
    println("Cleaning up after tests.")
    Utils.clean()
  }
}

