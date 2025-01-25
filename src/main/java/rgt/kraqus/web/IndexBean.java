package rgt.kraqus.web;

import java.io.Serializable;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.util.List;

/**
 * JSF bean for Index page
 *
 * @author rgt
 */
@SessionScoped
@Named(value = "indexBean")
public class IndexBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private List<String> resultList;

    /*
    @EJB
    ConfigEJB config;
    @EJB
    TimerEJB timer;
    @EJB
    TradeEJB trade;
    @EJB
    CandleEJB candle;
    @EJB
    BollingerEJB bollinger;
    */

    /**
     * Show check message
     *
     * @param type
     * @param message
     */
    private void showResult(String type, String message) {
        FacesMessage msg;

        if (this.resultList.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, type, message + ": OK");
        } else {
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Candle", message + ": Errors(" + this.resultList.size() + ")");
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }







    public List<String> getResultList() {
        return resultList;
    }

    public String getResultText() {
        if (this.resultList == null || this.resultList.isEmpty()) {
            return "Result";
        }
        return "Result (" + this.resultList.size() + ")";
    }

}
