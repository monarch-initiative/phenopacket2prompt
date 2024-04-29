package org.monarchinitiative.phenopacket2prompt.output.impl.dutch;

import org.monarchinitiative.phenopacket2prompt.output.PhenopacketTextGenerator;

public class PpktTextDutch implements PhenopacketTextGenerator {

    @Override
    public String QUERY_HEADER() {
        return  """
Ik doe een experiment met een klinisch verslag om te zien hoe jullie diagnoses zich verhouden tot die van menselijke experts. Ik geef je een deel van een medisch geval. Je probeert geen patiënten te behandelen. In dit geval ben je "Dr. GPT-4", een AI-taalmodel dat een diagnose stelt. Hier zijn enkele richtlijnen. Ten eerste is er één definitieve diagnose en dat is een diagnose waarvan bekend is dat die bij mensen bestaat. De diagnose wordt bijna altijd bevestigd door een of andere genetische test, maar in zeldzame gevallen waarin zo'n test niet bestaat voor een diagnose, kan de diagnose in plaats daarvan worden gesteld op basis van gevalideerde klinische criteria of heel zelden gewoon worden bevestigd door de mening van een expert. Nadat je de casus hebt gelezen, wil ik dat je een differentiaaldiagnose stelt met een lijst van mogelijke diagnoses gerangschikt naar waarschijnlijkheid, te beginnen met de meest waarschijnlijke kandidaat. Elke kandidaat moet gespecificeerd worden met de OMIM identifier en de naam van de ziekte. Bijvoorbeeld, als de eerste kandidaat het Branchiooculofaciaal syndroom is en de tweede Cystic fibrosis, geef dan dit:

1. OMIM:113620 - Branchiooculofaciaal syndroom
2. OMIM:219700 - Taaislijmziekte

Deze lijst moet zoveel diagnoses bevatten als je redelijk acht.

Je hoeft je redenering niet uit te leggen, je hoeft alleen de diagnoses samen met de OMIM-identifiers op te sommen. Dit is het geval:
""";
    }

}
