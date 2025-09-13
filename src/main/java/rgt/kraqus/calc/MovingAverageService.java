package rgt.kraqus.calc;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.lte;
import com.mongodb.client.model.Sorts;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import rgt.kraqus.MyConfig;

/**
 * Calculate Moving Averages
 *
 * @author rgt
 */
@ApplicationScoped
public class MovingAverageService {

    private static final String STARTDATE = "startDate";

    @Inject
    MyConfig config;

    /**
     * Calculate Moving Average values
     */
    public void calculateMovingAverage() {
        List<CandleDTO> candleList;
        MovingAverageDTO ma;

        //Get the candles
        candleList = config.getCandleColl()
                .find(and(eq("calcCandle", true), eq("movingAverage.calcMovingAverage", false)))
                .sort(Sorts.ascending(STARTDATE))
                .into(new ArrayList<>());

        for (CandleDTO candle : candleList) {
            ma = candle.getMovingAverage();
            ma.setCalcMovingAverage(true);

            ma.setSma20(this.calcSMA(candle, 20, true).setScale(5, RoundingMode.HALF_UP));

            ma.setEma9(this.calcEMA(candle, 9, true).setScale(5, RoundingMode.HALF_UP));
            ma.setEma12(this.calcEMA(candle, 12, true).setScale(5, RoundingMode.HALF_UP));
            ma.setEma26(this.calcEMA(candle, 26, true).setScale(5, RoundingMode.HALF_UP));

            //Save candle
            config.getCandleColl().replaceOne(eq("_id", candle.getId()), candle);

        }
    }

    /**
     * Calculate Standard Moving Average
     *
     * @param dto
     * @param limit
     * @return
     */
    private BigDecimal calcSMA(CandleDTO dto, int limit, boolean isClose) {
        BigDecimal sum = BigDecimal.ZERO;
        int i = 0;

        //Get the candles
        List<CandleDTO> candleList = config.getCandleColl()
                .find(lte(STARTDATE, dto.getStartDate()))
                .sort(Sorts.descending(STARTDATE))
                .limit(limit)
                .into(new ArrayList<>());

        for (CandleDTO candle : candleList) {

            if (isClose) {
                //SUM from the Candle.Close
                sum = sum.add(candle.getClose());
            } else {
                //SUM from the MAXD Line
                sum = sum.add(candle.getMacd().getMacdLine());
            }

            i++;
        }

        if (i > 0) {
            return sum.divide(BigDecimal.valueOf(i), RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Exponential Moving Average
     *
     * @param dto
     * @param limit
     * @param isClose
     * @return
     */
    public BigDecimal calcEMA(CandleDTO dto, int limit, boolean isClose) {

        List<CandleDTO> candleList = config.getCandleColl()
                .find(lte(STARTDATE, dto.getStartDate()))
                .sort(Sorts.descending(STARTDATE))
                .limit(limit)
                .into(new ArrayList<>());

        if (candleList.size() == limit) {

            //get previous
            CandleDTO prev = config.getCandleColl()
                    .find(lt(STARTDATE, dto.getStartDate()))
                    .sort(Sorts.descending(STARTDATE))
                    .first();

            if (isClose) {
                //from Candle clsoe
                return this.calcEMAClose(dto, prev, limit, isClose);
            } else {
                //from MACD Line
                return this.calcEMAMacd(dto, prev, limit, isClose);
            }

        }
        return BigDecimal.ZERO;
    }

    /**
     * Sub calculation from the MACD.MACDLine
     *
     * @param dto
     * @param prev
     * @param limit
     * @param isClose
     * @return
     */
    private BigDecimal calcEMAMacd(CandleDTO dto, CandleDTO prev, int limit, boolean isClose) {
        if (prev.getMacd().getSignalLine().equals(BigDecimal.ZERO)) {
            //First element
            return this.calcSMA(dto, limit, isClose);
        } else {
            //Next elements
            BigDecimal smooth = BigDecimal.valueOf(2).divide(
                    BigDecimal.valueOf(limit).add(BigDecimal.ONE));
            BigDecimal prevEma = prev.getMacd().getSignalLine();

            return smooth.multiply(
                    dto.getMacd().getMacdLine().subtract(prevEma)).add(prevEma);
        }
    }

    /**
     * Sub calculation from the Candle.Close
     *
     * @param dto
     * @param prev
     * @param limit
     * @param isClose
     * @return
     */
    private BigDecimal calcEMAClose(CandleDTO dto, CandleDTO prev, int limit, boolean isClose) {
        if (prev.getMovingAverage().getEMA(limit).equals(BigDecimal.ZERO)) {
            //First element
            return this.calcSMA(dto, limit, isClose);
        } else {
            //Next elements
            BigDecimal smooth = BigDecimal.valueOf(2).divide(
                    BigDecimal.valueOf(limit).add(BigDecimal.ONE), MathContext.DECIMAL128);

            BigDecimal prevEma = prev.getMovingAverage().getEMA(limit);

            return smooth.multiply(
                    dto.getClose().subtract(prevEma)).add(prevEma);
        }
    }

}
