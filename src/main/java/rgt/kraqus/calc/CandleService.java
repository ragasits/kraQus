package rgt.kraqus.calc;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.lte;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.bson.types.ObjectId;
import rgt.kraqus.MyConfig;
import rgt.kraqus.MyException;
import rgt.kraqus.get.TradePairDTO;

/**
 * Calculate and store candle elements
 *
 * @author rgt
 */
@ApplicationScoped
public class CandleService {

    private static final String STARTDATE = "startDate";
    private static final String TIMEDATE = "timeDate";

    @Inject
    MyConfig config;

    @Inject
    MovingAverageService maService;

    @Inject
    BollingerService bollingerService;

    @Inject
    RsiService rsiService;

    @Inject
    MacdService macdService;

    @Inject
    CciService cciService;

    /**
     * Get all Candles
     * @return 
     */
    public List<CandleDTO> get() {
        return this.config.getCandleColl()
                .find()
                .sort(Sorts.ascending(STARTDATE))
                .into(new ArrayList<>());

    }
    
    
    /**
     * Get one Candle by startDate
     *
     * @param startDate
     * @return
     */
    public CandleDTO get(Date startDate) {
        return this.config.getCandleColl()
                .find(eq(STARTDATE, startDate))
                .first();
    }

    /**
     * Get one Candle by ID
     *
     * @param id
     * @return
     */
    public CandleDTO get(ObjectId id) {
        return config.getCandleColl()
                .find(eq("_id", id))
                .first();
    }

    /**
     * Get Candles
     *
     * @param first
     * @param last
     * @return
     */
    public List<CandleDTO> get(Date first, Date last) {
        return config.getCandleColl()
                .find(and(gte(STARTDATE, first), lte(STARTDATE, last)))
                .sort(Sorts.ascending(STARTDATE))
                .into(new ArrayList<>());
    }

    /**
     * Get first date from the candle collection
     *
     * @return
     */
    public Date getFirstDate() {
        CandleDTO dto = config.getCandleColl()
                .find()
                .sort(Sorts.ascending(STARTDATE))
                .first();

        if (dto == null) {
            return null;
        }

        return dto.getStartDate();
    }

    /**
     * Get latest date value from Candle collection
     *
     * @return
     */
    public Date getLatesDate() {
        CandleDTO dto = config.getCandleColl()
                .find()
                .sort(Sorts.descending(STARTDATE))
                .first();

        if (dto == null) {
            return null;
        }
        return dto.getStartDate();
    }

    /**
     * Get one day's all Candles
     *
     * @param startDate
     * @return
     */
    public List<CandleDTO> getOneDay(Date startDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.add(Calendar.DAY_OF_YEAR, 1);
        Date stopDate = cal.getTime();

        return config.getCandleColl()
                .find(and(gte(STARTDATE, startDate), lt(STARTDATE, stopDate)))
                .sort(Sorts.ascending(STARTDATE))
                .into(new ArrayList<>());

    }

    /**
     * get last "limit" size candles
     *
     * @param limit
     * @return
     */
    public List<CandleDTO> getLasts(int limit) {
        return config.getCandleColl()
                .find()
                .sort(Sorts.descending(STARTDATE))
                .limit(limit)
                .into(new ArrayList<>());
    }

    /**
     * get candles last "limit" size from startDate
     *
     * @param startDate
     * @param limit
     * @return
     */
    public List<CandleDTO> getLasts(Date startDate, int limit) {
        return config.getCandleColl()
                .find(lt(STARTDATE, startDate))
                .sort(Sorts.descending(STARTDATE))
                .limit(limit)
                .into(new ArrayList<>());
    }
    
    /**
     * Get limited Candles from startDate
     * @param startDate
     * @param limit
     * @return 
     */
    public List<CandleDTO> geNexts(Date startDate, int limit) {
        return config.getCandleColl()
                .find(gt(STARTDATE, startDate))
                .sort(Sorts.ascending(STARTDATE))
                .limit(limit)
                .into(new ArrayList<>());
    }    

    /**
     * Delete the last candle
     */
    public void deleteLastCandle() {
        CandleDTO dto = this.getLast();

        if (dto != null) {
            config.getCandleColl()
                    .deleteOne(eq("_id", dto.getId()));
        }
    }

    /**
     * Delete empty candles open = 0
     */
    private void deleteEmptyCandles() {
        DeleteResult result = config.getCandleColl()
                .deleteMany(
                        and(
                                eq("calcCandle", true),
                                eq("open", 0)
                        )
                );

        Log.info("deleteEmptyCandles: " + result.getDeletedCount());
    }

    /**
     * Get last inserted candle
     *
     * @return
     */
    public CandleDTO getLast() {
        return config.getCandleColl()
                .find()
                .sort(Sorts.descending(STARTDATE))
                .first();
    }

    /**
     * Get previous candle
     *
     * @param startDate
     * @return
     */
    public CandleDTO getPrev(Date startDate) {
        return this.config.getCandleColl()
                .find(lt(STARTDATE, startDate))
                .sort(Sorts.descending(STARTDATE))
                .limit(1)
                .first();
    }

    /**
     * Get first Candle
     *
     * @return
     */
    public CandleDTO getFirst() {
        return this.config.getCandleColl()
                .find()
                .sort(Sorts.ascending(STARTDATE))
                .first();
    }

    /**
     * Calculate Start date Get latest Date from the candle collection If it
     * does not exist, get the first from the tradepair and adjust it to 0 or 30
     * minute
     *
     * @return
     * @throws rgt.kraqus.MyException
     */
    public Date getStartDate() throws MyException {
        Date startDate;

        if (config.getCandleColl().countDocuments() > 0) {
            CandleDTO dto = config.getCandleColl().find()
                    .sort(Sorts.descending(STARTDATE))
                    .first();

            if (dto == null) {
                throw new MyException("Missing: startDate");
            }

            startDate = dto.getStartDate();

            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            cal.add(Calendar.MINUTE, 30);
            startDate = cal.getTime();

        } else {
            TradePairDTO dto = config.getTradePairColl().find()
                    .sort(Sorts.ascending(TIMEDATE))
                    .first();

            if (dto == null) {
                throw new MyException("Missing: startDate");
            }

            startDate = dto.getTimeDate();
        }
        return this.calcCandle30Min(startDate);
    }

    /**
     * Calculate 30min dates
     *
     * @param date
     * @return
     */
    public Date calcCandle30Min(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int minute = cal.get(Calendar.MINUTE);

        // Set 30 minutes candle
        if (minute < 30) {
            cal.set(Calendar.MINUTE, 0);
        } else {
            cal.set(Calendar.MINUTE, 30);
        }

        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Run candle generation methods in production mode
     */
    public void callCandleProd() {
        this.calcCandle();
        this.deleteEmptyCandles();
        maService.calculateMovingAverage();
        bollingerService.calculateBollinger();
        rsiService.calculateRsi();
        macdService.calculateMacd();
        cciService.calculateCci();
    }

    /**
     * Calculate candle values Max 5k rows (Prevent the timeout?)
     */
    private void calcCandle() {
        int i = 0;
        Date lastDate;

        List<CandleDTO> candleList = config.getCandleColl()
                .find(eq("calcCandle", false))
                .sort(Sorts.ascending(STARTDATE))
                //.limit(5000)
                .into(new ArrayList<>());

        for (CandleDTO dto : candleList) {
            this.calcCandleItem(dto);
            i++;
        }

        lastDate = candleList.get(candleList.size() - 1).getStartDate();
        Log.info("calcCandle: " + i + " " + lastDate);

    }

    /**
     * Calculate CandelItem values
     *
     * @param dto
     */
    private void calcCandleItem(CandleDTO dto) {
        TradePairDTO trade;

        FindIterable<TradePairDTO> result = config.getTradePairColl()
                .find(and(gte(TIMEDATE, dto.getStartDate()), lt(TIMEDATE, dto.getStopDate())))
                .sort(Sorts.ascending(TIMEDATE));
        MongoCursor<TradePairDTO> cursor = result.iterator();

        Integer count = 0;
        Integer countBuy = 0;
        Integer countSell = 0;
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal totalBuy = BigDecimal.ZERO;
        BigDecimal totalSell = BigDecimal.ZERO;
        BigDecimal volume = BigDecimal.ZERO;
        BigDecimal volumeBuy = BigDecimal.ZERO;
        BigDecimal volumeSell = BigDecimal.ZERO;

        trade = result.sort(Sorts.ascending(TIMEDATE)).first();
        if (trade != null) {
            dto.setOpen(trade.getPrice());
        }

        trade = result.sort(Sorts.descending(TIMEDATE)).first();
        if (trade != null) {
            dto.setClose(trade.getPrice());
        }

        trade = result.sort(Sorts.ascending("price")).first();
        if (trade != null) {
            dto.setLow(trade.getPrice());
        }

        trade = result.sort(Sorts.descending("price")).first();
        if (trade != null) {
            dto.setHigh(trade.getPrice());
        }

        while (cursor.hasNext()) {
            trade = cursor.next();
            count++;
            total = total.add(trade.getTotal());
            volume = volume.add(trade.getVolume());

            if ("b".equals(trade.getBuySel())) {
                countBuy++;
                totalBuy = totalBuy.add(trade.getTotal());
                volumeBuy = volumeBuy.add(trade.getVolume());
            } else if ("s".equals(trade.getBuySel())) {
                countSell++;
                totalSell = totalSell.add(trade.getTotal());
                volumeSell = volumeBuy.add(trade.getVolume());
            }
        }
        dto.setCount(count);
        dto.setCountBuy(countBuy);
        dto.setCountSell(countSell);
        dto.setTotal(total);
        dto.setTotalBuy(totalBuy);
        dto.setTotalSell(totalSell);
        dto.setVolume(volume);
        dto.setVolumeBuy(volumeBuy);
        dto.setVolumeSell(volumeSell);
        dto.setCalcCandle(true);

        config.getCandleColl().replaceOne(
                eq("_id", dto.getId()), dto);
    }

}
