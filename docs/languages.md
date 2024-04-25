# Languages

phenopacket2prompt creates phenopackets in various languages using a template system. 
(TODO explain HPO translations).


## The template

Phenopackets are generated according to the same scheme (template) in all languages. The following example explains the template in English.


The template contains a series of components that can be translated in isolation.

### Header

This is the explanation for the start of the prompt for GPT.

:bulb: 
I am running an experiment on a clinicopathological case conference to see how your diagnoses 
compare with those of human experts. I am going to give you part of a medical case. ... After you read the case, 
I want you to give two pieces of information. The first piece of information is your most likely 
diagnosis/diagnoses. You need to be as specific as possible -- the goal is to get the correct 
answer, not a broad category of answers.
Do you have any questions, Dr. GPT-4?


### Describe the individual

In this section, we present the age and sex of the proband (patient or individual) and the symptoms with which they presented. Each individual can have an age of onset and an age at last examination. Therefore, we have this

1. Age of onset and age at last examination available
The proband was a 39-year old woman who presented at the age of 12 years with HPO1, HPO2, and HPO3. HPO4 and HPO5 were excluded.

2. Age at last examination available but age of onset not available
The proband was a 39-year old woman who presented with HPO1, HPO2, and HPO3. HPO4 and HPO5 were excluded.

3. Age at last examination not available but age of onset available
The proband  presented  at the age of 12 years with HPO1, HPO2, and HPO3. HPO4 and HPO5 were excluded.

4. No age information available
The proband  presented  with HPO1, HPO2, and HPO3. HPO4 and HPO5 were excluded.

### Describe findings at other specified ages
Some of the phenopackets have multiple ages at which specific features were first observed. Each of these is written as follows.

At the age of 42 years, he/she/the individual presented with HPO1, HPO2, and HPO3, and HPO4 and HPO5 were excluded.