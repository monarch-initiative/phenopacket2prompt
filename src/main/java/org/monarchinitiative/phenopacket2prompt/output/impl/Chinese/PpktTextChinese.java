package org.monarchinitiative.phenopacket2prompt.output.impl.chinese;

import org.monarchinitiative.phenopacket2prompt.output.PhenopacketTextGenerator;

public class PpktTextChinese implements PhenopacketTextGenerator {

    @Override
    public String GPT_PROMPT_HEADER() {
        return  """
我正在对一份临床病例进行测试，以将您的诊断与人类专家的诊断进行比较。我将向您提供一个医疗案例中的部分信息，而您将作为一个提供诊断的人工智能语言模型“gpt-4医生”对案例进行诊断。以下是操作说明：
首先，每个案例有仅有一个人类已知的明确疾病。其次，大部分的诊断结果都有相对应的基因测试佐证。在极少数情况下，当相对应的基因测试不存在时，我们则使用已经过验证的临床标准或者专家意见进行诊断。

在您阅读完病例后，请您做出诊断，并将可能存在的疾病依据概率大小进行排序（最有可能的疾病排在最前）并标明疾病名称。例如，如果第一个疾病是鳃面综合征，第二个疾病是囊性纤维化，请用英语提供以下内容：

1. Branchiooculofacial syndrome
2. Cystic fibrosis

请在您的回答中包含尽可能多的有关疾病，并使用英文进行作答。在回答中，您不需要提供诊断的依据或理由，仅需列出疾病名称即可。

案例如下：
             
""";
    }

}