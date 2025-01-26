package rgt.kraqus.learn;

import java.math.BigDecimal;
import java.util.Date;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

/**
 * Store Learning data
 *
 * @author rgt
 */
public class LearnDTO {

    private ObjectId id;
    private String name;
    private Date startDate;
    private String trade;
    private BigDecimal close;
    private String chkMessage;

    public LearnDTO() {
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTrade() {
        return trade;
    }

    public void setTrade(String trade) {
        this.trade = trade;
    }

    public Date getStartDate() {
        return (Date) startDate.clone();
    }

    public void setStartDate(Date startDate) {
        this.startDate = (Date) startDate.clone();
    }

    public String getChkMessage() {
        return chkMessage;
    }

    public void setChkMessage(String chkMessage) {
        this.chkMessage = chkMessage;
    }

    public BigDecimal getClose() {
        return close;
    }

    public void setClose(BigDecimal close) {
        this.close = close;
    }

    @BsonIgnore
    public boolean isBuy() {
        return this.getTrade().equals("buy");
    }

    @BsonIgnore
    public boolean isSell() {
        return this.getTrade().equals("sell");
    }
}
