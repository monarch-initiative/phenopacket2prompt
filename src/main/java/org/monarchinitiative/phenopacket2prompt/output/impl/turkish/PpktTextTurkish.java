package org.monarchinitiative.phenopacket2prompt.output.impl.turkish;

import org.monarchinitiative.phenopacket2prompt.output.PhenopacketTextGenerator;

public class PpktTextTurkish implements PhenopacketTextGenerator {

    @Override
    public String GPT_PROMPT_HEADER() {
        return  """
Teşhislerinizin insan uzmanlarınkine kıyasla nasıl olduğunu görmek için klinik bir vaka raporu ile bir deney yapıyorum. Size tıbbi bir vakanın bir bölümünü sunacağım. Herhangi bir hastayı tedavi etmeye çalışmıyorsunuz. Bu durumda siz, teşhis koyan bir yapay zeka dil modeli olan "Dr GPT-4 "sünüz. İşte bazı kurallar. İlk olarak, tek bir kesin tanı vardır ve bu artık insanlarda var olduğu bilinen bir tanıdır. Teşhis neredeyse her zaman genetik testlerle doğrulanır. Bununla birlikte, tanı için böyle bir testin mevcut olmadığı nadir durumlarda, tanı doğrulanmış klinik kriterler kullanılarak konulabilir veya çok nadir durumlarda sadece uzman görüşü ile doğrulanabilir. Vakayı okuduktan sonra, en olası adaydan başlayarak, olasılığa göre sıralanmış aday tanıların bir listesini içeren bir ayırıcı tanı yapmanızı istiyorum. Her aday hastalık adıyla birlikte listelenmelidir. Örneğin, ilk aday brankiookülofasiyal sendrom ve ikincisi kistik fibrozis ise, aşağıdakileri İngilizce olarak belirtiniz:

1. brankiookülofasiyal sendrom
2. Kistik fibrozis

Bu liste uygun olduğunu düşündüğünüz kadar çok tanı içermelidir.

Gerekçenizi açıklamanıza gerek yok, sadece teşhisleri listeleyin.\s
Bu talimatları size Almanca olarak verdim, ancak cevabınızı yalnızca İngilizce olarak vermenizi rica ediyorum.
İşte vaka:

""";
    }

}
