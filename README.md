elasticsearch-akka
======================

Using akka java and java/scala API with elasticsearch to generate and index documents.


Scenarios:

Setup Index:
setup all the indices from start, and change to alias later.
Setup multiple indices in parallel. 
Only if all indexing done, change to alias.
Monitor indexing of all doc, how many successful and failures.
Depending all count of docs, change aliasing.

Approach:
BooststrapSearchSetup(): Kick off search setup and monitor status.
->SetupIndexMasterActor: Monitor status for all indexes. Single instance per app context.
-->SetupIndexWorkerActor: foreach index monitor status. One instance per index.
--->SetupDocumentTypeWorkerActor: foreach document type track status and index in parallel. One instance per document type under an index.
---->DataGeneratorWorkerActor: Generate data which needs to be indexed. Single instance per document type.
---->DocumentGeneratorWorkerActor: Generate search document. multiple instance let's say 10.
---->IndexProductDataWorkerActor: index search document to ES. multiple instance let's say 10.


Regular update:
Index a document Asynch on real time. Update/Delete a doc, and check status.

Exception handling:
Each Actor to report status using exception handling.
Actor state and status reporting from child actor to Parent with timeout approach.

Executor, Dispatcher and Routing:
Specific configurations for executors, dispatchers and routing for each actor.
Balancing dispatcher for same actor type as diving work to idle actors also.
Round-robin Resizable router pool.

Testing:
Unit test cases for Actors using testkit.
Integration test cases with spring test.

-----

[Jaibeer Malik](http://jaibeermalik.wordpress.com/category/tech-stuff/elasticsearch/)