package rgt.kraqus;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * My common methods
 *
 * @author rgt
 */
public class MyCommon {

    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    /**
     * Local BigDecimal.sqrt (Babylonian_method) Implemented only in the JAVA9
     *
     * @param A
     * @param SCALE
     * @return
     */
    public static BigDecimal sqrt(BigDecimal A, final int SCALE) {
        BigDecimal x0 = new BigDecimal("0");
        BigDecimal x1 = new BigDecimal(Math.sqrt(A.doubleValue()));
        while (!x0.equals(x1)) {
            x0 = x1;
            x1 = A.divide(x0, SCALE, RoundingMode.HALF_UP);
            x1 = x1.add(x0);
            x1 = x1.divide(TWO, SCALE, RoundingMode.HALF_UP);

        }
        return x1;
    }

}
