package rgt.kraqus;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import io.quarkus.logging.Log;
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
import rgt.kraqus.learn.LearnPairDTO;
import rgt.kraqus.model.ModelDTO;
import rgt.kraqus.profit.ProfitDTO;

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
    private MongoCollection<ProfitDTO> profitColl;
    private MongoCollection<ModelDTO> modelColl;
    private MongoCollection<LearnPairDTO> learnPairlColl;

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

        this.profitColl = database.getCollection("profit", ProfitDTO.class);
        if (!this.isIndex(profitColl, "testNum_1")) {
            this.profitColl.createIndex(Indexes.ascending("testNum"), new IndexOptions().unique(true));
        }

        this.learnColl = database.getCollection("learn", LearnDTO.class);
        this.modelColl = database.getCollection("model", ModelDTO.class);

        this.learnPairlColl = database.getCollection("learnPair", LearnPairDTO.class);
        if (!this.isIndex(learnPairlColl, "buyDate_1")) {
            this.learnPairlColl.createIndex(Indexes.ascending("buyDate_1"), new IndexOptions().unique(true));
        }
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
        MongoCursor<?> cursor = collection.listIndexes().iterator();
        while (cursor.hasNext()) {
            Object item = cursor.next();
            if (item instanceof Document) {
                Document index = (Document) item;
                if (indexName.equals(index.getString("name"))) {
                    return true;
                }
            } else {
                Log.error("Unexpected type in index list: " + item.getClass().getName());
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

    public MongoCollection<ProfitDTO> getProfitColl() {
        return profitColl;
    }

    public MongoCollection<ModelDTO> getModelColl() {
        return modelColl;
    }

    public MongoCollection<LearnPairDTO> getLearnPairlColl() {
        return learnPairlColl;
    }

    public boolean isRunProduction() {
        return runProduction;
    }

    public void setRunProduction(boolean runProduction) {
        this.runProduction = runProduction;
    }

}
