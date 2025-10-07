package rgt.kraqus.learn;

import com.mongodb.BasicDBObject;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import com.mongodb.client.model.Sorts;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lte;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import org.primefaces.component.knob.KnobBase;
import rgt.kraqus.MyConfig;
import rgt.kraqus.calc.CandleDTO;
import rgt.kraqus.calc.CandleService;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

/**
 * Learn Pairs Service
 *
 * @author rgt
 */
@ApplicationScoped
public class LearnPairService {

    private static final String BUYDATE = "buyDate";
    private static final String PROFIT = "profit";

    private final AtomicBoolean isExecuting = new AtomicBoolean(false);

    @Inject
    private MyConfig config;

    @Inject
    private CandleService candleService;

    @Inject
    private LearnService learnService;

    /**
     * Get limited Learn Pairs
     *
     * @param limit
     * @return
     */
    public List<LearnPairDTO> get(int limit) {
        return config.getLearnPairlColl()
                .find()
                .sort(Sorts.ascending(BUYDATE))
                .limit(limit)
                .into(new ArrayList<>());
    }

    /**
     * Get the all LearnPairs Order by buyDate Desc
     *
     * @return
     */
    public List<LearnPairDTO> get() {
        return config.getLearnPairlColl()
                .find()
                .sort(Sorts.ascending(BUYDATE))
                .into(new ArrayList<>());
    }

    /**
     * Get LearnPairs where greater than buyDate Order by buyDate
     *
     * @param buytDate
     * @return
     */
    public List<LearnPairDTO> get(Date buytDate) {
        return config.getLearnPairlColl()
                .find(gt(BUYDATE, buytDate))
                .sort(Sorts.ascending(BUYDATE))
                .into(new ArrayList<>());
    }

    /**
     * Get Learn Pairs where learn is true / false
     *
     * @param learn
     * @return
     */
    private List<LearnPairDTO> get(boolean learn) {
        return config.getLearnPairlColl()
                .find(eq("learn", learn))
                .sort(Sorts.ascending(BUYDATE))
                .into(new ArrayList<>());
    }

    /**
     * Get the first LearnPair
     *
     * @return
     */
    private LearnPairDTO getFirst() {
        return config.getLearnPairlColl()
                .find()
                .sort(Sorts.ascending(BUYDATE))
                .first();
    }

    /**
     * Get firsts LearnPair after the date
     *
     * @param date
     * @return
     */
    private LearnPairDTO getFirstAfter(Date date) {
        return config.getLearnPairlColl()
                .find(gte(BUYDATE, date))
                .sort(Sorts.ascending(BUYDATE))
                .first();
    }

    /**
     * Add one Learn Pair
     *
     * @param dto
     */
    public void add(LearnPairDTO dto) {
        config.getLearnPairlColl().insertOne(dto);
    }

    /**
     * Update LearnPair
     *
     * @param dto
     */
    public void update(LearnPairDTO dto) {
        config.getLearnPairlColl().replaceOne(
                eq("_id", dto.getId()), dto);
    }

    /**
     * Delete all Learn Pairs
     */
    public void deleteAll() {
        BasicDBObject document = new BasicDBObject();
        config.getLearnPairlColl().deleteMany(document);
    }

    /**
     * We're looking for the bests buy-sell items without overlap
     */
    public void bestLearns() {

        if (isExecuting.compareAndSet(false, true)) {
            try {
                LearnPairDTO pair = this.getFirst();

                while (pair != null) {

                    Log.debug("first:" + pair.getBuyDate() + "-" + pair.getSellDate());

                    //Get the overlaps
                    LearnPairDTO overlap = config.getLearnPairlColl()
                            .find(and(gte(BUYDATE, pair.getBuyDate()), lte(BUYDATE, pair.getSellDate())))
                            .sort(Sorts.descending(PROFIT))
                            .first();

                    Log.debug("overlap:" + overlap.getBuyDate() + "-" + overlap.getSellDate());

                    if (overlap != null) {
                        overlap.setLearn(true);
                        this.update(overlap);

                        pair = this.getFirstAfter(overlap.getSellDate());
                    } else {
                        pair = null;
                    }

                }
            } finally {
                isExecuting.set(false);
            }

        } else {
            Log.info("Method already executing: generate");
        }

    }

    /**
     * Generate buy-sell positions
     *
     * @param scope
     * @param minProfitPercent
     */
    public void generate(int scope, int minProfitPercent) {

        if (isExecuting.compareAndSet(false, true)) {
            try {

                //Delete all 
                this.deleteAll();

                Log.info("Delete all: OK");

                //Get all candles
                List<CandleDTO> candleList = candleService.get();

                for (CandleDTO candle : candleList) {

                    LearnPairDTO learnPair = new LearnPairDTO(candle.getStartDate(), candle.getClose());

                    Date maxDate = null;
                    BigDecimal maxClose = BigDecimal.ZERO;
                    BigDecimal maxProfit = BigDecimal.ZERO;

                    //Get next 1000 candles
                    List<CandleDTO> nextCandleList = candleService.getNexts(candle.getStartDate(), scope);
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

                Log.info("Generate: OK");

            } finally {
                isExecuting.set(false);
            }
        } else {
            Log.info("Method already executing: generate");
        }

    }

    /**
     * Save generated learns
     *
     * @param learnName
     * @return
     */
    public String saveLearn(String learnName) {

        //Delete the existing learns
        learnService.delete(learnName);

        List<LearnPairDTO> pairList = this.get(true);
        for (LearnPairDTO pair : pairList) {

            //Buy            
            LearnDTO dto = new LearnDTO();
            dto.setName(learnName);
            dto.setStartDate(pair.getBuyDate());
            dto.setClose(pair.getBuyClose());
            dto.setTrade("buy");
            learnService.add(dto);

            //Sell
            dto = new LearnDTO();
            dto.setName(learnName);
            dto.setStartDate(pair.getSellDate());
            dto.setClose(pair.getSellClose());
            dto.setTrade("sell");
            learnService.add(dto);

        }

        return "Save " + learnName + ": OK";
    }

    /**
     * Converts all LearnPairDTOs to Weka Instances.
     *
     * @return Instances containing all LearnPairDTO data.
     */
    public Instances toInstances() {
        Instances instances = new Instances("learnPair", LearnPairDTO.getAttributes(), 0);

        for (LearnPairDTO dto : this.get()) {
            DenseInstance instance = dto.getValues(instances);
            instances.add(instance);
        }
        return instances;
    }
}