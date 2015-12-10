# Intygstjänsten prestandatest

## Målmiljö
Primärt är det QA-miljön hos Basefarm som prestandatesterna skall exekveras mot. Tanken är att en aktuell anonymiserad produktionsdump skall läsas in i databasen.
 
Sekundärt kan man mycket väl köra prestandatesterna mot lokal maskin för att identifiera hotspots eller problem i ett tidigt skede. Vi är dock lite begränsade av vilka stubbar som är aktiva.

## Konfiguration och loggning
Om man vill finjustera hur gatling beter sig så finns följande konfigurationsfiler:

- src/test/resources/gatling.conf
- src/test/resources/logback-test.xml

Det man ofta vill komma åt är felloggar när ens tester börjar spruta ur sig 500 Server Error eller påstår att de inte kan parsa ut saker ur svaren. Öppna då logback-test.xml och kommentera in följande:

    <!-- Uncomment for logging ALL HTTP request and responses  -->
    <!-- <logger name="io.gatling.http" level="TRACE" />    -->
    <!-- Uncomment for logging ONLY FAILED HTTP request and responses -->
    <!-- <logger name="io.gatling.http" level="DEBUG" /> -->    
 
Som framgår ovan så kan man slå på antingen all HTTP eller enbart failade request/responses. Ovärderligt då Gatling inte ger särskilt mycket hjälp annat än HTTP Status när något går fel på servern. 

## Seedning av testdata
I mappen src/test/resources finns en csv-filer med testpersonnummer från skatteverket. (ca 16000 st)

### SOAP-request
I mappen src/test/resources/request-bodies finns xml-filer med SOAP-requests för de olika testerna. Dessa används som templates men innehåller till störst del statisk information som inte är av intresse för testerna.

### StoreCertificates
Lagrar 1000 intyg, 10 per testperson, från csv-filen med personnummer.

- mvn test -DsimulationClass=se.inera.intygstjanst.StoreCertificates

### ListCertificates
Körs med fördel efter StoreCertificates så att det finns något att visa...
Lägger in medgivande och listar ut intyg för samma personnummer som StoreCertificates.

- mvn test -DsimulationClass=se.inera.intygstjanst.ListCertificates

### Radera testdata
Man kan även tömma databasen genom att köra:

- mvn test -DsimulationClass=se.inera.webcert.RemoveTestData

### Testpersonnummer
Testpersonnummer används primärt mot demo/QA eller annan miljö där PU-tjänstens testmiljö är aktiv.

Simulationen SkrivSigneraSkickaIntyg nyttjar cirkulärt dessa personnummer för att mata in olika patienter för varje 
iteration som skall författa och signera ett intyg.

## Hur startar jag en simulering

### Välj målmiljö
Ett alternativ är att öppna pom.xml och redigera element under <properties>

- <baseUrl>http://localhost:9088</baseUrl>

Alternativt kan man ange -DbaseUrl=....... på kommandoraden.

### Exekvering från command-line

Från command-line fungerar följande:

- Lägga in intyg i databasen
  mvn test -DsimulationClass=se.inera.intygstjanst.StoreCertificates
  
- Medgivande och lista intyg
  mvn test -DsimulationClass=se.inera.webcert.ListCertificates


## Hur följer jag upp utfallet?
Medan testet kör skriver Gatling ut lite progress-info på command-line men den ger ganska rudimentär information. Det intressanta är att titta på rapporterna som genereras efter att testerna slutförts. Dessa finns under mappen:

target/gatling/results

Varje körning hamnar i en egen mapp, t.ex.:

/results/listcertificates-14534525346 etc

och har en index.html-fil där utfallet av simulationen redovisas.