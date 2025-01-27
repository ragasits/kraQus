package rgt.kraqus.profit;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.List;
import org.bson.types.ObjectId;

/**
 *
 * @author rgt
 */
public class ProfitDTO {

    // Random values
    static final String BUY = "buy";
    static final String SELL = "sell";
    static final String NONE = "none";

    private ObjectId id;
    private Long testNum;
    private String learnName;
    private double eur;
    private List<ProfitItemDTO> items;

    private Date buyDate;
    private Date sellDate;

    private String strategy;
    private Integer treshold;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Long getTestNum() {
        return testNum;
    }

    public void setTestNum(Long testNum) {
        this.testNum = testNum;
    }

    public double getEur() {
        return eur;
    }

    public void setEur(double eur) {
        this.eur = eur;
    }

    /**
     * Format large EUR value to readable
     *
     * @return
     */
    public String getEurFormat() {
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        decimalFormatSymbols.setGroupingSeparator(',');
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00", decimalFormatSymbols);
        return decimalFormat.format(this.eur);
    }

    public List<ProfitItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ProfitItemDTO> items) {
        this.items = items;
    }

    public String getLearnName() {
        return learnName;
    }

    public void setLearnName(String learnName) {
        this.learnName = learnName;
    }

    public Date getBuyDate() {
        return buyDate;
    }

    public void setBuyDate(Date buyDate) {
        this.buyDate = buyDate;
    }

    public Date getSellDate() {
        return sellDate;
    }

    public void setSellDate(Date sellDate) {
        this.sellDate = sellDate;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public Integer getTreshold() {
        return treshold;
    }

    public void setTreshold(Integer treshold) {
        this.treshold = treshold;
    }
    
    /**
     * Clone Profit (except id)
     * @return 
     */
    public ProfitDTO clone(){
        ProfitDTO dto = new ProfitDTO();
        dto.setLearnName(learnName);
        dto.setTestNum(testNum);
        dto.setBuyDate(buyDate);
        dto.setSellDate(sellDate);
        dto.setEur(eur);
        dto.setStrategy(strategy);
        dto.setTreshold(treshold);
        dto.setItems(items);
        return dto;
    }

}
