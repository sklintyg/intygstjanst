package se.inera.intygstjanst

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.concurrent.duration._
import scala.io.Source._
import collection.mutable.{ HashMap, MultiMap, Set }
import scala.xml.XML

import java.util.UUID

import scalaj.http._

object Utils {

  val baseUrl = System.getProperty("baseUrl", "http://localhost:8080/inera-certificate" )

  val consent = exec(http("Set Consent 1.0")
        .post("/set-consent/v1.0")
          .headers(Headers.set_consent)
          .body(ELFileBody("request-bodies/set-consent.xml"))
          .check(status.is(200)));

  // purges message queue (to statistics), otherwise the app hangs when the queue is full.
  def purgeQueue = exec(http("Purge ST queue")
    .get("/resources/statisticsresource/purge")
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
    this.purgeQueue
    var url = baseUrl + "/resources/certificate"
    Http(url)
     .method("delete")
     .header("content-type", "application/json")
     .asString
  }

  def clean() = {
    this.purgeQueue
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

  def preloadDatabase(): Unit = {
    val bufferedSource = fromFile("src/test/resources/data/intyg.csv")
    for (line <- bufferedSource.getLines) {
      val cols = line.split(",").map(_.trim)
      storeCertificate(cols(0), cols(1))
    }
    bufferedSource.close
  }

  def storeCertificate(intygsId: String, personNr: String) = {
    var url = baseUrl + "/register-certificate/v3.0"
    val origXml = XML.loadFile("src/test/resources/request-bodies/register-medical-certificate.xml").toString()
    Http(url)
      .postData(
        s"""
           |<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
           |   xmlns:add="http://www.w3.org/2005/08/addressing"
           |   xmlns:urn="urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificateResponder:3"
           |   xmlns:urn1="urn:riv:insuranceprocess:healthreporting:mu7263:3"
           |   xmlns:urn2="urn:riv:insuranceprocess:healthreporting:2">
           |   <soapenv:Header>
           |      <add:To>?</add:To>
           |   </soapenv:Header>
           |   <soapenv:Body>
           |      <urn:RegisterMedicalCertificate>
           |  <urn:lakarutlatande>
           |        <urn1:lakarutlatande-id>${intygsId}</urn1:lakarutlatande-id>
           |      <urn1:typAvUtlatande>Läkarintyg enligt 3 kap, 8 § lagen (1962:381) om allmän försäkring</urn1:typAvUtlatande>
           |      <urn1:kommentar>Har haft mailkontakt med patientenPrognosen att återgå till arbete är svår att bedömma förrän utredningen är genomförd.</urn1:kommentar>
           |      <urn1:signeringsdatum>2013-03-17T00:00:00</urn1:signeringsdatum>
           |      <urn1:skickatDatum>2013-03-17T00:00:00</urn1:skickatDatum>
           |      <urn1:patient>
           |        <urn2:person-id root="1.2.752.129.2.1.3.1" extension="${personNr}"/>
           |        <urn2:fullstandigtNamn>Test Testorsson</urn2:fullstandigtNamn>
           |      </urn1:patient>
           |      <urn1:skapadAvHosPersonal>
           |        <urn2:personal-id root="1.2.752.129.2.1.4.1" extension="Personal HSA-ID"/>
           |        <urn2:fullstandigtNamn>En Läkare</urn2:fullstandigtNamn>
           |        <urn2:forskrivarkod>1234567</urn2:forskrivarkod>
           |        <urn2:enhet>
           |          <urn2:enhets-id root="1.2.752.129.2.1.4.1" extension="Enhetsid"/>
           |          <urn2:arbetsplatskod root="1.2.752.29.4.71" extension="123456789011"/>
           |          <urn2:enhetsnamn>Kir Mott</urn2:enhetsnamn>
           |          <urn2:postadress>Lasarettvägen 13</urn2:postadress>
           |          <urn2:postnummer>85150</urn2:postnummer>
           |          <urn2:postort>Sundsvall</urn2:postort>
           |          <urn2:telefonnummer>060-8188000</urn2:telefonnummer>
           |          <urn2:vardgivare>
           |            <urn2:vardgivare-id root="1.2.752.129.2.1.4.1" extension="VardgivarId"/>
           |            <urn2:vardgivarnamn>Landstinget Norrland</urn2:vardgivarnamn>
           |          </urn2:vardgivare>
           |        </urn2:enhet>
           |      </urn1:skapadAvHosPersonal>
           |      <urn1:vardkontakt>
           |        <urn1:vardkontakttyp>Min_undersokning_av_patienten</urn1:vardkontakttyp>
           |        <urn1:vardkontaktstid>2013-03-17</urn1:vardkontaktstid>
           |      </urn1:vardkontakt>
           |      <urn1:vardkontakt>
           |        <urn1:vardkontakttyp>Min_telefonkontakt_med_patienten</urn1:vardkontakttyp>
           |        <urn1:vardkontaktstid>2013-03-10</urn1:vardkontaktstid>
           |      </urn1:vardkontakt>
           |      <urn1:referens>
           |        <urn1:referenstyp>Journaluppgifter</urn1:referenstyp>
           |        <urn1:datum>2013-03-10</urn1:datum>
           |      </urn1:referens>
           |      <urn1:referens>
           |        <urn1:referenstyp>Annat</urn1:referenstyp>
           |        <urn1:datum>2013-03-10</urn1:datum>
           |      </urn1:referens>
           |      <urn1:aktivitet>
           |        <urn1:aktivitetskod>Ovrigt</urn1:aktivitetskod>
           |        <urn1:beskrivning>När skadan förbättrats rekommenderas muskeluppbyggande sjukgymnastik</urn1:beskrivning>
           |      </urn1:aktivitet>
           |      <urn1:bedomtTillstand>
           |        <urn1:beskrivning>Patienten klämde höger överarm vid olycka i hemmet. Problemen har pågått en längre tid.
           |        </urn1:beskrivning>
           |      </urn1:bedomtTillstand>
           |      <urn1:medicinsktTillstand>
           |        <urn1:beskrivning>Klämskada på överarm</urn1:beskrivning>
           |        <urn1:tillstandskod codeSystemName="ICD-10" code="S47"/>
           |      </urn1:medicinsktTillstand>
           |      <urn1:funktionstillstand>
           |        <urn1:beskrivning>Kraftigt nedsatt rörlighet i överarmen pga skadan. Böj- och sträckförmågan är mycket dålig.
           |          Smärtar vid rörelse vilket ger att patienten inte kan använda armen särkilt mycket.
           |        </urn1:beskrivning>
           |        <urn1:typAvFunktionstillstand>Kroppsfunktion</urn1:typAvFunktionstillstand>
           |      </urn1:funktionstillstand>
           |      <urn1:funktionstillstand>
           |        <urn1:beskrivning>Patienten bör/kan inte använda armen förrän skadan läkt. Skadan förvärras vid för tidigt
           |          påtvingad belastning. Patienten kan inte lyfta armen utan den ska hållas riktad nedåt och i fast läge så mycket
           |          som möjligt under tiden för läkning.
           |        </urn1:beskrivning>
           |        <urn1:typAvFunktionstillstand>Aktivitet</urn1:typAvFunktionstillstand>
           |        <urn1:arbetsformaga>
           |          <urn1:motivering>Skadan har förvärrats vid varje tillfälle patienten använt armen. Måste hållas i total stillhet
           |            tills läkningsprocessen kommit en bit på väg. Eventuellt kan utredning visa att operation är nödvändig för att
           |            läka skadan.
           |          </urn1:motivering>
           |          <urn1:prognosangivelse>Det_gar_inte_att_bedomma</urn1:prognosangivelse>
           |          <urn1:arbetsuppgift>
           |            <urn1:typAvArbetsuppgift>Dirigent
           |              Dirigerar en för större orkester på deltid
           |            </urn1:typAvArbetsuppgift>
           |          </urn1:arbetsuppgift>
           |          <urn1:arbetsformagaNedsattning>
           |            <urn1:varaktighetFrom>2013-03-17</urn1:varaktighetFrom>
           |            <urn1:varaktighetTom>2013-04-07</urn1:varaktighetTom>
           |            <urn1:nedsattningsgrad>Helt_nedsatt</urn1:nedsattningsgrad>
           |          </urn1:arbetsformagaNedsattning>
           |          <urn1:arbetsformagaNedsattning>
           |            <urn1:varaktighetFrom>2013-04-08</urn1:varaktighetFrom>
           |            <urn1:varaktighetTom>2013-04-15</urn1:varaktighetTom>
           |            <urn1:nedsattningsgrad>Nedsatt_med_1/4</urn1:nedsattningsgrad>
           |          </urn1:arbetsformagaNedsattning>
           |          <urn1:arbetsformagaNedsattning>
           |            <urn1:varaktighetFrom>2013-04-16</urn1:varaktighetFrom>
           |            <urn1:varaktighetTom>2013-04-23</urn1:varaktighetTom>
           |            <urn1:nedsattningsgrad>Nedsatt_med_1/2</urn1:nedsattningsgrad>
           |          </urn1:arbetsformagaNedsattning>
           |          <urn1:arbetsformagaNedsattning>
           |            <urn1:varaktighetFrom>2013-04-24</urn1:varaktighetFrom>
           |            <urn1:varaktighetTom>2013-05-01</urn1:varaktighetTom>
           |            <urn1:nedsattningsgrad>Nedsatt_med_3/4</urn1:nedsattningsgrad>
           |          </urn1:arbetsformagaNedsattning>
           |          <urn1:sysselsattning>
           |            <urn1:typAvSysselsattning>Nuvarande_arbete</urn1:typAvSysselsattning>
           |          </urn1:sysselsattning>
           |        </urn1:arbetsformaga>
           |      </urn1:funktionstillstand>
           |
           |  </urn:lakarutlatande>
           |         <!--You may enter ANY elements at this point-->
           |      </urn:RegisterMedicalCertificate>
           |   </soapenv:Body>
           |</soapenv:Envelope>
         """.stripMargin.replaceAll("\n", " "))
      .method("post")
      .option(HttpOptions.allowUnsafeSSL)
      .option(HttpOptions.connTimeout(5000))
      .option(HttpOptions.readTimeout(5000))
      .header("Content-type", "application/xml").asString.code
  }
}
