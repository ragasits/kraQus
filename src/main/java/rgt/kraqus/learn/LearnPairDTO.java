package rgt.kraqus.learn;

import java.math.BigDecimal;
import java.util.Date;
import org.bson.types.ObjectId;

/**
 * Learn Pairs Data
 * @author rgt
 */
public class LearnPairDTO {

    private ObjectId id;
    private Date buyDate;
    private BigDecimal buyClose;

    private Date sellDate;
    private BigDecimal sellClose;
    
    private BigDecimal profit;    
    private BigDecimal minProfit;

    public LearnPairDTO() {
    }
    
    
    public LearnPairDTO(Date buyDate, BigDecimal buyClose) {
        this.buyDate = buyDate;
        this.buyClose = buyClose;
        this.profit = BigDecimal.ZERO;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Date getBuyDate() {
        return buyDate;
    }

    public void setBuyDate(Date buyDate) {
        this.buyDate = buyDate;
    }

    public BigDecimal getBuyClose() {
        return buyClose;
    }

    public void setBuyClose(BigDecimal buyClose) {
        this.buyClose = buyClose;
    }

    public Date getSellDate() {
        return sellDate;
    }

    public void setSellDate(Date sellDate) {
        this.sellDate = sellDate;
    }

    public BigDecimal getSellClose() {
        return sellClose;
    }

    public void setSellClose(BigDecimal sellClose) {
        this.sellClose = sellClose;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public BigDecimal getMinProfit() {
        return minProfit;
    }

    public void setMinProfit(BigDecimal minProfit) {
        this.minProfit = minProfit;
    }
    
    
}
