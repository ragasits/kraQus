package rgt.kraqus.calc;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.lte;
import com.mongodb.client.model.Sorts;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import rgt.kraqus.MyConfig;

/**
 * Calculate Average True Range (ATR) indicator
 * Using ATR period 14 and RMA smoothing
 *
 * Based on J. Welles Wilder Jr. method
 *
 * @author rgt
 */
@ApplicationScoped
public class AtrService {

    static final int ATR_PERIOD = 14;
    static final String STARTDATE = "startDate";

    @Inject
    CandleService candleService;

    @Inject
    MyConfig config;

    /**
     * Calculate ATR for candles with ATR not yet calculated
     */
    public void calculateAtr() {
        List<CandleDTO> candleList;
        CandleDTO prev;

        // Get candles that need ATR calculation
        candleList = config.getCandleColl()
                .find(and(eq("calcCandle", true), eq("atr.calcAtr", false)))
                .sort(Sorts.ascending(STARTDATE))
                .into(new ArrayList<>());

        if (candleList.size() < ATR_PERIOD + 1) {
            // Not enough data
            return;
        }

        int index = 0;
        for (CandleDTO candle : candleList) {
            AtrDTO atrDto = candle.getAtr();

            // Replace above query with getPrev
            prev = candleService.getPrev(candle.getStartDate());

            // Calculate True Range
            BigDecimal trueRange = calculateTrueRange(candle, prev);

            // Ensure candle has a MovingAverageDTO
            MovingAverageDTO maDto = candle.getMovingAverage();
            if (maDto == null) {
                maDto = new MovingAverageDTO();
            }

            // First ATR value (index == ATR_PERIOD) calculated by simple average
            if (prev == null) {
                // If no previous, just save true range as ATR (usually first candle)
                if (atrDto == null) {
                    atrDto = new AtrDTO(index, trueRange, trueRange);
                } else {
                    atrDto.setTrueRange(trueRange);
                    atrDto.setAtrValue(trueRange);
                }
                candle.setAtr(atrDto);

                // Set ATR value into MovingAverageDTO
                //maDto.setAtr14(trueRange);
                candle.setMovingAverage(maDto);

                saveAtr(candle);
                index++;
                continue;
            }

            if (index == ATR_PERIOD) {
                // Compute initial ATR as average of first ATR_PERIOD true ranges
                List<CandleDTO> initialCandles = config.getCandleColl()
                        .find(lte(STARTDATE, candle.getStartDate()))
                        .sort(Sorts.descending(STARTDATE))
                        .limit(ATR_PERIOD)
                        .into(new ArrayList<>());

                BigDecimal sumTr = BigDecimal.ZERO;
                for (CandleDTO c : initialCandles) {
                    AtrDTO cAtr = c.getAtr();
                    if (cAtr == null || cAtr.getTrueRange() == null) {
                        // Calculate true range if missing
                        CandleDTO prevForTr = config.getCandleColl()
                                .find(lt(STARTDATE, c.getStartDate()))
                                .sort(Sorts.descending(STARTDATE))
                                .first();
                        cAtr = new AtrDTO(index, calculateTrueRange(c, prevForTr), null);
                    }
                    sumTr = sumTr.add(cAtr.getTrueRange());
                }

                BigDecimal atrValue = sumTr.divide(BigDecimal.valueOf(ATR_PERIOD), RoundingMode.HALF_UP);
                atrDto.setTrueRange(trueRange);
                atrDto.setAtrValue(atrValue);
                candle.setAtr(atrDto);

                // Set ATR value into MovingAverageDTO
                //maDto.setAtr14(atrValue);
                candle.setMovingAverage(maDto);

            } else if (index > ATR_PERIOD) {
                AtrDTO prevAtr = prev.getAtr();
                if (prevAtr == null || prevAtr.getAtrValue() == null) {
                    index++;
                    continue; // Skip if we cannot calculate
                }
                BigDecimal prevAtrValue = prevAtr.getAtrValue();
                BigDecimal atrValue = prevAtrValue.multiply(BigDecimal.valueOf(ATR_PERIOD - 1))
                        .add(trueRange)
                        .divide(BigDecimal.valueOf(ATR_PERIOD), RoundingMode.HALF_UP);

                atrDto.setTrueRange(trueRange);
                atrDto.setAtrValue(atrValue);
                candle.setAtr(atrDto);

                // Set ATR value into MovingAverageDTO
                //maDto.setAtr14(atrValue);
                candle.setMovingAverage(maDto);
            } else {
                atrDto.setTrueRange(trueRange);
                atrDto.setAtrValue(null);
                candle.setAtr(atrDto);

                // Set zero if ATR undefined
                //maDto.setAtr14(BigDecimal.ZERO);
                candle.setMovingAverage(maDto);
            }

            saveAtr(candle);
            index++;
        }
    }

    /**
     * Calculate the True Range for the candle given previous candle
     */
    private BigDecimal calculateTrueRange(CandleDTO candle, CandleDTO prev) {
        if (prev == null) {
            return candle.getHigh().subtract(candle.getLow());
        }

        BigDecimal tr1 = candle.getHigh().subtract(candle.getLow()).abs();
        BigDecimal tr2 = candle.getHigh().subtract(prev.getClose()).abs();
        BigDecimal tr3 = candle.getLow().subtract(prev.getClose()).abs();

        return tr1.max(tr2).max(tr3);
    }

    /**
     * Save candle with ATR info updated
     */
    private void saveAtr(CandleDTO candle) {
        candle.getAtr().setCalcAtr(true);
        config.getCandleColl()
                .replaceOne(eq("_id", candle.getId()), candle);
    }
}
