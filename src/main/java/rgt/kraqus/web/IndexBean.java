package rgt.kraqus.web;

import java.io.Serializable;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Date;
import rgt.kraqus.MyConfig;
import rgt.kraqus.calc.CandleDTO;
import rgt.kraqus.calc.CandleService;
import rgt.kraqus.get.TradePairDTO;
import rgt.kraqus.get.TradeService;

/**
 * JSF bean for Index page
 *
 * @author rgt
 */
@SessionScoped
@Named(value = "indexBean")
public class IndexBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private MyConfig config;

    @Inject
    private TradeService tradeService;

    @Inject
    private CandleService candleService;

    public boolean isRunProduction() {
        return config.isRunProduction();
    }

    /**
     * getFirst Trade
     * @return 
     */
    public Date getFirstTradeDate() {
        TradePairDTO trade = tradeService.getFirst();
        if (trade != null) {
            return trade.getTimeDate();
        }
        return null;
    }

    /**
     * Get last trade
     * @return 
     */
    public Date getLastTradeDate() {
        TradePairDTO trade = tradeService.getLast();
        if (trade != null) {
            return trade.getTimeDate();
        }
        return null;
    }

    /**
     * Get first Candle
     * @return 
     */
    public Date getFirstCandleDate() {
        CandleDTO dto = candleService.getFirst();
        if (dto != null) {
            return dto.getStartDate();
        }
        return null;
    }

    /**
     * Get Last Candle
     * @return 
     */
    public Date getLasttCandleDate() {
        CandleDTO dto = candleService.getLast();
        if (dto != null) {
            return dto.getStartDate();
        }
        return null;
    }

}
