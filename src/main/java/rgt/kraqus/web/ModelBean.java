package rgt.kraqus.web;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.bson.types.Binary;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.file.UploadedFile;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rgt.kraqus.MyException;
import rgt.kraqus.export.ExportType;
import rgt.kraqus.model.ModelDTO;
import rgt.kraqus.model.ModelService;

@SessionScoped
@Named(value = "modelBean")
public class ModelBean implements Serializable {

    private ModelDTO detail = new ModelDTO();

    @Inject
    private ModelService modelService;

    public List<String> complete(String query) {
        return modelService.getNames();
    }

    public void onSelectedName(SelectEvent<String> event) {
        this.detail = modelService.get(event.getObject());
    }

    /**
     * Run WEKA prediction
     */
    public void onRunWeka() {
        try {
            modelService.runWeka(detail);
        } catch (MyException ex) {
            Logger.getLogger(ModelBean.class.getName()).log(Level.SEVERE, null, ex);
            this.addErrMsg(ex.getMessage());
        }
    }

    /**
     * Add / Update model
     */
    public void onSave() {
        ModelDTO dto = modelService.get(this.detail.getModelName());

        if (dto == null) {
            this.detail.setId(null);
            modelService.add(this.detail);
        } else {
            modelService.update(this.detail);
        }
    }

    /**
     * Delete model
     */
    public void onDelete() {
        ModelDTO dto = modelService.get(this.detail.getModelName());

        if (dto != null) {
            modelService.delete(this.detail);
            this.detail = new ModelDTO();

        }
    }

    public String getSelectedName() {
        if (this.detail != null) {
            return this.detail.getModelName();
        }
        return null;
    }

    public void setSelectedName(String selectedName) {
        if (this.detail != null) {
            this.detail.setModelName(selectedName);
        }
    }

    public ModelDTO getDetail() {
        return detail;
    }

    public ExportType getSelectedExportType() {
        if (detail != null && this.detail.getExportType() != null) {
            return ExportType.valueOf(this.detail.getExportType());
        }
        return null;
    }

    public void setSelectedExportType(ExportType selectedExportType) {
        if (detail != null) {
            this.detail.setExportType(selectedExportType.toString());
        }
    }

    /**
     * Add error message
     *
     * @param msg
     */
    private void addErrMsg(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }
}
