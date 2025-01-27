package rgt.kraqus.profit;

import com.mongodb.client.model.Sorts;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import rgt.kraqus.MyConfig;
import rgt.kraqus.calc.CandleDTO;
import rgt.kraqus.calc.CandleService;
import rgt.kraqus.learn.LearnDTO;
import rgt.kraqus.learn.LearnService;

/**
 *
 * @author rgt
 */
@ApplicationScoped
public class ProfitCalcService {

    private double eur;
    private double btc;
    private double lastEur;
    private Long testNum;
    private List<LearnDTO> learnList;
    private List<ProfitItemDTO> profitList;

    @Inject
    private MyConfig config;

    @Inject
    private LearnService learnService;

    @Inject
    private CandleService candleService;

    /**
     * Init calculator environment
     *
     * @param profit
     */
    private void init(ProfitDTO profit) {
        this.eur = 1000;
        this.btc = 0;
        this.lastEur = 0;
        this.testNum = 1L + this.getMaxTestNum();
        this.learnList = learnService.get(profit.getLearnName(), profit.getBuyDate(), profit.getSellDate());
        this.profitList = new ArrayList<>();
    }

    /**
     * Get MAX testNum value
     *
     * @return
     */
    private Long getMaxTestNum() {
        this.testNum = 0L;
        ProfitDTO dto = config.getProfitColl()
                .find()
                .sort(Sorts.descending("testNum"))
                .first();
        if (dto != null && dto.getTestNum() != null) {
            testNum = dto.getTestNum();
        }
        return testNum;
    }

    /**
     * Store Profit in MongoDB
     *
     * @param profit
     */
    private void storeProfit(ProfitDTO profit) {
        //Store profit
        profit.setTestNum(testNum);
        profit.setEur(lastEur);
        profit.setItems(profitList);
        config.getProfitColl().insertOne(profit);
    }

    /**
     * Calculate sell item values
     *
     * @param dto
     */
    private void sellItem(ProfitItemDTO dto) {
        dto.sellBtc(btc);
        eur = dto.getEur();
        lastEur = eur;
        btc = dto.getBtc();
        profitList.add(dto);
    }

    /**
     * Calculate buy item values
     *
     * @param dto
     */
    private void buyItem(ProfitItemDTO dto) {
        dto.buyBtc(eur);
        eur = dto.getEur();
        btc = dto.getBtc();
        profitList.add(dto);
    }

    /**
     * Calculate profit - strategy: First sell
     *
     * @param profit
     * @return
     */
    public ProfitDTO calcFirtSell(ProfitDTO profit) {
        this.init(profit);

        for (LearnDTO learn : learnList) {
            CandleDTO candle = candleService.get(learn.getStartDate());
            ProfitItemDTO dto = new ProfitItemDTO(candle, learn.getTrade(), testNum);

            switch (learn.getTrade()) {
                case ProfitDTO.BUY:
                    if (eur > 0) {
                        buyItem(dto);
                    }
                    break;
                case ProfitDTO.SELL:
                    if (btc > 0) {
                        sellItem(dto);
                    }
                    break;
                case ProfitDTO.NONE:
                    break;
            }
        }
        storeProfit(profit);
        return profit;
    }

    /**
     * alculate profit - strategy: RSI
     *
     * @param profit
     * @return
     */
    public ProfitDTO calcRSIBuySell(ProfitDTO profit) {
        this.init(profit);

        for (LearnDTO learn : learnList) {
            CandleDTO candle = candleService.get(learn.getStartDate());
            ProfitItemDTO dto = new ProfitItemDTO(candle, learn.getTrade(), testNum);

            switch (learn.getTrade()) {
                case ProfitDTO.BUY:
                    if (eur > 0 && candle.getRsi().isRsiBuy()) {
                        buyItem(dto);
                    }
                    break;
                case ProfitDTO.SELL:
                    if (btc > 0 && candle.getRsi().isRsiSell()) {
                        sellItem(dto);
                    }
                    break;
                case ProfitDTO.NONE:
                    break;
            }
        }

        storeProfit(profit);
        return profit;
    }

    /**
     * *
     * Calculate profit - strategy: Fisrt profit (buy<sell)
     *
     * @param profit
     * @return
     */
    public ProfitDTO calcFirstProfit(ProfitDTO profit) {
        CandleDTO candle;
        ProfitItemDTO profitDto;

        this.init(profit);

        LearnDTO buyLearn = null;
        LearnDTO sellLearn = null;

        for (LearnDTO learnDto : learnList) {

            //Fist Buy
            if (learnDto.isBuy() && buyLearn == null) {
                buyLearn = learnDto;

                //Buy
                candle = candleService.get(buyLearn.getStartDate());
                profitDto = new ProfitItemDTO(candle, buyLearn.getTrade(), testNum);
                buyItem(profitDto);
            }

            //First Profit (sell>buy)
            if (learnDto.isSell() && buyLearn != null && sellLearn == null) {
                if (buyLearn.getClose().compareTo(learnDto.getClose()) == -1) {
                    sellLearn = learnDto;
                }
            }

            //Calculate profit
            if (buyLearn != null && sellLearn != null) {

                //Sell
                candle = this.candleService.get(sellLearn.getStartDate());
                profitDto = new ProfitItemDTO(candle, sellLearn.getTrade(), testNum);
                sellItem(profitDto);

                //reset pair
                buyLearn = null;
                sellLearn = null;
            }
        }

        storeProfit(profit);
        return profit;
    }

    /**
     * Calculate profit - strategy: Treshold (sell-buy>treshold)
     *
     * @param profit
     * @return
     */
    public ProfitDTO calcTresholdProfit(ProfitDTO profit) {
        CandleDTO candle;
        ProfitItemDTO profitDto;

        init(profit);

        LearnDTO buyLearn = null;
        LearnDTO sellLearn = null;

        for (LearnDTO learnDto : learnList) {

            //Fist Buy
            if (learnDto.isBuy() && buyLearn == null) {
                buyLearn = learnDto;
                //Buy
                candle = this.candleService.get(buyLearn.getStartDate());
                profitDto = new ProfitItemDTO(candle, buyLearn.getTrade(), testNum);
                buyItem(profitDto);
            }

            //First Profit (sell>buy)
            if (learnDto.isSell() && this.sellCondition(buyLearn, sellLearn, learnDto, profit.getTreshold())) {
                sellLearn = learnDto;
            }

            //Calculate profit
            if (buyLearn != null && sellLearn != null) {
                //Sell
                candle = this.candleService.get(sellLearn.getStartDate());
                profitDto = new ProfitItemDTO(candle, sellLearn.getTrade(), testNum);
                sellItem(profitDto);

                //reset pair
                buyLearn = null;
                sellLearn = null;
            }
        }

        storeProfit(profit);
        return profit;
    }

    /**
     * Calculate sell Condition (Treshold)
     *
     * @param buyLearn
     * @param sellLearn
     * @param learnDto
     * @param treshold
     * @return
     */
    private boolean sellCondition(LearnDTO buyLearn, LearnDTO sellLearn, LearnDTO learnDto, Integer treshold) {
        if (buyLearn != null && sellLearn == null) {

            CandleDTO candle = this.candleService.get(buyLearn.getStartDate());
            BigDecimal minProfit = candle.getClose()
                    .divide(BigDecimal.valueOf(100L), 0, RoundingMode.CEILING)
                    .multiply(BigDecimal.valueOf(treshold));

            BigDecimal diff = learnDto.getClose()
                    .subtract(buyLearn.getClose());

            if (diff.signum() > 0 && diff.compareTo(minProfit) > 0) {
                return true;
            }
        }
        return false;
    }

}
