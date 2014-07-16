package org.jai.search.test;

import static org.junit.Assert.assertTrue;

import org.jai.search.actors.AppConfigurationScala;
import org.jai.search.actors.BootStrapIndexService;
import org.jai.search.actors.BootStrapIndexServiceScala;
import org.jai.search.client.SearchClientService;
import org.jai.search.config.ElasticSearchIndexConfig;
import org.jai.search.data.SampleDataGeneratorService;
import org.jai.search.index.IndexProductDataService;
import org.jai.search.query.ProductQueryService;
import org.jai.search.setup.SetupIndexService;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.util.Random;

@ContextConfiguration(locations = { "classpath:applicationContext-elasticsearch.xml" })
public abstract class AbstractSearchJUnit4SpringContextTests extends AbstractJUnit4SpringContextTests
{
    @Autowired
    @Qualifier("searchClientService")
    protected SearchClientService searchClientService;

    @Autowired
    protected SetupIndexService setupIndexService;

    @Autowired
    protected SampleDataGeneratorService sampleDataGenerator;

    @Autowired
    protected ProductQueryService productQueryService;

    @Autowired
    protected IndexProductDataService indexProductData;

    @Autowired
    protected BootStrapIndexService bootStrapIndexService;

    @Autowired
    protected BootStrapIndexServiceScala bootStrapIndexServiceScala;

    protected Client getClient()
    {
        return searchClientService.getClient();
    }

    @Before
    public void prepare()
    {
        //Randomly run to build indices using Java Akka impl or Scala Akka impl
        if (new Random().nextBoolean())
        {
            System.out.println("Test setup indexing preparation building with Java Akka Impl!");
            bootStrapIndexService.preparingIndexes();
        }
        else
        {
            System.out.println("Test setup indexing preparation building with Scala Akka Impl!");
            AppConfigurationScala appConfigurationScala = new AppConfigurationScala();
            appConfigurationScala.setActorSystemScala();
            bootStrapIndexServiceScala.prepareAllIndices(appConfigurationScala.indexDataMasterActorScala(applicationContext));
        }
        searchClientService.getClient().admin().indices().refresh(Requests.refreshRequest()).actionGet();
        System.out.println("yes, test setup indexing preparation done!");
    }

    protected void refreshSearchServer()
    {
        searchClientService.getClient().admin().indices().refresh(Requests.refreshRequest()).actionGet();
    }

    protected void checkIndexHealthStatus(String indexName)
    {
        ClusterHealthRequest request = new ClusterHealthRequest(indexName);
        ClusterHealthStatus clusterHealthStatus = searchClientService.getClient().admin().cluster().health(request).actionGet().getStatus();
        assertTrue(clusterHealthStatus.equals(ClusterHealthStatus.GREEN));
    }

    protected long getIndexTotalDocumentCount(ElasticSearchIndexConfig elasticSearchIndexConfig)
    {
        long count = searchClientService.getClient().prepareCount(elasticSearchIndexConfig.getIndexAliasName())
                .setTypes(elasticSearchIndexConfig.getDocumentType()).execute().actionGet().getCount();
        return count;
    }
}
