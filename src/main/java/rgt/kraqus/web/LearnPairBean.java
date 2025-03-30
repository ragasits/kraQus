package rgt.kraqus.web;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import rgt.kraqus.learn.LearnPairDTO;
import rgt.kraqus.learn.LearnPairService;

/**
 * JSF bean for Learn Pairs
 * @author rgt
 */
@SessionScoped
@Named(value = "learnPairBean")
public class LearnPairBean {
    
    private Integer minProfit = 2;
    private Integer scope = 1000;

    @Inject
    private LearnPairService learnPairService;
    
    public void onGenerate() {
        this.learnPairService.generate(scope, minProfit);
    }
    
    public void onBestLearns() {
        this.learnPairService.bestLearns();
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
}
