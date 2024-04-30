# Set-up

phenopacket2prompt requires at least Java 17. To build it from scratch, maven is also required.

## Download command
Before running the batch command, run the download command to get the necessary files

```
java -jar target/phenopacket2prompt.jar download
```



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

