# Intygstjänsten
Intygstjänsten är den underliggande tjänsten för [Mina intyg](https://github.com/sklintyg/minaintyg), [Webcert](https://github.com/sklintyg/webcert) och [Statistik](https://github.com/sklintyg/statistik).

## Kom igång
Här hittar du grundläggande instruktioner för hur man kommer igång med projektet. Mer detaljerade instruktioner för att sätta upp sin utvecklingsmiljö och liknande hittar du på projektets [Wiki för utveckling](https://github.com/sklintyg/common/wiki).

### Bygg projektet
Intygstjänsten byggs med hjälp av Maven enligt följande:
```
$ git clone https://github.com/sklintyg/intygstjanst.git

$ cd intygstjanst
$ mvn install
```

### Starta webbapplikationen
Webbapplikationen kan startas med Jetty enligt följande:
```
$ cd web
$ mvn jetty:run
$ open http://localhost:8080/inera-certificate/
```

### Visa databasen
Man kan även komma åt H2-databasen som startas:
```
$ open http://localhost:8082/
```

### Kör FitNesse
För att köra FitNesse-testerna måste man starta FitNesse wiki:
```
$ cd ../specifications
$ mvn verify -Pwiki
$ open http://localhost:9123/
```

## Licens
Copyright (C) 2014 Inera AB (http://www.inera.se)

Webcert is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

Webcert is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.

Se även [LICENSE.md](https://github.com/sklintyg/common/blob/master/LICENSE.md). 