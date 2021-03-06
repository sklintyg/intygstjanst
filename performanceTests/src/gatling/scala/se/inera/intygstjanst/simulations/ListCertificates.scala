package se.inera.intygstjanst.simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.ELFileBody
import se.inera.intygstjanst.util.{Conf, Headers, Utils}

import scala.concurrent.duration._


class ListCertificates extends Simulation {

  val numberOfUsers = 100

  val testpersonnummer = csv("intyg.csv").circular

  val scn = scenario("List Certificates")
    .feed(testpersonnummer)
    .exec(http("List certificates for ${personNr}")
      .post("/list-certificates/v1.0")
      .headers(Headers.list_certificates)
      .body(ELFileBody("list-certificates.xml"))
      .check(
        status.is(200),
        // Response should contain a certificateId element if successful
        substring("<ns2:certificateId>")))
    .pause(2 seconds)

  before {
    println("Setting up database before execution.")
    Utils.clean()
    Utils.preloadDatabase()
  }

  setUp(scn.inject(nothingFor(5 seconds), rampUsers(numberOfUsers) over (120 seconds)).protocols(Conf.httpConf))

  after {
    println("Cleaning up after tests.")
    Utils.clean()
  }
}

