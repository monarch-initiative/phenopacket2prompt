# phenopacket2prompt
GA4GH Phenopacket to LLM prompt

Prompts generated with this code base have been used for:
- A benchmark of some large language models against a differential diagnostics tool, [Exomiser](https://pmc.ncbi.nlm.nih.gov/articles/PMC5467691/) (code [here](https://github.com/exomiser/Exomiser)). Our evidence shows Exomiser outperforms the LLMs as per our [preprint](https://www.medrxiv.org/content/10.1101/2024.07.22.24310816v3), [data at this zenodo](https://doi.org/10.5281/zenodo.14008476).
- A comparison of gpt-4o's and [Meditron3-70B](https://huggingface.co/OpenMeditron/Meditron3-70B) ability to carry out differential diagnosis when prompted in 10 different languages, results at this [medRxiv](https://www.medrxiv.org/content/10.1101/2025.02.26.25322769v1) or [zenodo](https://doi.org/10.5281/zenodo.14804250) (see also this related [link](https://doi.org/10.5281/zenodo.15065293) for some more data). 
