package rgt.kraqus.calc;

import java.math.BigDecimal;

/**
 * Store Moving Averages
 *
 * @author rgt
 */
public class MovingAverageDTO {

    private boolean calcMovingAverage = false;

    //Single Moving Average 20
    private BigDecimal sma20 = BigDecimal.ZERO;

    //Exponential Moving Average
    private BigDecimal ema9 = BigDecimal.ZERO;
    private BigDecimal ema12 = BigDecimal.ZERO;
    private BigDecimal ema26 = BigDecimal.ZERO;

    /**
     * Select EMA value
     *
     * @param limit
     * @return
     */
    public BigDecimal getEMA(int limit) {
        return switch (limit) {
            case 9 -> this.ema9;
            case 12 -> this.ema12;
            case 26 -> this.ema26;
            default -> BigDecimal.ZERO;
        };
    }

    public boolean isCalcMovingAverage() {
        return calcMovingAverage;
    }

    public void setCalcMovingAverage(boolean calcMovingAverage) {
        this.calcMovingAverage = calcMovingAverage;
    }

    public BigDecimal getSma20() {
        return sma20;
    }

    public void setSma20(BigDecimal sma20) {
        this.sma20 = sma20;
    }

    public BigDecimal getEma9() {
        return ema9;
    }

    public void setEma9(BigDecimal ema9) {
        this.ema9 = ema9;
    }

    public BigDecimal getEma12() {
        return ema12;
    }

    public void setEma12(BigDecimal ema12) {
        this.ema12 = ema12;
    }

    public BigDecimal getEma26() {
        return ema26;
    }

    public void setEma26(BigDecimal ema26) {
        this.ema26 = ema26;
    }
}
