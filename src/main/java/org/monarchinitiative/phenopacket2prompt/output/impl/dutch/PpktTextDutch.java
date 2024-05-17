package org.monarchinitiative.phenopacket2prompt.output.impl.dutch;

import org.monarchinitiative.phenopacket2prompt.output.PhenopacketTextGenerator;

public class PpktTextDutch implements PhenopacketTextGenerator {

    @Override
    public String QUERY_HEADER() {
        return  """
Ik voer een experiment uit op basis van een klinisch casusrapport om te zien hoe jouw diagnoses zich verhouden tot die van menselijke experts. Ik ga je een deel van een medische casus geven. Je probeert geen patiënten te behandelen. In dit geval ben je “Dr. GPT-4”, een AI-taalmodel dat een diagnose stelt. Hier zijn enkele richtlijnen. Ten eerste bestaat er één definitieve diagnose, en het is een diagnose waarvan tegenwoordig bekend is dat deze ook bij mensen voorkomt. De diagnose wordt bijna altijd bevestigd door een soort genetische test, hoewel in zeldzame gevallen, wanneer een dergelijke test niet bestaat voor een diagnose, de diagnose in plaats daarvan kan worden gesteld op basis van gevalideerde klinische criteria of zeer zelden alleen maar kan worden bevestigd door de mening van deskundigen. Nadat je de casus hebt gelezen, wil ik dat je een differentiële diagnose geeft met een lijst met kandidaat-diagnoses, gerangschikt op waarschijnlijkheid, te beginnen met de meest waarschijnlijke kandidaat. Elke kandidaat moet worden gespecificeerd met de ziektenaam. Als de eerste kandidaat bijvoorbeeld het Branchio-oculofaciaal syndroom is en de tweede cystische fibrose, geef het dan zo in het Engels weer:

1. Branchiooculofacial syndrome
2. Cystic fibrosis

Deze lijst moet zoveel diagnoses bevatten als je redelijk acht.
Je hoeft je redenering niet uit te leggen, vermeld alleen de diagnoses.
Ik heb je deze instructies in het Nederlands gegeven, maar ik zou graag willen dat je je antwoord alleen in het Engels geeft.

Hier is het geval:
""";
    }

}
