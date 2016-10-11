package se.inera.intygstjanst
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.request.Body
import scala.concurrent.duration._
import java.util.UUID

class ListCertificates extends Simulation {

  val numberOfUsers = 100

  val testpersonnummer = csv("data/intyg.csv").circular
  val intyg = csv("data/intyg.csv").records

  val preload = scenario("Preload database")
    .foreach(intyg, "record") {
      exec(flattenMapIntoAttributes("${record}"))
        .exec(http("Store cert")
          .post("/register-certificate/v3.0")
          .headers(Headers.store_certificate)
          .body(ELFileBody("request-bodies/register-medical-certificate.xml")))
    }

  val scn = scenario("List Certificates")
    .feed(testpersonnummer)
    //Give consent for current user
    .exec(Utils.consent)
    .exec(http("List certificates for ${personNr}")
      .post("/list-certificates/v1.0")
      .headers(Headers.list_certificates)
      .body(ELFileBody("request-bodies/list-certificates.xml"))
      .check(
        status.is(200),
        // Response should contain a certificateId element if successful
        substring("<ns2:certificateId>")))
    .pause(2 seconds)

  setUp(preload.inject(atOnceUsers(1)).protocols(Conf.httpConf),
      scn.inject(rampUsers(numberOfUsers) over (120 seconds)).protocols(Conf.httpConf))
}

