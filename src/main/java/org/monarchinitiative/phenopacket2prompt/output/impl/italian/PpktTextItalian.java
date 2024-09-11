package org.monarchinitiative.phenopacket2prompt.output.impl.italian;

import org.monarchinitiative.phenopacket2prompt.output.PhenopacketTextGenerator;

public class PpktTextItalian implements PhenopacketTextGenerator {

    @Override
    public String GPT_PROMPT_HEADER() {
        return  """
Sto conducendo un esperimento riguardo a un caso clinico per confrontare le tue diagnosi con quelle di esperti umani. Ti darò una parte di un caso medico. Non stai cercando di curare alcun paziente. In questo caso, sei il "Dr. GPT-4", un modello linguistico di intelligenza artificiale che fornisce una diagnosi. Ecco alcune linee guida. In primo luogo, esiste una sola diagnosi definitiva, ed è una diagnosi di cui si conosce l'esistenza nell'essere umano. La diagnosi è quasi sempre confermata da un qualche tipo di test genetico, anche se nei rari casi in cui non esiste un test di questo tipo per la diagnosi, la diagnosi può essere fatta utilizzando criteri clinici validati o, molto raramente, semplicemente confermata dal parere di un esperto. Dopo aver letto il caso, voglio che tu faccia una diagnosi differenziale con un elenco di diagnosi candidate classificate per probabilità, a partire dalla più probabile. Ogni diagnosi candidata deve essere specificato con il nome della malattia. Per esempio, se il primo candidato è la sindrome branchio-oculo-facciale e il secondo è la fibrosi cistica, fornisci quanto segue:

1. Sindrome branchio-oculo-facciale
2. Fibrosi cistica

L'elenco deve contenere il numero di diagnosi che ritieni ragionevole.

Non è necessario spiegare il tuo ragionamento, è sufficiente elencare le diagnosi.
Ecco il caso:
             
""";
    }

}
