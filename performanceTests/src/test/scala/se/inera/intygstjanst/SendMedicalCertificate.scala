package se.inera.intygstjanst
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.core.session._
import io.gatling.http.request.Body
import scala.concurrent.duration._
import collection.mutable.{ HashMap, MultiMap, Set }

class SendMedicalCertificate extends Simulation {

  val numberOfUsers = 100

  val testpersonnummer = csv("data/intyg.csv").circular

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

