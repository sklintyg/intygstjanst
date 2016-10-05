package se.inera.intygstjanst
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.core.session._
import io.gatling.http.request.Body
import scala.concurrent.duration._
import collection.mutable.{ HashMap, MultiMap, Set }
import java.util.UUID

class RegisterCertificate extends Simulation {

  val numberOfUsers = 100

  val testpersonnummer = csv("data/intyg.csv").circular
  val intyg = csv("data/intyg.csv").records

  def luse = exec(http("Register LUSE certificate ${intygsId} for user ${personNr}")
          .post("/register-certificate-se/v2.0")
          .headers(Headers.register_certificate)
          .body(ELFileBody("request-bodies/register-luse.xml"))
          .check(
            status.is(200)))

  def lisu = exec(http("Register LISU certificate ${intygsId} for user ${personNr}")
          .post("/register-certificate-se/v2.0")
          .headers(Headers.register_certificate)
          .body(ELFileBody("request-bodies/register-lisu.xml"))
          .check(
                  status.is(200)))

  val scn = scenario("Register Certificates")
    .feed(testpersonnummer)
    //Give consent for current user
    .exec(Utils.consent)
    .uniformRandomSwitch(
      lisu,
      luse)

  setUp(scn.inject(rampUsers(numberOfUsers) over (120 seconds)).protocols(Conf.httpConf))

  after {
    var result = Utils.clean()
    println(if (result.code == 200) "Succesfully cleaned database" else "Error while cleaning")
  }
}

