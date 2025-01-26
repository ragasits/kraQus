package rgt.kraqus.web;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.bson.types.ObjectId;
import rgt.kraqus.calc.CandleDTO;
import rgt.kraqus.calc.CandleService;
import rgt.kraqus.learn.LearnDTO;
import rgt.kraqus.learn.LearnService;

/**
 * JSF bean for one Candle
 *
 * @author rgt
 */
@SessionScoped
@Named(value = "candleBean")
public class CandleBean implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Inject
    private LearnService learnService;
    
    @Inject
    private CandleService candleService;

    private String selectedIdHexa = null;
    private Date selectedDate;
    private LearnDTO learnDetail = new LearnDTO();
    private boolean insertLearn = false;
    private String selectedLearn;

    /**
     * get one Candle
     *
     * @return
     */
    public CandleDTO getDetail() {
        if (this.selectedIdHexa == null || this.selectedIdHexa.isEmpty()) {
            return null;
        }
        return candleService.get(new ObjectId(selectedIdHexa));
    }

    
    public Date getSelectedDate() {
        if (selectedDate != null) {
            return (Date) selectedDate.clone();
        }
        return null;
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = (Date) selectedDate.clone();
    }

    /**
     * Get date related candles
     *
     * @return
     */
    public List<CandleDTO> getCandleList() {
        if (selectedDate != null) {
            return candleService.getOneDay(selectedDate);
        }
        return null;
    }

    /**
     * Name list for p:autoComplete
     *
     * @param query
     * @return
     */
    public List<String> complete(String query) {
        return learnService.getNames();
    }

    /**
     * Is the candle exists?
     *
     * @return
     */
    public boolean isSelectedCandle() {
        return (this.selectedIdHexa == null || this.selectedIdHexa.isEmpty());
    }

    /**
     * Get Candle related learns
     *
     * @return
     */
    public List<LearnDTO> getLearnList() {
        if (this.getDetail() != null) {
            return learnService.get(this.getDetail().getStartDate());
        }
        return null;
    }

    public void showLearnDetail(LearnDTO dto) {
        this.insertLearn = false;
        this.learnDetail = dto;
    }

    /**
     * Create a new Learn
     *
     * @return
     */
    public String onNewLearn() {
        this.insertLearn = true;
        this.learnDetail = new LearnDTO();
        this.learnDetail.setStartDate(this.getDetail().getStartDate());
        return null;
    }

    /**
     * Add error message
     *
     * @param msg
     */
    private void addErrMsg(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    /**
     * Add oneClick sell,buy learn line
     *
     * @param trade
     */
    public void onNewLearn(String trade) {
        if (this.selectedLearn == null || this.selectedLearn.isEmpty()) {
            this.addErrMsg("Learn.Name: must not be null");
            return;
        }

        this.learnDetail = new LearnDTO();
        learnDetail.setName(this.selectedLearn);
        learnDetail.setStartDate(this.getDetail().getStartDate());
        learnDetail.setTrade(trade);
        learnDetail.setClose(this.getDetail().getClose());
        this.learnService.add(learnDetail);
    }

    /**
     * Save a Learn data
     */
    public void onSaveLearn() {
        if (this.insertLearn) {
            learnService.add(learnDetail);
        } else {
            learnDetail.setClose(this.getDetail().getClose());
            learnService.update(learnDetail);
        }
    }

    /**
     * Delete a learn data
     */
    public void onDeleteLearn() {
        learnService.delete(learnDetail);
    }

    /**
     * Get the minimum date from the Candle
     *
     * @return
     */
    public Date getMinDate() {
        return candleService.getFirstDate();
    }

    /**
     * Get the maximum date from the Candle
     *
     * @return
     */
    public Date getMaxDate() {
        return candleService.getLatesDate();
    }

    public String getSelectedIdHexa() {
        return selectedIdHexa;
    }

    public void setSelectedIdHexa(String selectedIdHexa) {
        this.selectedIdHexa = selectedIdHexa;
    }

    public LearnDTO getLearnDetail() {
        return learnDetail;
    }

    public void setLearnDetail(LearnDTO learnDetail) {
        this.learnDetail = learnDetail;
    }

    public boolean isInsertLearn() {
        return insertLearn;
    }

    public String getSelectedLearn() {
        return selectedLearn;
    }

    public void setSelectedLearn(String selectedLearn) {
        this.selectedLearn = selectedLearn;
    }

}
