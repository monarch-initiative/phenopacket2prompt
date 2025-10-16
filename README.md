# phenopacket2prompt
GA4GH Phenopacket to LLM prompt

Prompts generated with this code base have been used for:
- A benchmark of some large language models against a differential diagnostics tool, [Exomiser](https://pmc.ncbi.nlm.nih.gov/articles/PMC5467691/) (code [here](https://github.com/exomiser/Exomiser)). Our evidence shows Exomiser outperforms the LLMs as per our [preprint](https://www.medrxiv.org/content/10.1101/2024.07.22.24310816v3), [data at this zenodo](https://doi.org/10.5281/zenodo.14008476).
- A comparison of gpt-4o's and [Meditron3-70B](https://huggingface.co/OpenMeditron/Meditron3-70B) ability to carry out differential diagnosis when prompted in 10 different languages, results published in [eBioMedicine](https://doi.org/10.1016/j.ebiom.2025.105957) and at [zenodo](https://doi.org/10.5281/zenodo.14804250).

At this related [link](https://doi.org/10.5281/zenodo.15065293) for you can find 5K+ patient descriptions in 10 languages created from [these phenopackets](https://github.com/monarch-initiative/phenopacket-store), automatically updated when a new release of the phenopacket-store happens.  
