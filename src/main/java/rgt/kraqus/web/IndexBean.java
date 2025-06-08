package rgt.kraqus.web;

import io.quarkus.logging.Log;
import java.io.Serializable;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Date;
import org.eclipse.microprofile.context.ManagedExecutor;
import rgt.kraqus.MyConfig;
import rgt.kraqus.calc.CandleDTO;
import rgt.kraqus.calc.CandleService;
import rgt.kraqus.get.TradePairDTO;
import rgt.kraqus.get.TradeService;
import rgt.kraqus.prod.ProdService;

/**
 * JSF bean for Index page
 *
 * @author rgt
 */
@SessionScoped
@Named(value = "indexBean")
public class IndexBean implements Serializable {

    @Inject
    private ProdService prodService;

    @Inject
    ManagedExecutor executor;

    /**
     * Asynchronously starts the regeneration of all candles and adds UI
     * messages about progress.
     */
    public void onRegenerateCandle() {
        addInfoMsg("Candle regeneration started.");
        executor.submit(() -> {
            try {
                Log.info("Start: regenerateAllCandles");
                prodService.regenerateAllCandles();
                Log.info("Finish: regenerateAllCandles");
                addInfoMsg("Candle regeneration done.");
            } catch (Exception e) {
                addErrorMsg(e.getMessage());
            }
        });
    }

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
     *
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
     *
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
     *
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
     *
     * @return
     */
    public Date getLastCandleDate() {
        CandleDTO dto = candleService.getLast();
        if (dto != null) {
            return dto.getStartDate();
        }
        return null;
    }

    /**
     * Add error message to the GUI
     *
     * @param msg
     */
    private void addErrorMsg(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    /**
     * Add info message to the GUI
     *
     * @param msg
     */
    private void addInfoMsg(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

}
