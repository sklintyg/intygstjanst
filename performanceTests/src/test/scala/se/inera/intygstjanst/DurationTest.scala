/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intygstjanst

import io.gatling.core.Predef._
import scala.concurrent.duration._

class DurationTest extends Simulation {

  val store = new StoreCertificates
  val register = new RegisterCertificate
  val send = new SendMedicalCertificate
  val get = new GetCertificates

  setUp(
    store.scn.inject(rampUsers(10) over (60 seconds),
      constantUsersPerSec(2) during(3 minutes) randomized,
      rampUsersPerSec(1) to 4 during (10 minutes), // from easy to hard, scale up to at least two replicas
      constantUsersPerSec(3) during(3 minutes) randomized, // keep on peak 1
      constantUsersPerSec(1) during(10 minutes), // easy, scale down to one pod
      rampUsersPerSec(1) to 3 during (10 minutes), // ramp up again
      constantUsersPerSec(3) during(3 minutes) randomized // keep on peak 2
    ).protocols(Conf.httpConf),

    register.scn.inject(rampUsers(10) over (60 seconds),
      constantUsersPerSec(2) during(3 minutes) randomized,
      rampUsersPerSec(1) to 4 during (10 minutes), // from easy to hard, scale up to at least two replicas
      constantUsersPerSec(3) during(3 minutes) randomized, // keep on peak 1
      constantUsersPerSec(1) during(10 minutes), // easy, scale down to one pod
      rampUsersPerSec(1) to 3 during (10 minutes), // ramp up again
      constantUsersPerSec(3) during(3 minutes) randomized // keep on peak 2
    ).protocols(Conf.httpConf),

    send.scn.inject(
      nothingFor(60 seconds),
      rampUsers(5) over (60 seconds),
      constantUsersPerSec(2) during(30 minutes) randomized
    ).protocols(Conf.httpConf),

    get.scn.inject(
      nothingFor(60 seconds),
      rampUsers(5) over (60 seconds),
      constantUsersPerSec(2) during(30 minutes) randomized
    ).protocols(Conf.httpConf)
  )

  before {
    println("Preparing database....please be patient!")
    Utils.clean()
    Utils.preloadDatabase()
  }

}
