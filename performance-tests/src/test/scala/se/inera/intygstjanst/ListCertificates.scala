package se.inera.intygstjanst
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.request.Body
import scala.concurrent.duration._
import java.util.UUID


class ListCertificates extends Simulation {

  val numberOfUsers = 100

  val testpersonnummer = csv("data/testpersonnummer_skatteverket.cvs").circular

  val scn = scenario("List Certificates")
    .feed(testpersonnummer)
      .exec(http("Give consent for ${personNr}")
        .post("/set-consent/v1.0")
          .headers(Headers.set_consent)
          .body(ELFileBody("request-bodies/set-consent.xml"))
          .check(status.is(200))
      )
      .exec(http("List certificates for ${personNr}")
        .post("/list-certificates/v1.0")
          .headers(Headers.list_certificates)
          .body(ELFileBody("request-bodies/list-certificates.xml"))
          .check(
              status.is(200),
              // Response should contain a certificateId element if successful
              substring("<ns2:certificateId>")
          )
      )
    .pause(2 seconds)

  setUp(scn.inject(rampUsers(numberOfUsers) over (120 seconds)).protocols(Conf.httpConf))

}

