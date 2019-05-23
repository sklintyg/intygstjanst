package se.inera.intygstjanst.simulations

import java.util.UUID

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.ELFileBody
import se.inera.intygstjanst.util.{Conf, Headers}

import scala.concurrent.duration._

class RegisterCertificate extends Simulation {

  val numberOfUsers = 100

  val testpersonnummer = csv("testpersonnummerSkatteverket.csv").circular

  def luse = exec(http("Register LUSE Certificate 3.0")
          .post("/register-certificate-se/v3.0")
          .headers(Headers.register_certificate)
          .body(ELFileBody("register-luse.xml"))
          .check(status.is(200)))

  def lisjp = exec(http("Register LISJP Certificate 3.0")
          .post("/register-certificate-se/v3.0")
          .headers(Headers.register_certificate)
          .body(ELFileBody("register-lisjp.xml"))
          .check(status.is(200)))

  val scn = scenario("Register Certificates")
    .feed(testpersonnummer)
    .exec(session => session.setAll(
        "personNr" -> session("personNr").as[String].replace("-",""),
        "intygsId" -> UUID.randomUUID()
        ))
    .uniformRandomSwitch(lisjp, luse)

  setUp(scn.inject(rampUsers(numberOfUsers) over (120 seconds)).protocols(Conf.httpConf))

}

