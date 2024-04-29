# Phenopacket2Prompt


phenopacket2promot is a Java 17 application that creates prompts intended for use with GPT starting from
GA4GH phenopackets.







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

