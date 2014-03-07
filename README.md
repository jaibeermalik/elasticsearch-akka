elasticsearch-akka
======================

Using akka java and scala API with elasticsearch to generate and index documents.


Scenarios:

Setup Index:
setup all the indices from start, and change to alias later.
Setup multiple indices in parallel. 
Only if all indexing done, change to alias.
Monitor indexing of all doc, how many successful and failures.
Depending all count of docs, change aliasing.

one approach:
BooststrapSearchSetup()
					->SetupMasterActor(foreach index)
													->SetupMasterWorkerActor(foreach index)
																										->IndexDocumentRouterActor(let's say 10)
																														->SearchDocumentGeneratorActor 
second approach:																														->IndexDocumentActor
BooststrapSearchSetup
					for full index, SearchConfig->SetupIndexWorkerActor(foreach index, first generate doc and then index)
																							->SearchDocumentGeneratorActor 
																							->IndexDocumentActor
					for single doc, Long id->SetupIndexWorkerActor(first generate doc and then index)
																									->SearchDocumentGeneratorActor 
																									->IndexDocumentActor

Regular update:
Index a document Asynch.

Delete doc:
delete a doc, and check status.

-----

[Jaibeer Malik](http://jaibeermalik.wordpress.com/category/tech-stuff/elasticsearch/)