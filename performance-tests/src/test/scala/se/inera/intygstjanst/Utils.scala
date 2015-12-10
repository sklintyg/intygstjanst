package se.inera.intygstjanst
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.concurrent.duration._
import java.util.UUID

object Utils {

  val testpersonnummer = csv("data/testpersonnummer_skatteverket.cvs").circular

  val store = scenario("Bootstrap database")
      .feed(testpersonnummer)
      .repeat(10) {
        exec(session=>session.set("utlatandeId", UUID.randomUUID().toString()))
        .exec(http("Store certificate ${personNr}")
          .post("/register-certificate/v3.0")
            .headers(Headers.store_certificate)
            .body(ELFileBody("request-bodies/register-medical-certificate.xml"))
            .check(status.is(200)))
      }
   .pause(2 seconds)

   val clean = exec(
       http("Clean database")
       .delete("/resources/certificate")
       .headers(Headers.json)
       .check(status.is(200)))
}