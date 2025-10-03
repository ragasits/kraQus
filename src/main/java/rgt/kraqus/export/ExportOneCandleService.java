package rgt.kraqus.export;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
public class ExportOneCandleService {

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
        attributes.add(new Attribute("count"));
        attributes.add(new Attribute("countBuy"));
        attributes.add(new Attribute("countSell"));
        attributes.add(new Attribute("open"));
        attributes.add(new Attribute("low"));
        attributes.add(new Attribute("high"));
        attributes.add(new Attribute("close"));
        attributes.add(new Attribute("total"));
        attributes.add(new Attribute("totalBuy"));
        attributes.add(new Attribute("totalSell"));
        attributes.add(new Attribute("volume"));
        attributes.add(new Attribute("volumeBuy"));
        attributes.add(new Attribute("volumeSell"));
        attributes.add(new Attribute("sma20"));
        attributes.add(new Attribute("ema9"));
        attributes.add(new Attribute("ema12"));
        attributes.add(new Attribute("ema26"));
        attributes.add(new Attribute("stDev"));
        attributes.add(new Attribute("bollingerUpper"));
        attributes.add(new Attribute("bollingerLower"));
        attributes.add(new Attribute("bollingerBandWidth"));
        attributes.add(new Attribute("tradeLower"));
        attributes.add(new Attribute("tradeUpper"));

        attributes.add(new Attribute("bollingerBuy", booleanValues));
        attributes.add(new Attribute("bollingerSell", booleanValues));

        attributes.add(new Attribute("change"));
        attributes.add(new Attribute("gain"));
        attributes.add(new Attribute("loss"));
        attributes.add(new Attribute("avgGain"));
        attributes.add(new Attribute("avgLoss"));
        attributes.add(new Attribute("rs"));
        attributes.add(new Attribute("rsi"));

        attributes.add(new Attribute("rsiBuy", booleanValues));
        attributes.add(new Attribute("rsiSell", booleanValues));

        attributes.add(new Attribute("macdLine"));
        attributes.add(new Attribute("signalLine"));
        attributes.add(new Attribute("macdHistogram"));

        attributes.add(new Attribute("bullMarket", booleanValues));
        attributes.add(new Attribute("bearMarket", booleanValues));
        attributes.add(new Attribute("crossover", booleanValues));

        attributes.add(new Attribute("typicalPrice"));
        attributes.add(new Attribute("sma20Typical"));
        attributes.add(new Attribute("mad20"));
        attributes.add(new Attribute("cci20"));

        attributes.add(new Attribute("overBought", booleanValues));
        attributes.add(new Attribute("overSold", booleanValues));

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
        instance.setValue(instances.attribute("count"), dto.getCount());
        instance.setValue(instances.attribute("countBuy"), dto.getCountBuy());
        instance.setValue(instances.attribute("countSell"), dto.getCountSell());
        instance.setValue(instances.attribute("open"), dto.getOpen().doubleValue());
        instance.setValue(instances.attribute("low"), dto.getLow().doubleValue());
        instance.setValue(instances.attribute("high"), dto.getHigh().doubleValue());
        instance.setValue(instances.attribute("close"), dto.getClose().doubleValue());
        instance.setValue(instances.attribute("total"), dto.getTotal().doubleValue());
        instance.setValue(instances.attribute("totalBuy"), dto.getTotalBuy().doubleValue());
        instance.setValue(instances.attribute("totalSell"), dto.getTotalSell().doubleValue());
        instance.setValue(instances.attribute("volume"), dto.getVolume().doubleValue());
        instance.setValue(instances.attribute("volumeBuy"), dto.getVolumeBuy().doubleValue());
        instance.setValue(instances.attribute("volumeSell"), dto.getVolumeSell().doubleValue());
        //MovingAverage
        instance.setValue(instances.attribute("sma20"), dto.getMovingAverage().getSma20().doubleValue());
        instance.setValue(instances.attribute("ema9"), dto.getMovingAverage().getEma9().doubleValue());
        instance.setValue(instances.attribute("ema12"), dto.getMovingAverage().getEma12().doubleValue());
        instance.setValue(instances.attribute("ema26"), dto.getMovingAverage().getEma26().doubleValue());
        //BollingerBand
        instance.setValue(instances.attribute("stDev"), dto.getBollinger().getStDev().doubleValue());
        instance.setValue(instances.attribute("bollingerUpper"), dto.getBollinger().getBollingerUpper().doubleValue());
        instance.setValue(instances.attribute("bollingerLower"), dto.getBollinger().getBollingerLower().doubleValue());
        instance.setValue(instances.attribute("bollingerBandWidth"), dto.getBollinger().getBollingerBandWidth().doubleValue());
        instance.setValue(instances.attribute("tradeLower"), dto.getBollinger().getTradeLower().doubleValue());
        instance.setValue(instances.attribute("tradeUpper"), dto.getBollinger().getTradeUpper().doubleValue());
        instance.setValue(instances.attribute("bollingerBuy"), Boolean.toString(dto.getBollinger().isBollingerBuy()));
        instance.setValue(instances.attribute("bollingerSell"), Boolean.toString(dto.getBollinger().isBollingerSell()));
        //RSI
        instance.setValue(instances.attribute("change"), dto.getRsi().getChange().doubleValue());
        instance.setValue(instances.attribute("gain"), dto.getRsi().getGain().doubleValue());
        instance.setValue(instances.attribute("loss"), dto.getRsi().getLoss().doubleValue());
        instance.setValue(instances.attribute("avgGain"), dto.getRsi().getAvgGain().doubleValue());
        instance.setValue(instances.attribute("avgLoss"), dto.getRsi().getAvgLoss().doubleValue());
        instance.setValue(instances.attribute("rs"), dto.getRsi().getRs().doubleValue());
        instance.setValue(instances.attribute("rsi"), dto.getRsi().getRsi().doubleValue());
        instance.setValue(instances.attribute("rsiBuy"), Boolean.toString(dto.getRsi().isRsiBuy()));
        instance.setValue(instances.attribute("rsiSell"), Boolean.toString(dto.getRsi().isRsiSell()));
        //MACD
        instance.setValue(instances.attribute("macdLine"), dto.getMacd().getMacdLine().doubleValue());
        instance.setValue(instances.attribute("signalLine"), dto.getMacd().getSignalLine().doubleValue());
        instance.setValue(instances.attribute("macdHistogram"), dto.getMacd().getMacdHistogram().doubleValue());
        instance.setValue(instances.attribute("bullMarket"), Boolean.toString(dto.getMacd().isBullMarket()));
        instance.setValue(instances.attribute("bearMarket"), Boolean.toString(dto.getMacd().isBearMarket()));
        instance.setValue(instances.attribute("crossover"), Boolean.toString(dto.getMacd().isCrossover()));
        //MACD
        instance.setValue(instances.attribute("typicalPrice"), dto.getCci().getTypicalPrice().doubleValue());
        instance.setValue(instances.attribute("sma20Typical"), dto.getCci().getSma20Typical().doubleValue());
        instance.setValue(instances.attribute("mad20"), dto.getCci().getMad20().doubleValue());
        instance.setValue(instances.attribute("cci20"), dto.getCci().getCci20().doubleValue());
        instance.setValue(instances.attribute("overBought"), Boolean.toString(dto.getCci().isOverBought()));
        instance.setValue(instances.attribute("overSold"), Boolean.toString(dto.getCci().isOverSold()));

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
     * Create ARFF relation message
     * @param exportType
     * @param candle
     * @return 
     */
    private String getRelation(ExportType exportType, CandleDTO candle) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddkkmm");

        return sb.append(exportType.toString())
                .append("-")
                .append(df.format(candle.getStartDate()))
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

    /**
     * Convert a single candle to WEKA Instances for prediction.
     *
     * @param exportType Export type for attribution.
     * @param candle Candle data to convert.
     * @return WEKA Instances object with one instance.
     */
    public Instances toInstances(ExportType exportType, CandleDTO candle) {

        ArrayList<Attribute> attributes = this.getAttributes();
        Instances instances = new Instances(this.getRelation(exportType, candle), attributes, 0);

        DenseInstance instance = this.getValues(candle, instances);
        //Set empty trade
        instance.setMissing(instances.attribute("trade"));
        instances.add(instance);

        instances.setClassIndex(instances.attribute("trade").index());
        return instances;
    }
}
