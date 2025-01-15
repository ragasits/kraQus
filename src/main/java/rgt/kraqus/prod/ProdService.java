package rgt.kraqus.prod;

import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Calendar;
import java.util.Date;
import rgt.kraqus.KraqusConfig;
import rgt.kraqus.get.TradeService;

/**
 *
 * @author rgt
 */
@ApplicationScoped
public class ProdService {

    @Inject
    KraqusConfig config;

    @Inject
    TradeService tradeService;

    @Scheduled(cron = "{cron.expr}")
    public void runProduction() {
        Date runDate = Calendar.getInstance().getTime();
        Log.info("Start runProduction: " + runDate);

        //Checks
        if (!config.isRunProduction()) {
            Log.info("isRunProduction: false");
            return;
        }

        if (config.isRunTrade() || config.isRunCandle()) {
            Log.info("isRunTrade " + config.isRunTrade() + " isRunCandle: " + config.isRunCandle());
            return;
        }

        // get Trades
        runTrade(runDate);

//        //Calculate Candles
//        candleEjb.deleteLastCandle();
//        try {
//            this.createCandle(runDate);
//            candleEjb.callCandleProd();
//        } catch (MyException ex) {
//            Logger.getLogger(ProdEJB.class.getName()).log(Level.SEVERE, null, ex);
//        }
        config.setRunCandle(false);
        config.setRunProduction(true);

        Date stopdate = Calendar.getInstance().getTime();
        Log.info("Stop runProduction: " + stopdate);

        //Save log
        ProdLogDTO dto = new ProdLogDTO(runDate,
                config.isRunProduction(),
                config.isRunTrade(),
                config.isRunCandle(), stopdate);

        config.getProdLogColl().insertOne(dto);
    }

    /**
     * Get Trades from Kraken
     *
     * @param runDate
     * @throws NumberFormatException
     */
    private void runTrade(Date runDate) throws NumberFormatException {
        config.setRunProduction(false);

        long runTime = runDate.getTime();
        String last = tradeService.getLastValue();
        long lastTime;

        if ("0".equals(last)) {
            lastTime = Long.parseLong(last);
        } else {
            lastTime = Long.parseLong(last.substring(0, 13));
        }

        Log.info("runProduction: runTime: " + runTime + " lastTime: " + lastTime);

        int i = 0;

        // Get Trades
        while ((runTime > lastTime) && (i < 20)) {
            tradeService.callKrakenTrade(last);
            last = tradeService.getLastValue();
            lastTime = Long.parseLong(last.substring(0, 13));

            Log.info("i:" + i);

            i++;

        }

        config.setRunTrade(false);
    }
}
