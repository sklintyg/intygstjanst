package se.inera.intygstjanst.simulations

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.ELFileBody
import se.inera.intygstjanst.util.{Conf, Headers, Utils}

import scala.concurrent.duration._

class ListActiveSickLeavesForCareUnit extends Simulation {

  val numberOfUsers = 100

  val testenheter = csv("intyg.csv").circular
  val intyg = csv("intyg.csv").records

  before {
    Utils.clean()
  }

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
          .body(ELFileBody("register-active-medical-certificate.xml")))
    }

  val scn = scenario("List Active Sick Leaves For Care Unit")
    .feed(testenheter)
    .repeat(10) {
      exec(http("List active sick leaves for ${enhetsId}")
        .post("/list-active-sick-leaves-for-care-unit/v1.0")
        .headers(Headers.list_active_sick_leaves)
        .body(ELFileBody("list-active-sick-leaves.xml"))
        .check(status.is(200),
          // Response should contain intygsData elements if successful
          substring(":intygsData>")))
  }

  setUp(preload.inject(atOnceUsers(1)).protocols(Conf.httpConf),
      scn.inject(nothingFor(5 seconds), rampUsers(numberOfUsers) over (30 seconds)).protocols(Conf.httpConf))

  after {
    Utils.clean()
  }
}

