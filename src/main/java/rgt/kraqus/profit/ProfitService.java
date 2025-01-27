package rgt.kraqus.profit;

import static com.mongodb.client.model.Filters.eq;
import com.mongodb.client.model.Sorts;
import java.util.ArrayList;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import rgt.kraqus.MyConfig;
import rgt.kraqus.calc.CandleService;
import rgt.kraqus.learn.LearnService;

/**
 * Calculate profit
 *
 * @author rgt
 */
@ApplicationScoped
public class ProfitService {

    @Inject
    private MyConfig config;

    @Inject
    private LearnService learnService;

    @Inject
    private CandleService candleService;

    @Inject
    private ProfitCalcService profitCalcService;

    public List<String> getStrategyList() {
        List<String> stratList = new ArrayList<>();
        stratList.add("FirtSell");
        stratList.add("FirstProfit");
        stratList.add("RSI");
        stratList.add("FirstTreshold");
        return stratList;
    }

    /**
     * Get all data from profit collection
     *
     * @return
     */
    public List<ProfitDTO> get() {
        return config.getProfitColl()
                .find()
                .sort(Sorts.descending("eur"))
                .into(new ArrayList<>());
    }

    /**
     * Get profit filter by learnName
     *
     * @param learnName
     * @return
     */
    public List<ProfitDTO> get(String learnName) {
        return config.getProfitColl()
                .find(eq("learnName", learnName))
                .sort(Sorts.descending("eur"))
                .into(new ArrayList<>());
    }

    /**
     * Get profit filter by testNum
     *
     * @param testNum
     * @return
     */
    public ProfitDTO get(Long testNum) {
        return config.getProfitColl()
                .find(eq("testNum", testNum))
                .first();
    }

    /**
     * Get best profit
     *
     * @return
     */
    public ProfitDTO getBest() {
        return config.getProfitColl()
                .find()
                .sort(Sorts.descending("eur"))
                .first();
    }

    /**
     * Delete one profit
     *
     * @param dto
     */
    public void delete(ProfitDTO dto) {
        config.getProfitColl().deleteOne(eq("_id", dto.getId()));
    }

    /**
     * Delete all profits by learName
     *
     * @param learnName
     */
    public void delete(String learnName) {
        config.getProfitColl().deleteMany(
                eq("learnName", learnName)
        );
    }

    /**
     * Calculate profit, select strategy
     *
     * @param profit
     * @return
     */
    public ProfitDTO calcProfit(ProfitDTO profit) {
        switch (profit.getStrategy()) {
            case "FirtSell":
                return profitCalcService.calcFirtSell(profit);
            case "FirstProfit":
                return profitCalcService.calcFirstProfit(profit);
            case "FirstTreshold":
                return profitCalcService.calcTresholdProfit(profit);
            case "RSI":
                return profitCalcService.calcRSIBuySell(profit);
        }
        return null;
    }

}
