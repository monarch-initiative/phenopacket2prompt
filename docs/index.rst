.. _home:

==================
Phenopacket2Prompt
==================

phenopacket2promot is a Java 17 application that performs text mining on files that have been extracted
from New England Journal of Medicine (NEJM) `Case Challenges <https://www.nejm.org/case-challenges>`_, i.e.,
NEJM Case Records of the Massachusetts General Hospital.





Installation
^^^^^^^^^^^^

Most users should download the prebuilt executable file from the
`Releases <https://github.com/monarch-initiative/phenopacket2prompt/releases>`_ page of the GutHub repository.

It is also possible to build the application from source using standard Maven and Java tools.

.. code-block:: shell
   :linenos:

   git clone https://github.com/monarch-initiative/phenopacket2prompt.git
   cd phenopacket2prompt
   maven package
   java -jar target/phenopacket2prompt.jar

Setup
^^^^^

First download the latest copy of the `Human Phenotype Ontology <https://hpo.jax.org/app/>`_ hp.json file. This file is
used for text mining of clinical signs and symptoms. For more information about the HPO, see
`Koehler et al. (2021) <https://pubmed.ncbi.nlm.nih.gov/33264411/>`_. Adjust the path to the `phenopacket2prompt.jar`
file as necessary.

.. code-block:: shell
   :linenos:

   java -jar phenopacket2prompt.jar download


Preparing the case report files
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

TODO describe the Python script or add it to this repo


Running phenopacket2prompt
^^^^^^^^^^^^^^^^^^^^^^^^^^

Assuming the hp.json file has been downloaded as described above and all of the case report text files
are available in a directory at ``some/path/gptdocs``, run


.. code-block:: shell
   :linenos:

   java -jar phenopacket2prompt.jar gpt -g some/path/gptdocs

This command will create a new directory called ``gptOut`` (this can be adjusted using the -o option).
It will contain four subdirectories

1. phenopackets. GA4GH phenopackets derived from each case report
2. phenopacket_based_queries. Feature-based query prompts for GPT-4 based on the information in the phenopackets
3. txt_without_discussion. Original query based on the original case report with text as presented by the first discussant up to but not including text contributed by the second discussant or any following text
4. txt_with_differential. Text that starts with the presentation by the first discussant up to and including the differential. This was used to check parsing but was not used in our analysis.


.. toctree::
    :caption: contents
    :name: phenopacket2prompt
    :maxdepth: 1

    cases/PMID_33471980.rst
    authors
    developers
    history
    LICENSE



--------
Feedback
--------

The best place to leave feedback, ask questions, and report bugs is the `WN2vec Issue Tracker <https://github.com/TheJacksonLaboratory/wn2vec/issues>`_.

