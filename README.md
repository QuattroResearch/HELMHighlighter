The HELMHighter is a java-program to highlight the HELM syntax. A file containing in each line a HELM notation is given as the input. It produces html-output 
according to the user-specified config file. This file contains syntax highlighting information for the HELM Notation. All available highlighting options can be found in data/HelmHighlighterConfig.txt. The data directory contains also an example input, config and the produced output file

The program is called by the following command:
java -jar HelmHighlighter-0.0.01-SNAPSHOT.ar -i data/helmstring.txt -o test.html -c data/HelmHighlighterConfig.txt



