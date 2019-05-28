package se.inera.intygstjanst.util

import io.gatling.core.session.ExpressionWrapper
import io.gatling.http.Predef.http

object Conf {

  val httpConf = http
    .baseURL(Utils.baseUrl)
    .acceptHeader(ExpressionWrapper("*/*").expression)
    .acceptEncodingHeader(ExpressionWrapper("gzip, deflate").expression)
    .connection(ExpressionWrapper("keep-alive").expression)

}
