## Evaluation of Medical Information Retrieval
___________________________________________

Based on a challenge [proposed by CLEF eHealth in 2015 (task 2):](https://sites.google.com/site/clefehealth2015/task-2)

"The 2015 CLEF eHealth Task 2 aims to evaluate the effectiveness of information retrieval systems when searching for health content on the web, with the objective to foster research and development of search engines tailored to health information seeking. 

This task is a continuation of the previous CLEF eHealth Task 3 that ran in 2013 and 2014, and embraces the TREC-style evaluation process, with a shared collection of documents and queries, the contribution of runs from participants and the subsequent formation of relevance assessments and evaluation of the participants submissions. 

In this year’s task, we explore queries that differ from the previous years tasks. This year’s queries in fact aim to mimic queries of lay people (ie. not medical expert) that are confronted with a sign, symptom or condition and attempt to find out more about the condition they may have. For example, when confronted with signs of jaundice, non experts may use queries like "white part of eye turned green" to search for information that allow them to diagnose themselves or better understand their health conditions. These queries are often circumlocutory in nature, where a long, ambiguous wording is used in place of the actual name to refer to a condition or disease. Recent research has shown that these queries are used by health consumers and that current web search engines fail to effectively support these queries (Zuccon et al., Staton et al.)

In addition to changes in query types, this year’s lab will introduce changes in the evaluation settings, where, to judge the effectiveness of a retrieval system, we will consider also the readability of the retrieved medical content, along with the common topical assessments of relevance. Also, the multilingual element added to the task last year will be further developed. This year, parallel queries in Arabic, Czech, French, German, Farsi and Portuguese will be offered, as well as baseline machine translations."
[https://sites.google.com/site/clefehealth2015/task-2]

This project utilizes two external tools - [Elasticsearch](https://www.elastic.co/products/elasticsearch), an open source RESTful search technology [https://www.elastic.co/products/elasticsearch] and [TREC_EVAL](https://trec.nist.gov/trec_eval/), the standard tool used by the TREC community for evaluating retrieval systems given search engine results and a set of human generated revelance scores.

Requires Elasticsearch version 5.0 or later (developed on version [6.4.2](https://www.elastic.co/downloads/past-releases/elasticsearch-6-4-2)) and TREC_EVAL Windows (developed on Windows version 3).

Two sets of external resources, both provided by CLEF eHealth, were used for the purposes of this study - a [collection of 1,104,337 medical and health related documents](http://catalog.elra.info/en-us/repository/browse/ELRA-E0043/) [documents obtainable from CLEF eHealth: http://catalog.elra.info/en-us/repository/browse/ELRA-E0043/] and a [dataset containing query files and query relevance (qrel) files](https://github.com/CLEFeHealth/CLEFeHealth2015Task2) [dataset obtainable from CLEF eHealth on Github: https://github.com/CLEFeHealth/CLEFeHealth2015Task2].
These documents, qrels and query files are optional - the toolset provided by this project allows the use of any custom document collection and dataset.

-------------------
Running Evaluations
___________________

Elasticsearch version 5.0 or later can be obtained from https://www.elastic.co/guide/index.html. Elasticsearch must be running before attempting to index or run queries, and it should be run on the default port (port 9200).

    Elasticsearch.bat -d -q
    
The Java project should be built and run. A provided graphical user interface simplifies the running of evaluations by providing three functions - index/reindex, run queries and run TREC_EVAL. Documents must first be indexed before running queries, and query results are required to run TREC_EVAL. Query results files for all similarity models evaluated are provided if only TREC_EVAL is required. 

![alt text](https://github.com/IRevaluation/IRmedical/blob/master/GUI_Images/UI_Win10.png "Run")

This user interface will request paths to two directories:

1. Document Directory. This directory should contain two folders: 'documents' and 'qrels'. The project will automatically search for these files. The TREC_EVAL executable should also be placed in this directory. The typical structure of this directory is shown below, where the 'evaluations' directory is generated automatically and contains the TREC_EVAL result files. The top-level text files contain query results and are generated automatically upon running queries. Evaluation results can be viewed directly through the 'Analysis' tab of the provided user interface.

        |--documents
           |--document1.dat
           |--document2.dat
        |--qrels
           |--qrels-graded.txt
        |--TREC_EVAL.exe
        |--evaluations
           |--bm25.txt
           |--dfr.txt
           |--ib.txt
        |--lmdirichlet.txt
        |--dfr.txt
        |--lmjelenikmercer.txt
           
2. Path to Query File. A direct path to the text file containing queries to be used by TREC_EVAL. May be anywhere on file system.

![alt text](https://github.com/IRevaluation/IRmedical/blob/master/GUI_Images/UI_Analyze_Win10.png "Analyze")

----------------------
Explanation of Results
______________________

An explanation of the results obtained can be found in the readme.md on the (TREC_EVAL github page)[https://github.com/CLEFeHealth/CLEFeHealth2015Task2]:

"1. Total number of documents over all queries
        Retrieved:
        Relevant:
        Rel_ret:     (relevant and retrieved)
   These should be self-explanatory.  All values are totals over all
   queries being evaluated.
   
2. Interpolated Recall - Precision Averages:
        at 0.00
        at 0.10
        ...
        at 1.00
   See any standard IR text (especially by Salton) for more details of 
   recall-precision evaluation.  Measures precision (percent of retrieved
   docs that are relevant) at various recall levels (after a certain
   percentage of all the relevant docs for that query have been retrieved).
   "Interpolated" means that, for example, precision at recall
   0.10 (ie, after 10% of rel docs for a query have been retrieved) is
   taken to be MAXIMUM of precision at all recall points >= 0.10.
   Values are averaged over all queries (for each of the 11 recall levels).
   These values are used for Recall-Precision graphs.
   
3. Average precision (non-interpolated) over all rel docs
   The precision is calculated after each relevant doc is retrieved.
   If a relevant doc is not retrieved, the precision is 0.0.
   All precision values are then averaged together to get a single number
   for the performance of a query.  Conceptually this is the area
   underneath the recall-precision graph for the query.
   The values are then averaged over all queries.
   
4. Precision:
       at 5    docs
       at 10   docs
       ...
       at 1000 docs   
   The precision (percent of retrieved docs that are relevant) after X
   documents (whether relevant or nonrelevant) have been retrieved.
   Values averaged over all queries.  If X docs were not retrieved
   for a query, then all missing docs are assumed to be non-relevant.
   
5. R-Precision (precision after R (= num_rel for a query) docs retrieved):
   New measure, intended mainly to be used for routing environments.
   Measures precision (or recall, they're the same) after R docs
   have been retrieved, where R is the total number of relevant docs
   for a query.  Thus if a query has 40 relevant docs, then precision
   is measured after 40 docs, while if it has 600 relevant docs, precision
   is measured after 600 docs.  This avoids some of the averaging
   problems of the "precision at X docs" values in (4) above.
   If R is greater than the number of docs retrieved for a query, then
   the nonretrieved docs are all assumed to be nonrelevant."
   
------------
Unit Testing
____________

A short set of unit tests are provided in the file UnitTest.java. Four out of five tests do not require Elasticsearch to be run. The user interface and TREC_EVAL may also be run without Elasticsearch, and query results files generated against the CLEF eHealth document collection by Elasticsearch have been provided for use with the TREC_EVAL function.
