package rgt.kraqus.model;

import com.mongodb.client.model.Sorts;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Date;
import rgt.kraqus.MyConfig;
import rgt.kraqus.MyException;
import rgt.kraqus.calc.CandleDTO;
import rgt.kraqus.calc.CandleService;
import rgt.kraqus.export.ExportOneCandleService;
import rgt.kraqus.export.ExportType;
import rgt.kraqus.learn.LearnDTO;
import rgt.kraqus.learn.LearnService;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

@ApplicationScoped
public class ModelService {

    private static final String MODELNAME = "modelName";

    @Inject
    private MyConfig config;

    @Inject
    private ExportOneCandleService exportService;

    @Inject
    private LearnService learnService;

    @Inject
    private CandleService candleService;

    /**
     * Get Models
     *
     * @return
     */
    public List<ModelDTO> get() {
        return config.getModelColl()
                .find()
                .sort(Sorts.ascending(MODELNAME))
                .into(new ArrayList<>());
    }

    /**
     * Get one model
     *
     * @param modelName
     * @return
     */
    public ModelDTO get(String modelName) {
        return config.getModelColl()
                .find(eq(MODELNAME, modelName))
                .first();
    }

    /**
     * Get Model names
     *
     * @return
     */
    public List<String> getNames() {
        return config.getModelColl()
                .distinct(MODELNAME, String.class)
                .into(new ArrayList<>());
    }

    /**
     * Add Model to mongo
     *
     * @param dto
     */
    public void add(ModelDTO dto) {
        config.getModelColl().insertOne(dto);
    }

    /**
     * Update Model from mongo
     *
     * @param dto
     */
    public void update(ModelDTO dto) {
        config.getModelColl().replaceOne(
                eq("_id", dto.getId()), dto);
    }

    /**
     * Delete model from mongo
     *
     * @param dto
     */
    public void delete(ModelDTO dto) {
        //Delete item
        config.getModelColl().deleteOne(eq("_id", dto.getId()));
    }

    /**
     * Load a WEKA Classifier model from the file system based on the model
     * information.
     *
     * @param model The ModelDTO containing model metadata used to locate the
     * model file.
     * @return An instance of WEKA Classifier deserialized from the model file.
     * @throws MyException if the model file cannot be found, read, or
     * deserialized correctly.
     */
    private Classifier loadModelFromFile(ModelDTO model) throws MyException {
        String filePath = config.getModelDir() + "/" + model.getModelName() + ".model";
        try {
            return (Classifier) SerializationHelper.read(filePath);
        } catch (Exception e) {
            throw new MyException("Failed to load model from file: " + filePath, e);
        }
    }

    /**
     * Execute WEKA prediction
     *
     * @param model
     * @throws MyException
     */
    public void runWeka(ModelDTO model) throws MyException {
        //Delete old learn
        learnService.delete(model.getModelName());

        //Create instance
        Date buyDate = model.getFirstBuyDate();
        Date sellDate = model.getLastSellDate();
        Instances dataset = exportService.toInstances(ExportType.valueOf(model.getExportType()), buyDate, sellDate);

        //Save new learn - we save only the trades (buy, sell)
        List<CandleDTO> candleList = candleService.get(buyDate, sellDate);

        //Run remove
        if (!model.getRemoveAttributeIndices().isEmpty()) {
            Remove remove = new Remove();
            remove.setAttributeIndices(model.getRemoveAttributeIndices());
            remove.setInvertSelection(model.getRemoveInvertSelection());
            try {
                remove.setInputFormat(dataset);
                dataset = Filter.useFilter(dataset, remove);
            } catch (Exception ex) {
                throw new MyException("Weka error - remove", ex);
            }

        }
        dataset.setClassIndex(dataset.numAttributes() - 1);

        try {
            //Run model
            Classifier classifier = this.loadModelFromFile(model);
            for (int i = 0; i < dataset.numInstances(); i++) {
                Instance instance = dataset.instance(i);
                double prediction = classifier.classifyInstance(instance);
                instance.setClassValue(prediction);

                String trade = instance.stringValue(instance.classIndex());

                if (!trade.equals("none")) {
                    //Add new learn
                    CandleDTO candle = candleList.get(i);
                    LearnDTO dto = new LearnDTO();
                    dto.setName(model.getModelName());
                    dto.setStartDate(candle.getStartDate());
                    dto.setTrade(trade);
                    dto.setClose(candle.getClose());
                    learnService.add(dto);
                }
            }

        } catch (Exception ex) {
            throw new MyException("Weka error - prediction", ex);
        }
    }

    /**
     * Execute WEKA prediction for one candle
     *
     * @param model
     * @throws MyException
     */
    public void runWekaOneCandle(ModelDTO model, CandleDTO candle) throws MyException {

        //Create instance
        Instances dataset = exportService.toInstances(ExportType.valueOf(model.getExportType()), candle);

        //Run remove
        if (!model.getRemoveAttributeIndices().isEmpty()) {
            Remove remove = new Remove();
            remove.setAttributeIndices(model.getRemoveAttributeIndices());
            remove.setInvertSelection(model.getRemoveInvertSelection());
            try {
                remove.setInputFormat(dataset);
                dataset = Filter.useFilter(dataset, remove);
            } catch (Exception ex) {
                throw new MyException("Weka error - remove", ex);
            }

        }
        dataset.setClassIndex(dataset.numAttributes() - 1);

        try {
            //Run model
            Classifier classifier = this.loadModelFromFile(model);
            Instance instance = dataset.instance(0);
            double prediction = classifier.classifyInstance(instance);
            instance.setClassValue(prediction);

            String trade = instance.stringValue(instance.classIndex());

            if (!trade.equals("none")) {
                //Add new learn
                LearnDTO dto = new LearnDTO();
                dto.setName(model.getModelName());
                dto.setStartDate(candle.getStartDate());
                dto.setTrade(trade);
                dto.setClose(candle.getClose());
                learnService.add(dto);

                Log.info(":" + dto.getClose() + " runWekaOneCandle: " + candle.getStartDate() + ":" + trade);
            }
        } catch (Exception ex) {
            throw new MyException("Weka error - prediction", ex);
        }
    }

}
