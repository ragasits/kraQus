package rgt.kraqus.web;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import rgt.kraqus.learn.LearnPairDTO;
import rgt.kraqus.learn.LearnPairService;
import rgt.kraqus.learn.LearnService;
import weka.core.Instances;
import weka.core.converters.AbstractFileSaver;
import weka.core.converters.CSVSaver;

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
    private String profitThisYear;
    private String profitLastYear;

    @Inject
    private ProfitBean profitBean;

    @Inject
    private LearnPairService learnPairService;

    @Inject
    private LearnService learnService;

    private String selectedLearnName;
    private StreamedContent file;

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
     * Runs the full LearnPairs workflow: generate, select best, and save.
     */
    public void onRunAll() {
        this.onGenerate();
        this.onBestLearns();
        this.onSaveLearn();

        this.addInfoMsg("RuanAll: OK");
    }

    public void onRunAllProfit() {

        //Check
        if (this.selectedLearnName == null || this.selectedLearnName.isEmpty()) {
            this.addErrorMsg("Missing value: learnName");
            return;
        }
        if (this.minProfit == null) {
            this.addErrorMsg("Missing value: minProfit");
            return;
        }
        if (this.scope == null) {
            this.addErrorMsg("Missing value: minProfit");
            return;
        }

        //Run All
        this.onRunAll();

        //Calculate profit - Prepare
        this.profitBean.setSelectedLearnName(selectedLearnName);
        this.profitBean.setSelectedStrategy("FirtSell");
        this.profitBean.onDeleteAll();

        // This Year
        this.profitBean.onThisYear();
        this.profitBean.onCalc();
        this.profitThisYear = this.profitBean.getDetail().getEurFormat();

        //Last Year
        this.profitBean.onLastYear();
        this.profitBean.onCalc();
        this.profitLastYear = this.profitBean.getDetail().getEurFormat();

        this.addInfoMsg("RuanAll + Profit calc: OK");
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
     * Exports LearnPairs data as a CSV file for download.
     */
    public void onCSVExport() {
        try {
            Instances instances = learnPairService.toInstances();
            File tempFile = File.createTempFile("learnpair", ".tmp");

            AbstractFileSaver saver = new CSVSaver();;
            saver.setInstances(instances);
            saver.setFile(tempFile);
            saver.writeBatch();

            final File fileToServe = tempFile;  // For lambda usage

            this.file = DefaultStreamedContent.builder()
                    .name("learnpair.csv")
                    .contentType("application/octet-stream")
                    .stream(() -> {
                        try {
                            return new FileInputStream(fileToServe);
                        } catch (FileNotFoundException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .build();
        } catch (IOException iOException) {
            this.addMsg("Error: " + iOException.getMessage());
        }
    }

    /**
     * Add error message to the GUI
     *
     * @param msg
     */
    private void addMsg(String msg) {
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

    /**
     * Add error message to the GUI
     *
     * @param msg
     */
    private void addErrorMsg(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
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

    public StreamedContent getFile() {
        return file;
    }

    public void setFile(StreamedContent file) {
        this.file = file;
    }

    public String getProfitThisYear() {
        return profitThisYear;
    }

    public String getProfitLastYear() {
        return profitLastYear;
    }

}
