# Intygstjänsten
Intygstjänsten är den underliggande tjänsten för [Mina intyg](https://github.com/sklintyg/minaintyg), [Webcert](https://github.com/sklintyg/webcert) och [Statistik](https://github.com/sklintyg/statistik).

## Kom igång
Här hittar du grundläggande instruktioner för hur man kommer igång med projektet. Mer detaljerade instruktioner för att sätta upp sin utvecklingsmiljö och liknande hittar du på projektets [Wiki för utveckling](https://github.com/sklintyg/common/wiki).

### Bygg projektet
Intygstjänsten byggs med hjälp av Gradle enligt följande:

    $ git clone https://github.com/sklintyg/intygstjanst.git
    $ cd intygstjanst
    $ ./gradlew build

### Starta webbapplikationen
Webbapplikationen kan startas med Jetty enligt följande:

    $ ./gradlew appRun

För att starta applikationen i debugläge används:

    $ ./gradlew appRunDebug

Applikationen kommer då att starta upp med debugPort = **5005**. Det är denna port du ska använda när du sätter upp din debug-konfiguration i din utvecklingsmiljö.

Bas-URL för webbtjänsten är: [http://localhost:8080/inera-certificate](http://localhost:8080/inera-certificate/) 

### Spring Profiler

| Profilnamn | Beskrivning |
| :------------ | :----------- |
| dev | Generellt för exekvering i dev miljö, används även i infra |
| embedded | Laddar databasen med testdata och aktiverar H2 webbgränssnitt |
| testability-api | Aktiverar APIer för integrationstester (restAssuredTest) |
| caching-enabled | Aktiverar cache, se infra och redis-cache |
| it-fk-stub | Aktiverar NTJP stubbar, om denna inte anges måste NTJP-integration konfigureras   | 
| wc-hsa-stub | Aktiverar HSA stubbar, se infra och hsa-integration |

Aktiverade profiler i dev: `dev,embedded,testability-api,caching-enabled,it-fk-stub,wc-hsa-stub`


### Visa databasen
H2-databasens webbgränssnitt nås med: [http://localhost:8082/](http://localhost:8082/)

JBDC URL: `jdbc:h2:mem:dataSource`

User Name: `sa`

Lämna Password fältet tomt.


### Kör RestAssured
RestAssured körs mot en lokal instans av Intygstjänsten via:

    $ ./gradlew restAssuredTest

## Licens
Copyright (C) 2017 Inera AB (http://www.inera.se)

Webcert is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

Webcert is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.

Se även [LICENSE.md](https://github.com/sklintyg/common/blob/master/LICENSE.md). 
