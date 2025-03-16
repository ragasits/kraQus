package rgt.kraqus.web;

import jakarta.enterprise.context.SessionScoped;
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
    private long  selectedSellTime;
    private String selectedLearn;
    
    @Inject
    private LearnService learnService;
    
    @Inject
    private CandleService candleService;
    
    @Inject
    private CandleBean candleBean;


    public void updateLists(){
        this.selectedBuyTime = learnService.getFirst(this.selectedLearn).getStartDate().getTime();
        this.selectedSellTime = learnService.getLast(this.selectedLearn).getStartDate().getTime();
    }

    /**
     * Get all Learn
     *
     * @return 
     */
    public List<LearnDTO> getLearnList() {
        if (this.selectedLearn!=null){
            return learnService.get(this.selectedLearn);
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

    //Check1
    public void chkLearnPeaks() {
        learnService.chkLearnPeaks();
    }

    //Check2
    public void chkLearnPairs() {
        learnService.chkLearnPairs(this.selectedLearn);
    }

    public List<LearnDTO> getBuyList() {
        if (this.selectedLearn!=null){
            return learnService.getBuy(this.selectedLearn);
        }
        return Collections.emptyList();
    }

    public List<LearnDTO> getSellList() {
        if (this.selectedLearn!=null){
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
}
