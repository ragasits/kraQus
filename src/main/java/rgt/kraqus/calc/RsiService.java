package rgt.kraqus.calc;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.lte;
import com.mongodb.client.model.Sorts;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import rgt.kraqus.MyConfig;

/**
 * Calculate Relative Strength Index (RSI) indicator
 *
 * @author rgt
 */
@ApplicationScoped
public class RsiService {

    static final BigDecimal RSIDAY = BigDecimal.valueOf(14);
    static final BigDecimal RSIUP = BigDecimal.valueOf(70);
    static final BigDecimal RSIDOWN = BigDecimal.valueOf(30);
    static final String STARTDATE = "startDate";

    @Inject
    MyConfig config;

    /**
     * Calculate RSI
     */
    public void calculateRsi() {
        List<CandleDTO> candleList;
        CandleDTO prev;
        RsiDTO rsi;

        //Get the candles
        candleList = config.getCandleColl()
                .find(and(eq("calcCandle", true), eq("rsi.calcRsi", false)))
                .sort(Sorts.ascending(STARTDATE))
                .into(new ArrayList<>());

        for (CandleDTO candle : candleList) {
            rsi = candle.getRsi();

            //Get the prev candle
            prev = config.getCandleColl()
                    .find(lt(STARTDATE, candle.getStartDate()))
                    .sort(Sorts.descending(STARTDATE))
                    .first();

            //First element
            if (prev == null) {
                this.saveRsi(candle);
                continue;
            } else {
                //Set change
                rsi.setChange(candle.getClose().subtract(prev.getClose()));
            }

            //Set Gain, Loss
            if (rsi.getChange() != null) {
                if (rsi.getChange().compareTo(BigDecimal.ZERO) == 1) {
                    rsi.setGain(rsi.getChange());
                } else if (rsi.getChange().compareTo(BigDecimal.ZERO) == -1) {
                    rsi.setLoss(rsi.getChange().abs());
                }
            }

            //Calc AVG Gain, Loss
            double d = prev.getRsi().getAvgGain().add(prev.getRsi().getAvgLoss()).doubleValue();
            if (d > 0) {
                rsi.setAvgGain(prev.getRsi().getAvgGain()
                        .multiply(BigDecimal.valueOf(13))
                        .add(candle.getRsi().getGain())
                        .divide(RSIDAY, RoundingMode.HALF_UP)
                );

                rsi.setAvgLoss(prev.getRsi().getAvgLoss()
                        .multiply(BigDecimal.valueOf(13))
                        .add(candle.getRsi().getLoss())
                        .divide(RSIDAY, RoundingMode.HALF_UP)
                );
            } else {
                ArrayList<CandleDTO> avgList = config.getCandleColl()
                        .find(lte(STARTDATE, candle.getStartDate()))
                        .sort(Sorts.descending(STARTDATE))
                        .limit(RSIDAY.intValue())
                        .into(new ArrayList<>());

                if (avgList.size() == RSIDAY.intValue()) {
                    BigDecimal sumGain = BigDecimal.ZERO;
                    BigDecimal sumLoss = BigDecimal.ZERO;

                    for (CandleDTO dto : avgList) {
                        sumGain = sumGain.add(dto.getRsi().getGain());
                        sumLoss = sumLoss.add(dto.getRsi().getLoss());

                    }
                    rsi.setAvgGain(sumGain.divide(RSIDAY, RoundingMode.HALF_UP));
                    rsi.setAvgLoss(sumLoss.divide(RSIDAY, RoundingMode.HALF_UP));
                }

            }

            //Calc RS
            boolean calcRs = false;
            if (rsi.getAvgLoss().doubleValue() != 0) {
                rsi.setRs(
                        rsi.getAvgGain().divide(rsi.getAvgLoss(), RoundingMode.HALF_UP)
                );
                calcRs = true;
            }

            //Calc RSI
            BigDecimal hundred = new BigDecimal(100);

            if (calcRs && rsi.getAvgLoss().doubleValue() == 0) {
                rsi.setRsi(hundred);
            } else {
                BigDecimal b = BigDecimal.ONE.add(rsi.getRs());
                b = hundred.divide(b, 2, RoundingMode.HALF_UP);
                b = hundred.subtract(b);
                rsi.setRsi(b);
            }

            //Buy, Sell
            if (rsi.getRsi().compareTo(RsiService.RSIUP) > 0) {
                rsi.setRsiSell(true);
            } else if (rsi.getRsi().compareTo(RSIDOWN) < 0) {
                rsi.setRsiBuy(true);
            }

            candle.setRsi(rsi);

            //Save candle
            this.saveRsi(candle);

        }
    }

    /**
     * Update RSI
     *
     * @param candle
     */
    private void saveRsi(CandleDTO candle) {
        candle.getRsi().setCalcRsi(true);

        config.getCandleColl()
                .replaceOne(eq("_id", candle.getId()), candle);
    }

}
