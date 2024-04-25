# Phenopacket2Prompt


phenopacket2promot is a Java 17 application that creates prompts intended for use with GPT starting from
GA4GH phenopackets.





## Installation


Most users should download the prebuilt executable file from the
[Releases](https://github.com/monarch-initiative/phenopacket2prompt/releases) page of the GutHub repository.

It is also possible to build the application from source using standard Maven and Java tools.

```shell title="building the app"
git clone https://github.com/monarch-initiative/phenopacket2prompt.git
cd phenopacket2prompt
maven package
java -jar target/phenopacket2prompt.jar
```

## Setup


First download the latest copy of the [Human Phenotype Ontology](https://hpo.jax.org/app/) hp.json file. This file is
used for text mining of clinical signs and symptoms. For more information about the HPO, see
[Koehler et al. (2021)](https://pubmed.ncbi.nlm.nih.gov/33264411/). Adjust the path to the `phenopacket2prompt.jar`
file as necessary.



```shell title="download"
java -jar phenopacket2prompt.jar download
```




## Running phenopacket2prompt


Assuming the hp.json file has been downloaded as described above and all of the case report text files
are available in a directory at ``some/path/gptdocs``, run


```shell title="running the app"
java -jar phenopacket2prompt.jar gpt -g some/path/gptdocs
```

   

This command will create a new directory called ``gptOut`` (this can be adjusted using the -o option).
It will contain four subdirectories

1. phenopackets. GA4GH phenopackets derived from each case report
2. phenopacket_based_queries. Feature-based query prompts for GPT-4 based on the information in the phenopackets
3. txt_without_discussion. Original query based on the original case report with text as presented by the first discussant up to but not including text contributed by the second discussant or any following text
4. txt_with_differential. Text that starts with the presentation by the first discussant up to and including the differential. This was used to check parsing but was not used in our analysis.





### Feedback


The best place to leave feedback, ask questions, and report bugs is the [phenopacket2prompt Issue Tracker](https://github.com/monarch-initiative/phenopacket2prompt/issues).

