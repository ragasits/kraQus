package rgt.kraqus.web;

import java.io.Serializable;
import java.util.List;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import rgt.kraqus.get.TradePairDTO;
import rgt.kraqus.get.TradeService;


/**
 * JSF bean for Trades
 * @author rgt
 */
@SessionScoped
@Named(value = "tradeBean")
public class TradeBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Inject 
    TradeService tradeService;


    private List<TradePairDTO> tradeList;
    private int queryLimit = 100;



    /**
     * Get Trade data
     */
    public void onTradeQuery() {
        this.tradeList = tradeService.getLasts(this.queryLimit);
    }

    public List<TradePairDTO> getTradeList() {
        return tradeList;
    }

    public int getQueryLimit() {
        return queryLimit;
    }

    public void setQueryLimit(int queryLimit) {
        this.queryLimit = queryLimit;
    }

}
