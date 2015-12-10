package se.inera.intygstjanst
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.concurrent.duration._
import java.util.UUID

class RemoveTestData extends Simulation{

   val clean = scenario("Remove testdata")
       .exec(http("Clean database")
         .delete("/resources/certificate")
         .headers(Headers.json)
         .check(status.is(200)))

  setUp(clean.inject(atOnceUsers(1)).protocols(Conf.httpConf))
}