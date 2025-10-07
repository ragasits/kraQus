package rgt.kraqus.learn;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import org.bson.types.ObjectId;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

/**
 * Learn Pairs Data
 *
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

    private boolean learn = false;

    public LearnPairDTO() {
    }

    public LearnPairDTO(Date buyDate, BigDecimal buyClose) {
        this.buyDate = buyDate;
        this.buyClose = buyClose;
        this.profit = BigDecimal.ZERO;
    }

    /**
     * Returns attributes for machine learning.
     *
     * @return List of Weka Attributes.
     */
    public static ArrayList<Attribute> getAttributes() {
        ArrayList<String> booleanValues = new ArrayList<>();
        booleanValues.add("true");
        booleanValues.add("false");

        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("buyDate", "yyyy-MM-dd HH:mm:ss"));
        attributes.add(new Attribute("buyClose"));

        attributes.add(new Attribute("sellDate", "yyyy-MM-dd HH:mm:ss"));
        attributes.add(new Attribute("sellClose"));

        attributes.add(new Attribute("profit"));
        attributes.add(new Attribute("minProfit"));

        attributes.add(new Attribute("learn", booleanValues));

        return attributes;
    }

    /**
     * Returns an instance populated with this object's attribute values.
     *
     * @param instances Instances structure to set values for.
     * @return DenseInstance with attribute values.
     */
    public DenseInstance getValues(Instances instances) {
        DenseInstance instance = new DenseInstance(instances.numAttributes());
        instance.setDataset(instances);

        instance.setValue(instances.attribute("buyDate"), this.getBuyDate().getTime());
        instance.setValue(instances.attribute("buyClose"), this.getBuyClose().doubleValue());

        instance.setValue(instances.attribute("sellDate"), this.getSellDate().getTime());
        instance.setValue(instances.attribute("sellClose"), this.getSellClose().doubleValue());

        instance.setValue(instances.attribute("profit"), this.getProfit().doubleValue());
        instance.setValue(instances.attribute("minProfit"), this.getMinProfit().doubleValue());

        instance.setValue(instances.attribute("learn"), Boolean.toString(this.isLearn()));

        return instance;
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

    public boolean isLearn() {
        return learn;
    }

    public void setLearn(boolean learn) {
        this.learn = learn;
    }
}
