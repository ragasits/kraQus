package rgt.kraqus.web;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import java.io.*;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import rgt.kraqus.export.ExportHistRSIDiffService;
import rgt.kraqus.export.ExportOneCandleService;
import rgt.kraqus.export.ExportType;
import rgt.kraqus.learn.LearnDTO;
import rgt.kraqus.learn.LearnService;
import weka.core.Instances;
import weka.core.converters.AbstractFileSaver;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVSaver;

/**
 * JSF bean for Export
 *
 * @author rgt
 */
@SessionScoped
@Named(value = "exportBean")
public class ExportBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private StreamedContent file;
    private long selectedBuyTime;
    private long selectedSellTime;
    private ExportType selectedExportType;
    private String selectedLearn;

    @Inject
    private LearnService learnService;

    @Inject
    private ExportOneCandleService exportOneCandleService;

    @Inject
    private ExportHistRSIDiffService exportHistRSIDiffService;

    /**
     * Show messages
     *
     */
    private void addMsg(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    /**
     * Update Sell, Buy lists
     */
    public void updateLists() {
        this.selectedBuyTime = learnService.getFirst(this.selectedLearn).getStartDate().getTime();
        this.selectedSellTime = learnService.getLast(this.selectedLearn).getStartDate().getTime();
    }

    /**
     * Get Export types lists
     *
     * @return
     */
    public ExportType[] getExportTypes() {
        return ExportType.values();
    }

    /**
     * Get all Learn
     *
     * @return
     */
    public List<LearnDTO> getLearnList() {
        if (this.selectedLearn != null) {
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
     * Export weka instance into ARFF, CSV file
     *
     * @param type
     */
    public void onExport(String type) {

        Date buyDate = new Date(selectedBuyTime);
        Date sellDate = new Date(selectedSellTime);

        Instances instances;
        if (this.getSelectedExportType().equals(ExportType.OneCandle)) {
            instances = exportOneCandleService.toInstances(selectedLearn, this.getSelectedExportType(), buyDate, sellDate);
        } else {
            instances = exportHistRSIDiffService.toInstances(selectedLearn, this.getSelectedExportType(), buyDate, sellDate);
        }

        String filename = this.getSelectedExportType().toString() + "." + type;

        AbstractFileSaver saver;
        File tempFile = null;
        try {
            if (type.equals("arff")) {
                saver = new ArffSaver();
            } else {
                saver = new CSVSaver();
            }

            // Create temp file with correct suffix
            tempFile = File.createTempFile("export-", "." + type);
            saver.setInstances(instances);
            saver.setFile(tempFile);
            saver.writeBatch();

            final File fileToServe = tempFile;  // For lambda usage

            this.file = DefaultStreamedContent.builder()
                    .name(filename)
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
            return;
        }
    }

    public StreamedContent getFile() {
        return file;
    }

    public void setFile(StreamedContent file) {
        this.file = file;
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

    public ExportType getSelectedExportType() {
        return selectedExportType;
    }

    public void setSelectedExportType(ExportType selectedExportType) {
        this.selectedExportType = selectedExportType;
    }
}
