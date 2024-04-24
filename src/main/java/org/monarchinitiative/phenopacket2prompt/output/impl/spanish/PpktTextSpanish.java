package org.monarchinitiative.phenopacket2prompt.output.impl.spanish;

import org.monarchinitiative.phenopacket2prompt.output.PhenopacketTextGenerator;

public class PpktTextSpanish implements PhenopacketTextGenerator {

    @Override
    public String QUERY_HEADER() {
        return  """
Estoy realizando un experimento en una conferencia de casos clinicopatológicos para ver cómo sus diagnósticos\s
se comparan con los de los expertos humanos. Les voy a dar parte de un caso médico. Estos han sido\s
todos han sido publicados en el New England Journal of Medicine. Usted no está tratando a ningún paciente.
Cuando lea el caso, observará que hay expertos que exponen sus opiniones.\s
En este caso, usted es el "Dr. GPT-4", un modelo de lenguaje Al que está discutiendo el caso junto con expertos humanos.\s
expertos humanos. Una conferencia clinicopatológica tiene varias reglas tácitas. La primera es\s
que la mayoría de las veces hay un único diagnóstico definitivo (aunque rara vez puede haber más de uno),
y se trata de un diagnóstico que hoy se sabe que existe en humanos. El diagnóstico casi siempre se\s
confirmado mediante algún tipo de prueba de patología clínica o anatomopatológica, aunque en\s
casos raros en los que no existe una prueba de este tipo para un diagnóstico, éste puede\s
diagnóstico puede realizarse mediante criterios clínicos validados o, en muy raras ocasiones, simplemente confirmarse mediante la opinión de un experto.\s
Al final de la descripción del caso se le indicará si se solicita alguna prueba o pruebas diagnósticas.\s
diagnósticas, que puede suponer que harán el diagnóstico o diagnósticos. Después de leer el caso\s
quiero que des dos datos. El primer dato es su diagnóstico o diagnósticos más probables.\s
diagnóstico/diagnósticos. El objetivo es obtener la respuesta correcta, no una amplia categoría de respuestas.\s
correcta, no una amplia categoría de respuestas. No es necesario que explique su razonamiento.\s
el/los diagnóstico/s. El segundo dato es dar un diagnóstico diferencial sólido,\s
ordenados por su probabilidad, de modo que el diagnóstico más probable esté arriba y el menos probable, abajo.\s
esté en la parte inferior. El número de diagnósticos diferenciales es ilimitado. Puede dar\s
Puede dar tantos diagnósticos como considere razonables. No es necesario que explique su razonamiento,\s
sólo enumere los diagnósticos. De nuevo, el objetivo es ser lo más específico posible con cada uno de los\s
diagnósticos.\s
¿Tiene alguna pregunta, Dr. GPT-4?
                                 
Este es el caso:
""";
    }

}
