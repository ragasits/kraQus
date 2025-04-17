package rgt.kraqus.web;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import rgt.kraqus.learn.LearnPairDTO;
import rgt.kraqus.learn.LearnPairService;
import rgt.kraqus.learn.LearnService;

/**
 * JSF bean for Learn Pairs
 *
 * @author rgt
 */
@SessionScoped
@Named(value = "learnPairBean")
public class LearnPairBean {

    private static final long serialVersionUID = 1L;

    private Integer minProfit = 2;
    private Integer scope = 1000;

    @Inject
    private LearnPairService learnPairService;

    @Inject
    private LearnService learnService;

    private String selectedLearnName;

    public List<String> complete(String query) {
        return learnService.getNames();
    }

    /**
     * Generate Learn Pairs
     */
    public void onGenerate() {
        this.learnPairService.generate(scope, minProfit);
    }

    /**
     * Looking for the best learnPairs
     */
    public void onBestLearns() {
        this.learnPairService.bestLearns();
    }

    /**
     * Save best LearnPairs into Learns
     */
    public void onSaveLearn() {
        if (this.selectedLearnName != null) {
            this.addInfoMsg(this.learnPairService.saveLearn(selectedLearnName));
        } else {
            this.addMsg("Mising selected learnName");
        }

    }

    /**
     * Add error message to the GUI
     * @param msg 
     */
    private void addMsg(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    /**
     * Add info message to the GUI
     * @param msg 
     */
    private void addInfoMsg(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    public Integer getMinProfit() {
        return minProfit;
    }

    public void setMinProfit(Integer minProfit) {
        this.minProfit = minProfit;
    }

    public Integer getScope() {
        return scope;
    }

    public void setScope(Integer scope) {
        this.scope = scope;
    }

    public List<LearnPairDTO> getLearnList() {
        return this.learnPairService.get(1000);
    }

    public String getSelectedLearnName() {
        return selectedLearnName;
    }

    public void setSelectedLearnName(String selectedLearnName) {
        this.selectedLearnName = selectedLearnName;
    }

}
