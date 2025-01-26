package rgt.kraqus;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bson.Document;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import rgt.kraqus.calc.CandleDTO;
import rgt.kraqus.get.TradePairDTO;
import rgt.kraqus.learn.LearnDTO;

/**
 *
 * @author rgt
 */
@Singleton
@Startup
public class MyConfig {

    @Inject
    private MongoClient mongoClient;

    @Inject
    @ConfigProperty(name = "kraqus.runProduction", defaultValue = "false")
    private boolean runProduction;

    private MongoCollection<TradePairDTO> tradePairColl;
    private MongoCollection<CandleDTO> candleColl;
    private MongoCollection<LearnDTO> learnColl;

    @PostConstruct
    public void init() {

        MongoDatabase database = mongoClient.getDatabase("kraqus");

        this.tradePairColl = database.getCollection("tradepair", TradePairDTO.class);
        if (!this.isIndex(tradePairColl, "last_-1")) {
            this.tradePairColl.createIndex(Indexes.descending("last"));
        }
        if (!this.isIndex(tradePairColl, "timeDate_1")) {
            this.tradePairColl.createIndex(Indexes.ascending("timeDate"));
        }

        this.candleColl = database.getCollection("candle", CandleDTO.class);
        if (!this.isIndex(candleColl, "startDate_1")) {
            this.candleColl.createIndex(Indexes.ascending("startDate"), new IndexOptions().unique(true));
        }
        if (!this.isIndex(candleColl, "startDate_-1")) {
            this.candleColl.createIndex(Indexes.descending("startDate"), new IndexOptions().unique(true));
        }
        if (!this.isIndex(candleColl, "calcCandle_1")) {
            this.candleColl.createIndex(Indexes.ascending("calcCandle"));
        }
        
        this.learnColl = database.getCollection("learn", LearnDTO.class);
    }

    @PreDestroy
    public void close() {
        this.mongoClient.close();
    }

    /**
     * Is the index exists?
     *
     * @param collection
     * @param indexName
     * @return
     */
    private boolean isIndex(MongoCollection collection, String indexName) {
        MongoCursor<Document> indexes = collection.listIndexes().iterator();
        while (indexes.hasNext()) {
            Document index = indexes.next();
            if (indexName.equals(index.getString("name"))) {
                return true;
            }
        }
        return false;
    }

    public MongoCollection<TradePairDTO> getTradePairColl() {
        return tradePairColl;
    }

    public MongoCollection<CandleDTO> getCandleColl() {
        return candleColl;
    }

    public MongoCollection<LearnDTO> getLearnColl() {
        return learnColl;
    }
    
    public boolean isRunProduction() {
        return runProduction;
    }

    public void setRunProduction(boolean runProduction) {
        this.runProduction = runProduction;
    }

}
