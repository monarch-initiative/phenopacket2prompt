# batch

This command creates prompts from all phenopackets in the input directory.

## Getting the input data

Go to the [Releases](https://github.com/monarch-initiative/phenopacket-store/releases) section of 
[phenopacket-store](https://github.com/monarch-initiative/phenopacket-store){:target="_blank"}, and download the
latest release (currently 0.1.5 on April 29, 2024, but evolving rapidly). Currently, this resource contains over 4300 phenopackets.


Download one of the archives (e.g., ``all_phenopackets.zip``) and unpack in a location of your choice.


Then run the following command.

```shell title="batch"
java -jar phenopacket2prompt.jar batch  -d <all_phenopackets>
```
where ``<all_phenopackets>`` is the complete relative or absolute path to the unpacked directory. 

phenopacket2prompt will create a new subdirectory called ``prompts``in the current directory. It will contain
one folder for each language (currently, English-en and Spanish-es), as well as a file called ``correct_results.tsv``
with the following structure


| Disease name                               | OMIM identifier |                                 Prompt file name |
|--------------------------------------------|:---------------:|-------------------------------------------------:|
| Birt-Hogg-Dube syndrome 2                  |   OMIM:620459   | PMID_36440963_IIIPMID_36440963_III-33-prompt.txt |
| Immunodeficiency 115 with autoinflammation |   OMIM:620632   |                 PMID_26008899_patient-prompt.txt |
| Jacobsen syndrome	                         |   OMIM:147791   |           	PMID_15266616_148-prompt.txt   |


Note that the prompt file name is the same for every language.