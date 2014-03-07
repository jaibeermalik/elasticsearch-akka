package org.jai.search.actors;

import java.util.List;

import org.elasticsearch.action.index.IndexRequestBuilder;

public interface IndexDataActorSystemHandlerService
{
    void handleIndexRequests(List<IndexRequestBuilder> requests);
}
