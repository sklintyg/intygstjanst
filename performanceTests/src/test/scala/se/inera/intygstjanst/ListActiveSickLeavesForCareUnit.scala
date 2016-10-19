package se.inera.intygstjanst
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.request.Body
import scala.concurrent.duration._
import java.util.UUID
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ListActiveSickLeavesForCareUnit extends Simulation {

  val numberOfUsers = 100

  val testenheter = csv("data/intyg.csv").circular
  val intyg = csv("data/intyg.csv").records

  val preload = scenario("Preload database")
    .foreach(intyg, "record") {
      exec(flattenMapIntoAttributes("${record}"))
        .exec(session => session.setAll(
          "varaktighetFrom" -> LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
          "varaktighetTom" -> LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
          ))
        .exec(http("Store cert")
          .post("/register-certificate/v3.0")
          .headers(Headers.store_certificate)
          .body(ELFileBody("request-bodies/register-active-medical-certificate.xml")))
    }

  val scn = scenario("List Active Sick Leaves For Care Unit")
    .feed(testenheter)
    .exec(http("List active sick leaves for ${enhetsId}")
      .post("/list-active-sick-leaves-for-care-unit/v1.0")
      .headers(Headers.list_active_sick_leaves)
      .body(ELFileBody("request-bodies/list-active-sick-leaves.xml"))
      .check(
        status.is(200),
        // Response should contain intygsData elements if successful
        substring(":intygsData>")))

  setUp(preload.inject(atOnceUsers(1)).protocols(Conf.httpConf),
      scn.inject(rampUsers(numberOfUsers) over (120 seconds)).protocols(Conf.httpConf))

}

