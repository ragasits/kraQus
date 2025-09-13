package rgt.kraqus.calc;

import java.math.BigDecimal;

public class AtrDTO {
    
    private boolean calcAtr = false;
    
    private int index;
    private BigDecimal trueRange;
    private BigDecimal atrValue;

    public AtrDTO() {
    }
    
    public AtrDTO(int index, BigDecimal trueRange, BigDecimal atrValue) {
        this.index = index;
        this.trueRange = trueRange;
        this.atrValue = atrValue;
    }

    public boolean isCalcAtr() {
        return calcAtr;
    }

    public void setCalcAtr(boolean calcAtr) {
        this.calcAtr = calcAtr;
    }
    

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public BigDecimal getTrueRange() {
        return trueRange;
    }

    public void setTrueRange(BigDecimal trueRange) {
        this.trueRange = trueRange;
    }

    public BigDecimal getAtrValue() {
        return atrValue;
    }

    public void setAtrValue(BigDecimal atrValue) {
        this.atrValue = atrValue;
    }

    @Override
    public String toString() {
        return "AtrDTO{" +
                "index=" + index +
                ", trueRange=" + trueRange +
                ", atrValue=" + atrValue +
                '}';
    }
}
