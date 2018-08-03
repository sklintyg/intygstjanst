package se.inera.intygstjanst

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

object Conf {

  val httpConf = http
    .baseURL(Utils.baseUrl)
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate")
    .connection("keep-alive")

}