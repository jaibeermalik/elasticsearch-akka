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
																														->IndexDocumentActor			

second approach:																														
BooststrapSearchSetup
					for full index, SearchConfig->SetupIndexWorkerActor(foreach index, first generate doc and then index)
																							->SearchDocumentGeneratorActor 
																							->IndexDocumentActor
					for single doc, Long id->SetupIndexWorkerActor(first generate doc and then index)
																									->SearchDocumentGeneratorActor 
																									->IndexDocumentActor

BooststrapSearchSetup->REBUILD_ALL_INDICES->Is all indexing done->REBUILD_ALL_INDICES_DONE
SetupIndexWorkerActor->


Regular update:
Index a document Asynch.

Delete doc:
delete a doc, and check status.

Check Pinned dispatcher for the database calls, if those are blocking calls. dispatcher n resource per actor.
Check balancing dispatcher for same actor type as diving work to idle actors also.
Pick->dispatcher, executor, no. of threads/cores, throughput (msg exec time)

check dynamically resing the routers.
check filedurablestorage mailbox type for storing messages for durability, usually for multi node distributed systems.

check spray/scalatra for exposing actors using http/rest api.

-----

[Jaibeer Malik](http://jaibeermalik.wordpress.com/category/tech-stuff/elasticsearch/)