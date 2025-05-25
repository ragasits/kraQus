package rgt.kraqus.web;

import java.io.Serializable;
import java.util.List;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Calendar;
import java.util.Date;
import rgt.kraqus.learn.LearnDTO;
import rgt.kraqus.learn.LearnService;
import rgt.kraqus.profit.ProfitDTO;
import rgt.kraqus.profit.ProfitItemDTO;
import rgt.kraqus.profit.ProfitService;

/**
 * JSF bean for Profit page
 *
 * @author rgt
 */
@SessionScoped
@Named(value = "profitBean")
public class ProfitBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private ProfitDTO detail = new ProfitDTO();

    @Inject
    private ProfitService profitService;

    @Inject
    private LearnService learnService;

    /**
     * Update Buy, Sell date
     */
    public void updateLearn() {
        detail.setBuyDate(learnService.getFirst(detail.getLearnName()).getStartDate());
        detail.setSellDate(learnService.getLast(detail.getLearnName()).getStartDate());
    }

    /**
     * Get tests by LearnName
     *
     * @return
     */
    public List<ProfitDTO> getTestNumList() {
        if (this.detail.getLearnName() != null) {
            return profitService.get(detail.getLearnName());
        }
        return null;
    }
    
    /**
     * Set the selected Buy,Sell dates
     */
    public void onThisYear(){
        
        //Get first element of current year
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        String learnName = this.detail.getLearnName();
        
        
        this.detail.setBuyDate(learnService.getThisYearBuy(learnName, year));
        this.detail.setSellDate(learnService.getThisYearSell(learnName, year));
    }

    /**
     * Calculate profit
     */
    public void onCalc() {
        this.detail = profitService.calcProfit(detail.clone());
    }

    /**
     * Calculate profits with different strategies and Tresholds
     */
    public void onCalcAll() {
        this.onDeleteAll();

        //FirtSell
        detail.setStrategy("FirtSell");
        this.onCalc();

        //FirstProfit
        detail.setStrategy("FirstProfit");
        this.onCalc();

        //RSI
        detail.setStrategy("RSI");
        this.onCalc();

        //FirstTreshold
        detail.setStrategy("FirstTreshold");

        for (int i = 0; i < 10; i++) {
            detail.setTreshold(i + 1);
            this.onCalc();
        }
    }

    /**
     * Delete One profit
     */
    public void onDelete() {
        if (this.detail.getTestNum() != null) {
            ProfitDTO dto = profitService.get(this.detail.getTestNum());
            profitService.delete(dto);
        }
    }

    /**
     * Delete the all profit by LearName
     */
    public void onDeleteAll() {
        if (this.detail.getLearnName() != null) {
            profitService.delete(this.detail.getLearnName());
        }
    }

    public ProfitDTO getDetail() {
        return detail;
    }

    /**
     * get profit list, filter by best
     *
     * @return
     */
    public List<ProfitItemDTO> getProfitList() {
        if (this.detail.getTestNum() != null) {
            ProfitDTO dto = profitService.get(this.detail.getTestNum());
            if (dto != null) {
                return dto.getItems();
            }
        }
        return null;
    }

    public boolean isTresholdDisabled() {
        if (detail.getStrategy() != null) {
            return !"FirstTreshold".equals(detail.getStrategy());
        }
        return false;
    }

    public List<LearnDTO> getBuyList() {
        if (this.detail != null && this.detail.getLearnName() != null) {
            return learnService.getBuy(this.detail.getLearnName());
        }
        return null;
    }

    public List<LearnDTO> getSellList() {
        if (this.detail != null && this.detail.getLearnName() != null) {
            return learnService.getSell(this.detail.getLearnName());
        }
        return null;
    }

    public Long getSelectedBuyTime() {
        if (detail.getBuyDate() != null) {
            return detail.getBuyDate().getTime();
        }
        return null;
    }

    public void setSelectedBuyTime(Long selectedBuyTime) {
        if (selectedBuyTime != null) {
            detail.setBuyDate(new Date(selectedBuyTime));
        } else {
            detail.setBuyDate(null);
        }
    }

    public Long getSelectedSellTime() {
        if (detail.getSellDate() != null) {
            return detail.getSellDate().getTime();
        }
        return null;
    }

    public void setSelectedSellTime(Long selectedSellTime) {
        if (selectedSellTime != null) {
            detail.setSellDate(new Date(selectedSellTime));
        } else {
            detail.setSellDate(null);
        }
    }

    public List<String> getStrategyList() {
        return profitService.getStrategyList();
    }

    public Long getSelectedTestNum() {
        if (detail != null) {
            return detail.getTestNum();
        }
        return null;
    }

    public void setSelectedTestNum(Long selectedTestNum) {
        if (selectedTestNum != null) {
            this.detail = profitService.get(selectedTestNum);
        } else {
            String learName = detail.getLearnName();
            this.detail = new ProfitDTO();
            detail.setLearnName(learName);
        }
    }

    public String getSelectedLearnName() {
        if (detail != null) {
            return this.detail.getLearnName();
        }
        return null;
    }

    public void setSelectedLearnName(String selectedLearnName) {
        if (selectedLearnName != null) {
            detail.setLearnName(selectedLearnName);
        } else {
            detail.setLearnName(null);
        }
    }

    public String getSelectedStrategy() {
        return detail.getStrategy();
    }

    public void setSelectedStrategy(String selectedStrategy) {
        if (selectedStrategy != null) {
            detail.setStrategy(selectedStrategy);
        } else {
            detail.setStrategy(null);
        }
    }

    public Integer getTreshold() {
        return detail.getTreshold();
    }

    public void setTreshold(Integer treshold) {
        if (treshold != null) {
            this.detail.setTreshold(treshold);
        } else {
            this.detail.setTreshold(null);
        }
    }
}
