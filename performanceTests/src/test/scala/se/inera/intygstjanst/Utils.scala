package se.inera.intygstjanst
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.concurrent.duration._
import scala.io.Source._
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

  // Use scalaj.http, so this function can be used without gatling DSL
  def cleanAll() : HttpResponse[String] = {
    var url = baseUrl + "/resources/certificate" 
    Http(url)
     .method("delete")
     .header("content-type", "application/json")
     .asString
  }

  def clean() = {
    val bufferedSource = fromFile("src/test/resources/data/intyg.csv")
    for (line <- bufferedSource.getLines) {
      val cols = line.split(",").map(_.trim)
      deleteCertificate(cols(0))
    }
    bufferedSource.close
  }

  def deleteCertificate(id : String) : HttpResponse[String] = {
    var url = baseUrl + "/resources/certificate/" + id
    Http(url)
     .method("delete")
     .header("content-type", "application/json")
     .asString
  }
}
