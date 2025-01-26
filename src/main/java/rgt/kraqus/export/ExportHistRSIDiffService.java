package rgt.kraqus.export;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import rgt.kraqus.calc.CandleDTO;
import rgt.kraqus.calc.CandleService;
import rgt.kraqus.learn.LearnDTO;
import rgt.kraqus.learn.LearnService;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

/**
 * Export candle to CSV / ARFF format
 *
 * @author rgt
 */
@ApplicationScoped
public class ExportHistRSIDiffService {
    
    @Inject
    private LearnService learnService;
    
    @Inject
    private CandleService candleService;

    /**
     * Add learning data
     *
     * @param learnDto
     * @return
     */
    private String getTrade(LearnDTO learnDto) {
        String trade = "none";

        if (learnDto != null) {
            trade = learnDto.getTrade();
        }
        return trade;
    }

    /**
     * Convert predicted trade value to nominal
     *
     * @param trade
     * @return
     */
    public String getTradeNom(double trade) {
        switch ((int) trade) {
            case 1:
                return "buy";
            case 2:
                return "none";
            default:
                return "sell";
        }
    }

    /**
     * Create weka attributes
     *
     * @return
     */
    private ArrayList<Attribute> getAttributes() {
        ArrayList<String> booleanValues = new ArrayList<>();
        booleanValues.add("true");
        booleanValues.add("false");

        ArrayList<Attribute> attributes = new ArrayList<>();

        attributes.add(new Attribute("startDate", "yyyy-MM-dd HH:mm:ss"));
        attributes.add(new Attribute("rsi_1"));
        attributes.add(new Attribute("rsi_2"));
        attributes.add(new Attribute("rsi_3"));
        attributes.add(new Attribute("rsi_4"));
        attributes.add(new Attribute("rsi_5"));
        attributes.add(new Attribute("rsi_6"));
        attributes.add(new Attribute("rsi_7"));
        attributes.add(new Attribute("rsi_8"));
        attributes.add(new Attribute("rsi_9"));
        attributes.add(new Attribute("rsi_10"));

        ArrayList<String> tradeValues = new ArrayList<>();
        tradeValues.add("buy");
        tradeValues.add("none");
        tradeValues.add("sell");
        attributes.add(new Attribute("trade", tradeValues));

        return attributes;
    }

    /**
     * Create weka values
     *
     * @param dto
     * @param instances
     * @return
     */
    private DenseInstance getValues(CandleDTO dto, Instances instances) {
        DenseInstance instance = new DenseInstance(instances.numAttributes());
        instance.setDataset(instances);

        instance.setValue(instances.attribute("startDate"), dto.getStartDate().getTime());

        BigDecimal baseRSI = dto.getRsi().getRsi();
        List<CandleDTO> candles = candleService.getLasts(dto.getStartDate(), 10);

        int i = 1;
        for (CandleDTO candle : candles) {

            BigDecimal rsi = candle.getRsi().getRsi();
            BigDecimal diffRSI = rsi.subtract(baseRSI);

            instance.setValue(instances.attribute("rsi_" + i), diffRSI.doubleValue());
            i++;
        }
        return instance;

    }

    /**
     * Create WEKA @Relation text from the input parameters
     *
     * @param learnName
     * @param exportType
     * @param buyDate
     * @param sellDate
     * @return
     */
    private String getRelation(String learnName, ExportType exportType, Date buyDate, Date sellDate) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddkkmm");

        return sb.append(exportType.toString())
                .append("-")
                .append(learnName)
                .append("-")
                .append(df.format(buyDate))
                .append("-")
                .append(df.format(sellDate))
                .toString();
    }

    /**
     * Create ARFF relation message
     *
     * @param exportType
     * @param buyDate
     * @param sellDate
     * @return
     */
    private String getRelation(ExportType exportType, Date buyDate, Date sellDate) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddkkmm");

        return sb.append(exportType.toString())
                .append("-")
                .append(df.format(buyDate))
                .append("-")
                .append(df.format(sellDate))
                .toString();
    }

    /**
     * Create weka instance
     *
     * @param learnName
     * @param exportType
     * @param buyDate
     * @param sellDate
     * @return
     */
    public Instances toInstances(String learnName, ExportType exportType, Date buyDate, Date sellDate) {

        List<CandleDTO> candleList = candleService.get(buyDate, sellDate);
        ArrayList<Attribute> attributes = this.getAttributes();
        Instances instances = new Instances(this.getRelation(learnName, exportType, buyDate, sellDate), attributes, 0);

        for (CandleDTO candle : candleList) {
            DenseInstance instance = this.getValues(candle, instances);

            LearnDTO learnDto = learnService.get(learnName, candle.getStartDate());
            instance.setValue(instances.attribute("trade"), getTrade(learnDto));

            instances.add(instance);
        }

        instances.setClassIndex(instances.attribute("trade").index());
        return instances;
    }

    /**
     * Create weka instance - With empty LearnName and missing trade data
     *
     * @param exportType
     * @param buyDate
     * @param sellDate
     * @return
     */
    public Instances toInstances(ExportType exportType, Date buyDate, Date sellDate) {

        List<CandleDTO> candleList = candleService.get(buyDate, sellDate);
        ArrayList<Attribute> attributes = this.getAttributes();
        Instances instances = new Instances(this.getRelation(exportType, buyDate, sellDate), attributes, 0);

        for (CandleDTO candle : candleList) {
            DenseInstance instance = this.getValues(candle, instances);
            //Set empty trade
            instance.setMissing(instances.attribute("trade"));
            instances.add(instance);
        }

        instances.setClassIndex(instances.attribute("trade").index());
        return instances;
    }
}
