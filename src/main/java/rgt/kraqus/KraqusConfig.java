package rgt.kraqus;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bson.Document;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import rgt.kraqus.get.TradePairDTO;
import rgt.kraqus.prod.ProdLogDTO;

/**
 *
 * @author rgt
 */
@Singleton
@Startup
public class KraqusConfig {

    @Inject
    private MongoClient mongoClient;

    @Inject
    @ConfigProperty(name = "kraqus.runTrade", defaultValue = "false")
    private boolean runTrade;

    @Inject
    @ConfigProperty(name = "kraqus.runCandle", defaultValue = "false")
    private boolean runCandle;

    @Inject
    @ConfigProperty(name = "kraqus.runProduction", defaultValue = "false")
    private boolean runProduction;

    private MongoCollection<TradePairDTO> tradePairColl;
    private MongoCollection<ProdLogDTO> prodLogColl;

    @PostConstruct
    public void init() {

        MongoDatabase database = mongoClient.getDatabase("kraqus");
        
        this.tradePairColl = database.getCollection("tradepair", TradePairDTO.class);
        if (!this.isIndex(this.tradePairColl, "last_-1")) {
            this.tradePairColl.createIndex(Indexes.descending("last"));
        }

        this.prodLogColl = database.getCollection("prodlog", ProdLogDTO.class);
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

    public MongoCollection<ProdLogDTO> getProdLogColl() {
        return prodLogColl;
    }

    public boolean isRunTrade() {
        return runTrade;
    }

    public void setRunTrade(boolean runTrade) {
        this.runTrade = runTrade;
    }

    public boolean isRunCandle() {
        return runCandle;
    }

    public void setRunCandle(boolean runCandle) {
        this.runCandle = runCandle;
    }

    public boolean isRunProduction() {
        return runProduction;
    }

    public void setRunProduction(boolean runProduction) {
        this.runProduction = runProduction;
    }

}
