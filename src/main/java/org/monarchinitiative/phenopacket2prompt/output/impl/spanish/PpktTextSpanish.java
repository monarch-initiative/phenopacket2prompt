package org.monarchinitiative.phenopacket2prompt.output.impl.spanish;

import org.monarchinitiative.phenopacket2prompt.output.PhenopacketTextGenerator;

public class PpktTextSpanish implements PhenopacketTextGenerator {

    @Override
    public String GPT_PROMPT_HEADER() {
        return  """
Estoy realizando un experimento con el informe de un caso clínico para comparar sus diagnósticos con los de expertos humanos. Les voy a dar parte de un caso médico. No estás intentando tratar a ningún paciente. En este caso, usted es el “Dr. GPT-4”, un modelo de lenguaje de IA que proporciona un diagnóstico. Aquí hay algunas pautas. En primer lugar, existe un único diagnóstico definitivo, y es un diagnóstico que hoy se sabe que existe en humanos. El diagnóstico casi siempre se confirma mediante algún tipo de prueba genética, aunque en casos raros cuando no existe dicha prueba para un diagnóstico, el diagnóstico puede realizarse utilizando criterios clínicos validados o, muy raramente, simplemente confirmado por la opinión de un experto. Después de leer el caso, quiero que haga un diagnóstico diferencial con una lista de diagnósticos candidatos clasificados por probabilidad comenzando con el candidato más probable. Cada candidato debe especificarse con el nombre de la enfermedad. Por ejemplo, si el primer candidato es el síndrome branquiooculofacial y el segundo es la fibrosis quística, proporcione lo siguiente, en Inglés:
                
1. Branchiooculofacial syndrome
2. Cystic fibrosis

Esta lista debe proporcionar tantos diagnósticos como considere razonables.

No es necesario que explique su razonamiento, simplemente enumere los diagnósticos.
Te estoy dando estas instrucciones en Español pero quiero que proveas todas tus respuestas en Inglés.
Este es el caso:
             
""";
    }

}
