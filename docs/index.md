# Phenopacket2Prompt


*phenopacket2prompt* is a [Java](https://www.java.com/){:target="_blank"} application that creates prompts for Large
Language Models (LLMs) on the basis of clinical data that has been encoded using 
the [Global Alliance for Genomics and Health (GA4GH)](https://www.ga4gh.org/){:target="_blank"}
[Phenopacket Schema](https://pubmed.ncbi.nlm.nih.gov/35705716/){:target="_blank"}.

Optionally, the prompts can be generated in Czech, Dutch German, Italian, and Spanish.

## Running phenopacket2prompt

There are currently two use cases: 
1. The creation of prompts (in several languages!), starting from phenopackets, intended for use with a Large Language Model (LLM) which is asked for a differential diagnosis.  
2. The creation of phenopackets from case reports via text mining using the [fenominal](https://pubmed.ncbi.nlm.nih.gov/38001031/){:target="_blank"}
3. library.

### Running with Phenopackets

For this use case, follow the instructions in [Set-up](setup.md) and [Batch](batch.md).

### Running with case reports

Assuming the hp.json file has been downloaded as described in [Set-up](setup.md) and all the case report text files
are available in a directory at ``some/path/gptdocs``, run


```shell title="running the app"
java -jar target/phenopacket2prompt.jar gpt -g some/path/gptdocs
```

This command will create a new directory called ``gptOut`` (this can be adjusted using the -o option).
It will contain four subdirectories

1. phenopackets. GA4GH phenopackets derived from each case report
2. phenopacket_based_queries. Feature-based query prompts for GPT-4 based on the information in the phenopackets
3. txt_without_discussion. Original query based on the original case report with text as presented by the first discussant up to but not including text contributed by the second discussant or any following text
4. txt_with_differential. Text that starts with the presentation by the first discussant up to and including the differential. This was used to check parsing but was not used in our analysis.


### Feedback

The best place to leave feedback, ask questions, and report bugs is the 
[phenopacket2prompt Issue Tracker](https://github.com/monarch-initiative/phenopacket2prompt/issues){:target="_blank"}.

