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

  val testpersonnummer = csv("data/testpersonnummerSkatteverket.csv").circular

  def luse = exec(http("Register LUSE certificate ${intygsId} for user ${personNr}")
          .post("/register-certificate-se/v3.0")
          .headers(Headers.register_certificate)
          .body(ELFileBody("request-bodies/register-luse.xml"))
          .check(
            status.is(200)))

  def lisjp = exec(http("Register LISJP certificate ${intygsId} for user ${personNr}")
          .post("/register-certificate-se/v3.0")
          .headers(Headers.register_certificate)
          .body(ELFileBody("request-bodies/register-lisjp.xml"))
          .check(
                  status.is(200)))

  val scn = scenario("Register Certificates")
    .feed(testpersonnummer)
    .exec(session => session.setAll(
        "personNr" -> session("personNr").as[String].replace("-",""),
        "intygsId" -> UUID.randomUUID()
        ))
    .uniformRandomSwitch(
      lisjp,
      luse)

  setUp(scn.inject(rampUsers(numberOfUsers) over (120 seconds)).protocols(Conf.httpConf))

}

