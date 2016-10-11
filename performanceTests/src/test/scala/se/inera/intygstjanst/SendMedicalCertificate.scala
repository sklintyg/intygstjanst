package se.inera.intygstjanst
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.core.session._
import io.gatling.http.request.Body
import scala.concurrent.duration._
import collection.mutable.{ HashMap, MultiMap, Set }
import java.util.UUID

class SendMedicalCertificate extends Simulation {

  val numberOfUsers = 100

  val testpersonnummer = csv("data/intyg.csv").circular
  val intyg = csv("data/intyg.csv").records

  val scn = scenario("Send Medical Certificates")
    .feed(testpersonnummer)
    //Give consent for current user
    .exec(Utils.consent)
    .exec(http("Send certificate ${intygsId} for user ${personNr}")
      .post("/send-certificate/v1.0")
      .headers(Headers.send_medical_certificate)
      .body(ELFileBody("request-bodies/send-medical-certificate.xml"))
      .check(
        status.is(200)))

  val preload = scenario("Preload database")
    .foreach(intyg, "record") {
      exec(flattenMapIntoAttributes("${record}"))
        .exec(http("Store cert")
          .post("/register-certificate/v3.0")
          .headers(Headers.store_certificate)
          .body(ELFileBody("request-bodies/register-medical-certificate.xml")))
    }

  setUp(preload.inject(atOnceUsers(1)).protocols(Conf.httpConf),
    scn.inject(rampUsers(numberOfUsers) over (120 seconds)).protocols(Conf.httpConf))
}

