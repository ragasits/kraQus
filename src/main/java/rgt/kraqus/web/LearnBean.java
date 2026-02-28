package rgt.kraqus.web;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
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
@Named(value = "learnBean")
public class LearnBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private long selectedBuyTime;
    private long selectedSellTime;
    private String selectedLearn;
    private boolean showOnlyErrors = false;

    @Inject
    private LearnService learnService;

    @Inject
    private CandleService candleService;

    @Inject
    private CandleBean candleBean;

    public void updateLists() {
        this.selectedBuyTime = learnService.getFirst(this.selectedLearn).getStartDate().getTime();
        this.selectedSellTime = learnService.getLast(this.selectedLearn).getStartDate().getTime();
    }

    /**
     * Get all Learn
     *
     * @return
     */
    public List<LearnDTO> getLearnList() {
        if (this.selectedLearn != null) {
            return learnService.get(this.selectedLearn, this.showOnlyErrors);
        }
        return Collections.emptyList();
    }

    /**
     * Get Names (Distinct)
     *
     * @return
     */
    public List<String> getLearnNameList() {
        return learnService.getNames();
    }

    public String getSelectedLearn() {
        return selectedLearn;
    }

    public void setSelectedLearn(String selectedLearn) {
        this.selectedLearn = selectedLearn;
    }

    /**
     * Link to candleDetail
     *
     * @param learn
     * @return
     */
    public String showDetail(LearnDTO learn) {

        if (learn != null) {
            candleBean.setSelectedDate(learn.getStartDate());

            CandleDTO dto = candleService.get(learn.getStartDate());
            candleBean.setSelectedIdHexa(dto.getIdHexa());

            return "candle?faces-redirect=true";
        }
        return null;
    }

    /**
     * Delete Learn
     */
    public void onDelete() {
        if (selectedLearn.isEmpty()){
            this.addErrorMsg("Not selected Learn");
        } else if (selectedLearn.equals("Első")) {
            this.addErrorMsg("Not a llowe to delete: Első");
        } else {
            learnService.delete(selectedLearn);
            this.addInfoMsg("Delete OK: " + selectedLearn);
        }
    }

    //Check1
    public void chkLearnPeaks() {
        learnService.chkLearnPeaks();
    }

    //Check2
    public void chkLearnPairs() {
        learnService.chkLearnPairs(this.selectedLearn);
    }

    public List<LearnDTO> getBuyList() {
        if (this.selectedLearn != null) {
            return learnService.getBuy(this.selectedLearn);
        }
        return Collections.emptyList();
    }

    public List<LearnDTO> getSellList() {
        if (this.selectedLearn != null) {
            return learnService.getSell(this.selectedLearn);
        }
        return Collections.emptyList();
    }

    public long getSelectedBuyTime() {
        return selectedBuyTime;
    }

    public void setSelectedBuyTime(long selectedBuyTime) {
        this.selectedBuyTime = selectedBuyTime;
    }

    public long getSelectedSellTime() {
        return selectedSellTime;
    }

    public void setSelectedSellTime(long selectedSellTime) {
        this.selectedSellTime = selectedSellTime;
    }

    public boolean isShowOnlyErrors() {
        return showOnlyErrors;
    }

    public void setShowOnlyErrors(boolean showOnlyErrors) {
        this.showOnlyErrors = showOnlyErrors;
    }
    

    /**
     * Add message: Info
     *
     * @param msg
     */
    private void addInfoMsg(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    /**
     * Add message: Error
     *
     * @param msg
     */
    private void addErrorMsg(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

}
