package rgt.kraqus.calc;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.lte;
import com.mongodb.client.model.Sorts;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import rgt.kraqus.MyCommon;
import rgt.kraqus.MyConfig;

/**
 * Calculate Bollinger values
 *
 * @author rgt
 */
@ApplicationScoped
public class BollingerService {

    @Inject
    MyConfig config;

    /**
     * Calculate Bollinger values + Delta + Trend
     *
     */
    public void calculateBollinger() {
        List<CandleDTO> candleList;
        BollingerDTO bollinger;

        //Get the candles
        candleList = config.getCandleColl()
                .find(and(eq("calcCandle", true), eq("bollinger.calcBollinger", false)))
                .into(new ArrayList<>());

        for (CandleDTO candle : candleList) {
            bollinger = candle.getBollinger();
            bollinger.setCalcBollinger(true);
            bollinger.setStDev(this.calcStDev(candle));

            bollinger.setBollingerUpper(candle.getMovingAverage().getSma20().add(bollinger.getStDev().multiply(BigDecimal.valueOf(2))));
            bollinger.setBollingerLower(candle.getMovingAverage().getSma20().subtract(bollinger.getStDev().multiply(BigDecimal.valueOf(2))));

            bollinger.setBollingerBandWidth(bollinger.getBollingerUpper().subtract(bollinger.getBollingerLower()));

            // Calculate trade upper value
            bollinger.setTradeUpper(calcTradeUpper(candle.getClose(), bollinger.getBollingerUpper()));

            //Calculate trade lower value
            bollinger.setTradeLower(calcTradeLower(candle.getClose(), bollinger.getBollingerLower()));

            //Calculate Buy / Sell
            bollinger.setBollingerSell(bollinger.getTradeUpper().compareTo(BigDecimal.ZERO) != 0);
            bollinger.setBollingerBuy(bollinger.getTradeLower().compareTo(BigDecimal.ZERO) != 0);

            //Save candle
            config.getCandleColl().replaceOne(eq("_id", candle.getId()), candle);

        }
    }

    /**
     * Calculate trade lower value
     *
     * @param close
     * @param lower
     * @return
     */
    private BigDecimal calcTradeLower(BigDecimal close, BigDecimal lower) {
        if (close.compareTo(lower) < 0) {
            return lower.subtract(close);
        } else {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Calculate trade upper value
     *
     * @param close
     * @param upper
     * @return
     */
    private BigDecimal calcTradeUpper(BigDecimal close, BigDecimal upper) {
        if (close.compareTo(upper) > 0) {
            return close.subtract(upper);
        } else {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Calculate Standard Deviation
     *
     * @param dto
     * @return
     */
    private BigDecimal calcStDev(CandleDTO dto) {
        BigDecimal stdev = BigDecimal.ZERO;
        int i = 0;

        //Get the candles
        List<CandleDTO> candleList = config.getCandleColl()
                .find(lte("startDate", dto.getStartDate()))
                .sort(Sorts.descending("startDate"))
                .limit(20)
                .into(new ArrayList<>());

        for (CandleDTO candle : candleList) {
            //Standard Deviation
            stdev = stdev.add(candle.getClose().subtract(dto.getMovingAverage().getSma20()).pow(2));
            i++;
        }

        if (i > 0) {
            return MyCommon.sqrt(stdev.divide(BigDecimal.valueOf(i), 5, RoundingMode.HALF_UP), 2);
        }
        return BigDecimal.ZERO;
    }

}