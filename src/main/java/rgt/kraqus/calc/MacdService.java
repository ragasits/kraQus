package rgt.kraqus.calc;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import com.mongodb.client.model.Sorts;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import rgt.kraqus.MyConfig;

/**
 * Calculate and store MACD elements
 *
 * @author rgt
 */
@ApplicationScoped
public class MacdService {

    @Inject
    MyConfig config;

    @Inject
    MovingAverageService maService;

    @Inject
    CandleService candleService;

    /**
     * Calculate MACD
     */
    public void calculateMacd() {

        List<CandleDTO> candleList = config.getCandleColl()
                .find(and(eq("calcCandle", true), eq("macd.calcMacd", false)))
                .sort(Sorts.ascending("startDate"))
                .into(new ArrayList<>());

        for (CandleDTO candle : candleList) {
            MacdDTO macd = candle.getMacd();
            macd.setCalcMacd(true);

            //MACD Line: (12-day EMA - 26-day EMA)
            macd.setMacdLine(candle
                    .getMovingAverage()
                    .getEma12()
                    .subtract(candle
                            .getMovingAverage()
                            .getEma26())
                    .setScale(5, RoundingMode.HALF_UP));

            //Signal Line: 9-day EMA of MACD Line
            macd.setSignalLine(maService.calcEMA(candle, 9, false).setScale(5, RoundingMode.HALF_UP));

            //MACD Histogram: MACD Line - Signal Line
            macd.setMacdHistogram(macd.getMacdLine().subtract(macd.getSignalLine()).setScale(5, RoundingMode.HALF_UP));

            //Flags
            macd.setBullMarket(macd.getMacdHistogram().compareTo(BigDecimal.ZERO) == 1);
            macd.setBearMarket(macd.getMacdHistogram().compareTo(BigDecimal.ZERO) == -1);

            //Crossover
            CandleDTO prev = candleService.getPrev(candle.getStartDate());
            if (prev != null) {
                double hist1 = prev.getMacd().getMacdHistogram().doubleValue();
                double hist2 = macd.getMacdHistogram().doubleValue();

                if ((hist1 > 0d && hist2 < 0d) || (hist1 < 0d && hist2 > 0d)) {
                    macd.setCrossover(true);
                }
            }

            //Save candle
            config.getCandleColl().replaceOne(eq("_id", candle.getId()), candle);
        }
    }

}
