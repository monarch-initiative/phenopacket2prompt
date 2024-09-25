# Set-up

*phenopacket2prompt* requires at least Java 17. To build it from scratch, [Apache Maven](https://maven.apache.org/){:target="_blank"} is also required.

## Installation


Most users should download the prebuilt executable file from the
[Releases](https://github.com/monarch-initiative/phenopacket2prompt/releases){:target="_blank"} page of the GutHub repository.

It is also possible to build the application from source using standard Maven and Java tools.

```shell title="building the app"
git clone https://github.com/monarch-initiative/phenopacket2prompt.git
cd phenopacket2prompt
maven package
java -jar target/phenopacket2prompt.jar [OPTIONS]
```

## Setup


## Downloading necessary input files
For more information about the HPO, see
[Koehler et al. (2021)](https://pubmed.ncbi.nlm.nih.gov/33264411/){:target="_blank"}.
To run the commands, we need to download the latest copy of the [Human Phenotype Ontology](https://hpo.jax.org/app/){:target="_blank"} hp.json file as well as the
hp-international.obo file. We can do this by runing the download command to get the necessary files. Adjust the path to the `phenopacket2prompt.jar`
file as necessary, the default is `target/phenopacket2prompt`.

```shell title="download"
java -jar target/phenopacket2prompt.jar download
```

## Obtaining phenopackets
This app is designed to work with the collection of GA4GH phenopackets obtained from the
[phenopacket-store](https://github.com/monarch-initiative/phenopacket-store){:target="_blank"} repository.
Obtain the latest release of these files from the Releases page of the repository and unpack (unzip or ungzip) the repository.

