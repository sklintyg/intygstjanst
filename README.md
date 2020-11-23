# Intygstjänsten
Intygstjänsten är den underliggande tjänsten för [Mina intyg](https://github.com/sklintyg/minaintyg), [Webcert](https://github.com/sklintyg/webcert) och [Statistik](https://github.com/sklintyg/statistik).

## Kom igång
Här hittar du grundläggande instruktioner för hur man kommer igång med projektet. Mer detaljerade instruktioner för att sätta upp sin utvecklingsmiljö och liknande hittar du på projektets [Wiki för utveckling](https://github.com/sklintyg/common/wiki) samt [devops/develop README-filen](https://github.com/sklintyg/devops/tree/release/2021-1/develop/README.md)


Bas-URL för webbtjänsten är: http://localhost:8080/inera-certificate

### Spring Profiler

| Profilnamn | Beskrivning |
| :------------ | :----------- |
| dev | Generellt för exekvering i dev miljö, används även i infra |
| bootstrap | Laddar databasen med testdata |
| testability-api | Aktiverar APIer för integrationstester (restAssuredTest) |
| caching-enabled | Aktiverar cache, se infra och redis-cache |
| it-fk-stub | Aktiverar NTJP stubbar, om denna inte anges måste NTJP-integration konfigureras   | 
| wc-hsa-stub | Aktiverar HSA stubbar, se infra och hsa-integration |

Aktiverade profiler i dev: `dev,bootstrap,testability-api,caching-enabled,it-fk-stub,wc-hsa-stub`

## Licens
Copyright (C) 2021 Inera AB (http://www.inera.se)

Intygstjänst is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

Intygstjänst is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.

Se även [LICENSE.md](LICENSE.md). 
