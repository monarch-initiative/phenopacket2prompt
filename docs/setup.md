# Set-up

TODO -- how to setup Java etc.

## Download command
Before running the batch command, run the download command to get the necessary files

```
java -jar target/phenopacket2prompt.jar download
```

## Batch command
To run the batch command, first download the latest release from the 
[releases](https://github.com/monarch-initiative/phenopacket-store/releases) section of the phenopacket-store
repository. Unpack either all_phenopackets.tgz or all_phenopackets.zip (the files are identical except for the
method of compression).

```
java -jar target/phenopacket2prompt.jar batch -d <all_phenopackets>
```
Replasce `<all_phenopackets>` with the actual path on your system.

The app should create a folder "prompts", with two subdirectories, "en" and "es" with English and Spanish prompts. 
There are some errors that still need to be fixed, but several thousand prompts should appear.

## Todo
also output a file with expected diagnosis
