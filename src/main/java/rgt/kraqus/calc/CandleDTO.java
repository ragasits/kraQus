package rgt.kraqus.calc;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

/**
 * DTO for the Candle data
 *
 * @author rgt
 */
public class CandleDTO {

    private ObjectId id;
    private Date startDate;
    private Integer count;
    private Integer countBuy;
    private Integer countSell;
    private BigDecimal open;
    private BigDecimal low;
    private BigDecimal high;
    private BigDecimal close;
    private BigDecimal total;
    private BigDecimal totalBuy;
    private BigDecimal totalSell;
    private BigDecimal volume;
    private BigDecimal volumeBuy;
    private BigDecimal volumeSell;
    private boolean calcCandle;

    private MovingAverageDTO movingAverage;
    private BollingerDTO bollinger;
    private RsiDTO rsi;
    private MacdDTO macd;
    private CciDTO cci;

    public CandleDTO() {
    }

    public CandleDTO(Date candleDate) {
        this.id = null;
        this.startDate = (Date) candleDate.clone();
        this.count = 0;
        this.countBuy = 0;
        this.countSell = 0;
        this.open = BigDecimal.ZERO;
        this.low = BigDecimal.ZERO;
        this.high = BigDecimal.ZERO;
        this.close = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.totalBuy = BigDecimal.ZERO;
        this.totalSell = BigDecimal.ZERO;
        this.volume = BigDecimal.ZERO;
        this.volumeBuy = BigDecimal.ZERO;
        this.volumeSell = BigDecimal.ZERO;
        this.calcCandle = false;

        this.bollinger = new BollingerDTO();
        this.rsi = new RsiDTO();
        this.movingAverage = new MovingAverageDTO();
        this.macd = new MacdDTO();
        this.cci = new CciDTO();
    }

    /**
     * Calculate stop Date candleDate + 30 minute
     *
     * @return
     */
    @BsonIgnore
    public Date getStopDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.startDate);
        cal.add(Calendar.MINUTE, 30);
        return cal.getTime();
    }

    @BsonIgnore
    public String getIdHexa() {
        if (this.id != null) {
            return this.id.toHexString();
        }
        return null;
    }

    public Date getStartDate() {
        if (this.startDate != null) {
            return (Date) startDate.clone();
        }
        return null;
    }

    /**
     * Format startDate
     *
     * @return
     */
    @BsonIgnore
    public String getFormatedStartDate() {
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sd.format(this.startDate);
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getCountBuy() {
        return countBuy;
    }

    public void setCountBuy(Integer countBuy) {
        this.countBuy = countBuy;
    }

    public Integer getCountSell() {
        return countSell;
    }

    public void setCountSell(Integer countSell) {
        this.countSell = countSell;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public void setOpen(BigDecimal open) {
        this.open = open;
    }

    public BigDecimal getLow() {
        return low;
    }

    public void setLow(BigDecimal low) {
        this.low = low;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public void setHigh(BigDecimal high) {
        this.high = high;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getTotalBuy() {
        return totalBuy;
    }

    public void setTotalBuy(BigDecimal totalBuy) {
        this.totalBuy = totalBuy;
    }

    public BigDecimal getTotalSell() {
        return totalSell;
    }

    public void setTotalSell(BigDecimal totalSell) {
        this.totalSell = totalSell;
    }

    public BigDecimal getClose() {
        return close;
    }

    public void setClose(BigDecimal close) {
        this.close = close;
    }

    public ObjectId getId() {
        return id;
    }

    public boolean isCalcCandle() {
        return calcCandle;
    }

    public void setCalcCandle(boolean calcCandle) {
        this.calcCandle = calcCandle;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public BigDecimal getVolumeBuy() {
        return volumeBuy;
    }

    public void setVolumeBuy(BigDecimal volumeBuy) {
        this.volumeBuy = volumeBuy;
    }

    public BigDecimal getVolumeSell() {
        return volumeSell;
    }

    public void setVolumeSell(BigDecimal volumeSell) {
        this.volumeSell = volumeSell;
    }

    @Override
    public String toString() {
        return "CandleDTO{" + "id=" + id + ", startDate=" + startDate + ", count=" + count + ", countBuy=" + countBuy + ", countSell=" + countSell + ", open=" + open + ", low=" + low + ", high=" + high + ", close=" + close + ", total=" + total + ", totalBuy=" + totalBuy + ", totalSell=" + totalSell + ", volume=" + volume + ", volumeBuy=" + volumeBuy + ", volumeSell=" + volumeSell + ", calcCandle=" + calcCandle + '}';
    }

    public BollingerDTO getBollinger() {
        return bollinger;
    }

    public void setBollinger(BollingerDTO bollinger) {
        this.bollinger = bollinger;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setStartDate(Date startDate) {
        this.startDate = (Date) startDate.clone();
    }

    public RsiDTO getRsi() {
        return rsi;
    }

    public void setRsi(RsiDTO rsi) {
        this.rsi = rsi;
    }

    public MovingAverageDTO getMovingAverage() {
        return movingAverage;
    }

    public void setMovingAverage(MovingAverageDTO movingAverage) {
        this.movingAverage = movingAverage;
    }

    public MacdDTO getMacd() {
        return macd;
    }

    public void setMacd(MacdDTO macd) {
        this.macd = macd;
    }

    public CciDTO getCci() {
        return cci;
    }

    public void setCci(CciDTO cci) {
        this.cci = cci;
    }
}
