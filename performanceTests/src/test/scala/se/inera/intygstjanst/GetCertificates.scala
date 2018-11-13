package se.inera.intygstjanst
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.core.session._
import io.gatling.http.request.Body
import scala.concurrent.duration._
import collection.mutable.{ HashMap, MultiMap, Set }

class GetCertificates extends Simulation {

  val numberOfUsers = 100

  val testpersonnummer = csv("data/intyg.csv").circular

  val scn = scenario("Get Certificates")
    .feed(testpersonnummer)
    .exec(http("Get Certificate 1.0")
      .post("/get-certificate/v1.0")
      .headers(Headers.get_certificate)
      .body(ELFileBody("request-bodies/get-certificate.xml"))
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

