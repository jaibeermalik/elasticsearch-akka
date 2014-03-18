package org.jai.search.actors;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.*;

import org.jai.search.config.ElasticSearchIndexConfig;
import org.jai.search.config.IndexDocumentType;
import org.jai.search.data.SampleDataGeneratorService;
import org.jai.search.model.Product;

import com.typesafe.config.ConfigFactory;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActor;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;

public class SetupDocumentTypeWorkerActorTest
{
    private static ActorSystem system;

    @BeforeClass
    public static void prepareBeforeClass()
    {
//        system = ActorSystem.create();
        system = ActorSystem.create("TestSearchIndexingSystem", ConfigFactory.load().getConfig("TestSearchIndexingSystem"));
    }
    
    private static String getTestConfig()
    {
        return "TestSearchIndexingSystem { "
                + " documentGenerateWorkerActorDispatcher"
                + " {"
                + " type = BalancingDispatcher"
                + "executor = \"thread-pool-executor\""
                + " thread-pool-executor {"
                + " core-pool-size-min = 2"
                + " core-pool-size-factor = 2.0"
                + " core-pool-size-max = 4"
                + " }"
                + " throughput = 10"
                + " mailbox-capacity = -1"
                + " }";
    }

    @Before
    public void prepareTest()
    {
    }

    @Test
    public void handleIndexDocumentTypeMessageVO()
    {
        final Props props = Props.create(SetupDocumentTypeWorkerActor.class, null, null);
        final TestActorRef<SetupDocumentTypeWorkerActor> ref = TestActorRef.create(system, props);
        final SetupDocumentTypeWorkerActor actor = ref.underlyingActor();
        
        TestProbe testProbeDataGeneratorWorker = TestProbe.apply(system);
        actor.dataGeneratorWorkerRouter = testProbeDataGeneratorWorker.ref();
        
        
        
        
        ElasticSearchIndexConfig config = ElasticSearchIndexConfig.COM_WEBSITE;
        IndexDocumentType documentType = IndexDocumentType.PRODUCT;
        String indexName = "trialindexName";
        IndexDocumentTypeMessageVO indexDocumentTypeMessageVO = new IndexDocumentTypeMessageVO()
        .config(config).documentType(documentType).newIndexName(indexName);
        
        TestProbe testProbe = TestProbe.apply(system);
        ref.tell(indexDocumentTypeMessageVO, testProbe.ref());
//
//        List<Product> productsList = new ArrayList<Product>();
//        Product product = new Product();
//        //For now ids hard coded in generator for testing
//        Long productId = 1l;
//        product.setId(productId);
//        productsList.add(product);
//        
//        TestProbe testProbe = TestProbe.apply(system);
//        ref.tell(indexDocumentTypeMessageVO, testProbe.ref());
//        
        testProbeDataGeneratorWorker.expectMsgClass(IndexDocumentTypeMessageVO.class);     
        TestActor.Message message = testProbeDataGeneratorWorker.lastMessage();
        IndexDocumentTypeMessageVO resultMsg = (IndexDocumentTypeMessageVO) message.msg();
//        assertEquals(productId, resultMsg.getDocumentId());
        assertEquals(config, resultMsg.getConfig());
        assertEquals(documentType, resultMsg.getIndexDocumentType());
        assertEquals(indexName, resultMsg.getNewIndexName());
    }
}
