package rgt.kraqus;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import rgt.kraqus.get.TradePairDTO;

/**
 *
 * @author rgt
 */
@ApplicationScoped
public class Configuration {

    @Inject
    private MongoClient mongoClient;

    public MongoCollection<TradePairDTO> getTradePairColl() {
        return mongoClient.getDatabase("kraqus").getCollection("tradepair", TradePairDTO.class);
    }

}
