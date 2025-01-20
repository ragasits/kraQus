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
import rgt.kraqus.MyConfig;

/**
 * Commodity Channel Index (CCI)
 *
 * @author rgt
 */
@ApplicationScoped
public class CciService {

    private static final BigDecimal CONST = BigDecimal.valueOf(0.015);
    private static final String STARTDATE = "startDate";

    @Inject
    MyConfig config;

    /**
     * Calculate CCI
     */
    public void calculateCci() {
        BigDecimal sum;
        BigDecimal avg;
        BigDecimal mad;
        BigDecimal cci;

        List<CandleDTO> candleList = config.getCandleColl()
                .find(and(eq("calcCandle", true), eq("cci.calcCci", false)))
                .sort(Sorts.ascending(STARTDATE))
                .into(new ArrayList<>());

        for (CandleDTO candle : candleList) {
            // Typical Price
            BigDecimal typicalPrice = candle.getHigh()
                    .add(candle.getLow())
                    .add(candle.getClose())
                    .divide(BigDecimal.valueOf(3), RoundingMode.HALF_UP);

            candle.getCci().setTypicalPrice(typicalPrice);
            this.saveCci(candle);

            // 20-day SMA of TP
            List<CandleDTO> smaList = config.getCandleColl()
                    .find(lte(STARTDATE, candle.getStartDate()))
                    .sort(Sorts.descending(STARTDATE))
                    .limit(20)
                    .into(new ArrayList<>());

            sum = BigDecimal.ZERO;

            for (CandleDTO dto : smaList) {
                sum = sum.add(dto.getCci().getTypicalPrice());
            }

            avg = sum.divide(BigDecimal.valueOf(20), RoundingMode.HALF_UP);
            candle.getCci().setSma20Typical(avg);
            this.saveCci(candle);

            //20-day Mean Deviation
            List<CandleDTO> madList = config.getCandleColl()
                    .find(lte(STARTDATE, candle.getStartDate()))
                    .sort(Sorts.descending(STARTDATE))
                    .limit(20)
                    .into(new ArrayList<>());

            sum = BigDecimal.ZERO;
            for (CandleDTO dto : madList) {
                sum = sum.add(
                        avg.subtract(dto.getCci().getTypicalPrice()).abs()
                );
            }
            mad = sum.divide(BigDecimal.valueOf(20), RoundingMode.HALF_UP);

            candle.getCci().setMad20(mad);
            this.saveCci(candle);

            //20-day CCI
            cci = candle.getCci().getTypicalPrice()
                    .subtract(candle.getCci().getSma20Typical())
                    .divide(
                            CONST.multiply(candle.getCci().getMad20()),
                            RoundingMode.HALF_UP);

            if (cci.compareTo(BigDecimal.valueOf(100)) >= 0) {
                candle.getCci().setOverBought(true);
            }

            if (cci.compareTo(BigDecimal.valueOf(-100)) <= 0) {
                candle.getCci().setOverSold(true);
            }

            candle.getCci().setCci20(cci);
            candle.getCci().setCalcCci(true);
            this.saveCci(candle);

        }

    }

    /**
     * Save CCI into MongoDB
     * @param candle 
     */
    private void saveCci(CandleDTO candle) {
        config.getCandleColl()
                .replaceOne(eq("_id", candle.getId()), candle);
    }
}
