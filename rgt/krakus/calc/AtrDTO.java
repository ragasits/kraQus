package rgt.krakus.calc;

import java.math.BigDecimal;

public class AtrDTO {
    private int index;
    private BigDecimal atrValue;

    public AtrDTO(int index, BigDecimal atrValue) {
        this.index = index;
        this.atrValue = atrValue;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
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
                ", atrValue=" + atrValue +
                '}';
    }
}
