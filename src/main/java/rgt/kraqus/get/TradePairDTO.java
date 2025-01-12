package rgt.kraqus.get;

import java.math.BigDecimal;
import java.util.Date;
import org.bson.types.ObjectId;

/**
 * DTO for Kraken trade pairs
 *
 * @author rgt
 */
public class TradePairDTO {

    private ObjectId id;
    private BigDecimal price;
    private BigDecimal volume;
    private BigDecimal time;
    private String buySel;
    private String marketLimit;
    private String miscellaneous;

    private String error;
    private String last;
    private String pair;

    public TradePairDTO() {
    }

    public TradePairDTO(BigDecimal price, BigDecimal volume, BigDecimal time, String buySel, String marketLimit, String miscellaneous, String error, String last, String pair) {
        this.price = price;
        this.volume = volume;
        this.time = time;
        this.buySel = buySel;
        this.marketLimit = marketLimit;
        this.miscellaneous = miscellaneous;
        this.error = error;
        this.last = last;
        this.pair = pair;
    }

    /**
     * Convert Time value to Date
     *
     * @return
     */
    public Date getTimeDate() {
        Date date = new Date();
        long milis = time.multiply(BigDecimal.valueOf(1000)).longValue();
        date.setTime(milis);
        return date;
    }

    /**
     * Convert Last value to Date
     *
     * @return
     */
    public Date getLastDate() {
        Date date = new Date();
        date.setTime(Long.parseLong(this.last.substring(0, 13)));
        return date;
    }

    /**
     * calculate total value
     *
     * @return
     */
    public BigDecimal getTotal() {
        return this.price.multiply(this.volume);
    }

    public BigDecimal getTime() {
        return time;
    }

    public String getLast() {
        return last;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getBuySel() {
        return buySel;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    @Override
    public String toString() {
        return "TradePairDTO{" + "price=" + price + ", volume=" + volume + ", time=" + time + ", buySel=" + buySel + ", marketLimit=" + marketLimit + ", miscellaneous=" + miscellaneous + ", error=" + error + ", last=" + last + ", pair=" + pair + '}';
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public void setTime(BigDecimal time) {
        this.time = time;
    }

    public void setBuySel(String buySel) {
        this.buySel = buySel;
    }

    public void setMarketLimit(String marketLimit) {
        this.marketLimit = marketLimit;
    }

    public void setMiscellaneous(String miscellaneous) {
        this.miscellaneous = miscellaneous;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }


    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
     
    public String getMarketLimit() {
        return marketLimit;
    }

    public String getMiscellaneous() {
        return miscellaneous;
    }

    public String getError() {
        return error;
    }

    public String getPair() {
        return pair;
    }

}