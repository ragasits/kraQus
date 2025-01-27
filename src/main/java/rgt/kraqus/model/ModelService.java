package rgt.kraqus.model;

import com.mongodb.client.model.Sorts;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
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
                throw new MyException("Weka error", ex);
            }

        }
        dataset.setClassIndex(dataset.numAttributes() - 1);

        try {
            //Run model
            Classifier classifier = (Classifier) SerializationHelper.read(model.getModelFileStream());
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
                    dto.setChkMessage(instance.toString());
                    learnService.add(dto);
                }
            }

        } catch (Exception ex) {
            throw new MyException("Weka error", ex);
        }
    }
}
