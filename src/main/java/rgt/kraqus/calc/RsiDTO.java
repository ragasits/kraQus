package rgt.kraqus.calc;

import java.math.BigDecimal;

/**
 *
 * @author rgt
 */
public class RsiDTO {

    private boolean calcRsi = false;
    private BigDecimal change = BigDecimal.ZERO;
    private BigDecimal gain = BigDecimal.ZERO;
    private BigDecimal loss = BigDecimal.ZERO;
    private BigDecimal avgGain = BigDecimal.ZERO;
    private BigDecimal avgLoss = BigDecimal.ZERO;
    private BigDecimal rs = BigDecimal.ZERO;
    private BigDecimal rsi = BigDecimal.ZERO;
    private boolean rsiBuy = false;
    private boolean rsiSell = false;


    public boolean isCalcRsi() {
        return calcRsi;
    }

    public void setCalcRsi(boolean calcRsi) {
        this.calcRsi = calcRsi;
    }

    public BigDecimal getChange() {
        return change;
    }

    public void setChange(BigDecimal change) {
        this.change = change;
    }

    public BigDecimal getGain() {
        return gain;
    }

    public void setGain(BigDecimal gain) {
        this.gain = gain;
    }

    public BigDecimal getLoss() {
        return loss;
    }

    public void setLoss(BigDecimal loss) {
        this.loss = loss;
    }

    public BigDecimal getAvgGain() {
        return avgGain;
    }

    public void setAvgGain(BigDecimal avgGain) {
        this.avgGain = avgGain;
    }

    public BigDecimal getAvgLoss() {
        return avgLoss;
    }

    public void setAvgLoss(BigDecimal avgLoss) {
        this.avgLoss = avgLoss;
    }

    public BigDecimal getRs() {
        return rs;
    }

    public void setRs(BigDecimal rs) {
        this.rs = rs;
    }

    public BigDecimal getRsi() {
        return rsi;
    }

    public void setRsi(BigDecimal rsi) {
        this.rsi = rsi;
    }

    public boolean isRsiBuy() {
        return rsiBuy;
    }

    public void setRsiBuy(boolean rsiBuy) {
        this.rsiBuy = rsiBuy;
    }

    public boolean isRsiSell() {
        return rsiSell;
    }

    public void setRsiSell(boolean rsiSell) {
        this.rsiSell = rsiSell;
    }
    
    
}
