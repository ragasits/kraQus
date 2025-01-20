package rgt.kraqus.calc;

import java.math.BigDecimal;

/**
 * Commodity Channel Index (CCI)
 * @author rgt
 */
public class CciDTO {
    private boolean calcCci = false;
    private BigDecimal typicalPrice = BigDecimal.ZERO;
    private BigDecimal sma20Typical = BigDecimal.ZERO;
    private BigDecimal mad20 = BigDecimal.ZERO;
    private BigDecimal cci20 = BigDecimal.ZERO;
    private boolean overBought = false;
    private boolean overSold= false;

    public boolean isCalcCci() {
        return calcCci;
    }

    public void setCalcCci(boolean calcCci) {
        this.calcCci = calcCci;
    }

    public BigDecimal getTypicalPrice() {
        return typicalPrice;
    }

    public void setTypicalPrice(BigDecimal typicalPrice) {
        this.typicalPrice = typicalPrice;
    }

    public BigDecimal getSma20Typical() {
        return sma20Typical;
    }

    public void setSma20Typical(BigDecimal sma20Typical) {
        this.sma20Typical = sma20Typical;
    }

    public BigDecimal getMad20() {
        return mad20;
    }

    public void setMad20(BigDecimal mad20) {
        this.mad20 = mad20;
    }

    public BigDecimal getCci20() {
        return cci20;
    }

    public void setCci20(BigDecimal cci20) {
        this.cci20 = cci20;
    }

    public boolean isOverBought() {
        return overBought;
    }

    public void setOverBought(boolean overbought) {
        this.overBought = overbought;
    }

    public boolean isOverSold() {
        return overSold;
    }

    public void setOverSold(boolean overSold) {
        this.overSold = overSold;
    }
        
}
