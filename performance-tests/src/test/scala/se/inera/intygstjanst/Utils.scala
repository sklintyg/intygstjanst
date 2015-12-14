package se.inera.intygstjanst
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.concurrent.duration._
import collection.mutable.{ HashMap, MultiMap, Set }

import java.util.UUID

import scalaj.http._

object Utils {

  val baseUrl = System.getProperty("baseUrl", "http://localhost:8080" )

  val consent = exec(http("Give consent for ${personNr}")
        .post("/set-consent/v1.0")
          .headers(Headers.set_consent)
          .body(ELFileBody("request-bodies/set-consent.xml"))
          .check(status.is(200)));
  
  def storeWithCorrelation() = { 
    exec(session=>session.set("intygsId", UUID.randomUUID().toString()))
    .exec(http("Store certificates for ${personNr}")
      .post("/register-certificate/v3.0")
      .headers(Headers.store_certificate)
      .body(ELFileBody("request-bodies/register-medical-certificate.xml"))
      .check(status.is(200)))
  }

  def clean() : HttpResponse[String] = {
    var url = baseUrl + "/resources/certificate" 
    Http(url)
     .method("delete")
     .header("content-type", "application/json")
     .asString
  }
}
