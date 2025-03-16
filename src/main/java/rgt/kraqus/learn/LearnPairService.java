package rgt.kraqus.learn;

import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Sorts;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import rgt.kraqus.MyConfig;
import rgt.kraqus.calc.CandleDTO;
import rgt.kraqus.calc.CandleService;

/**
 * Learn Pairs Service
 * @author rgt
 */
@ApplicationScoped
public class LearnPairService {

    private static final String STARTDATE = "startDate";

    @Inject
    private MyConfig config;

    @Inject
    private CandleService candleService;

    /**
     * Get limited Learn Pairs
     * @param limit
     * @return 
     */
    public List<LearnPairDTO> get(int limit) {
        return config.getLearnPairlColl()
                .find()
                .sort(Sorts.ascending(STARTDATE))
                .limit(limit)
                .into(new ArrayList<>());
    }

    /**
     * Add one Learn Pair
     * @param dto 
     */
    public void add(LearnPairDTO dto) {
        config.getLearnPairlColl().insertOne(dto);
    }

    /**
     * Delete all Learn Pairs
     */
    public void deleteAll() {
        BasicDBObject document = new BasicDBObject();
        config.getLearnPairlColl().deleteMany(document);
    }

    public void generate(int scope, int minProfitPercent) {
        //Delete all 
        this.deleteAll();

        //Get all candles
        List<CandleDTO> candleList = candleService.get();
        for (CandleDTO candle : candleList) {

            LearnPairDTO learnPair = new LearnPairDTO(candle.getStartDate(), candle.getClose());

            Date maxDate = null;
            BigDecimal maxClose = BigDecimal.ZERO;
            BigDecimal maxProfit = BigDecimal.ZERO;

            //Get next 1000 candles
            List<CandleDTO> nextCandleList = candleService.geNexts(candle.getStartDate(), scope);
            for (CandleDTO next : nextCandleList) {

                BigDecimal profit = next.getClose().subtract(candle.getClose());

                if (profit.compareTo(maxProfit) == 1) {
                    maxDate = next.getStartDate();
                    maxClose = next.getClose();
                    maxProfit = profit;
                }
            }

            BigDecimal minProfit = (learnPair.getBuyClose().divide(BigDecimal.valueOf(100)).multiply(BigDecimal.valueOf(minProfitPercent)));

            if (minProfit.compareTo(maxProfit) == -1) {
                //Save result
                
                learnPair.setMinProfit(minProfit);
                learnPair.setSellDate(maxDate);
                learnPair.setSellClose(maxClose);
                learnPair.setProfit(maxProfit);
                this.add(learnPair);
            }
        }
    }
}
