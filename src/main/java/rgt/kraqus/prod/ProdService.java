package rgt.kraqus.prod;

import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Calendar;
import java.util.Date;
import org.eclipse.microprofile.context.ManagedExecutor;
import rgt.kraqus.MyConfig;
import rgt.kraqus.MyException;
import rgt.kraqus.calc.CandleDTO;
import rgt.kraqus.calc.CandleService;
import rgt.kraqus.get.TradePairDTO;
import rgt.kraqus.get.TradeService;

/**
 *
 * @author rgt
 */
@ApplicationScoped
public class ProdService {

    @Inject
    MyConfig config;

    @Inject
    TradeService tradeService;

    @Inject
    CandleService candleService;

    @Inject
    ManagedExecutor executor;

    @Scheduled(cron = "{cron.expr}")
    public void startWork() {

        //Checks
        if (!config.isRunProduction()) {
            Log.info("isRunProduction: false");
            return;
        }

        executor.submit(() -> runProduction());
    }

    /**
     * Run scheduled production process
     */
    public void runProduction() {
        Date runDate = Calendar.getInstance().getTime();
        Log.info("runProduction: Start ");

        try {
            // get Trades
            runTrade(runDate);
        } catch (NumberFormatException | MyException ex) {
            Log.error(ex.getMessage());
        }

        //Calculate Candles
        candleService.deleteLastCandle();
        try {
            this.createCandle();
            candleService.callCandleProd();
        } catch (MyException ex) {
            Log.error(ex.getMessage());
        }
        config.setRunProduction(true);

        Log.info("runProduction: Done");
    }

    /**
     * Create candles
     *
     * @param runDate
     * @throws MyException
     */
    private void createCandle() throws MyException {
        Date startDate = candleService.getStartDate();
        Calendar cal = Calendar.getInstance();

        TradePairDTO dto = tradeService.getLast();
        if (dto == null || dto.getTimeDate() == null) {
            throw new MyException("Empty trade collection or invalid stop date");
        }

        //Generate candle dates
        Date stopDate = dto.getTimeDate();
        Log.info("createCandle: startDate: " + startDate);

        while (startDate.getTime() < stopDate.getTime()) {
            Log.debug("calcDateList " + startDate + " " + stopDate);

            config.getCandleColl().insertOne(new CandleDTO(startDate));
            cal.setTime(startDate);
            cal.add(Calendar.MINUTE, 30);
            startDate = cal.getTime();
        }
        Log.info("createCandle: stopDate: " + stopDate);

    }

    /**
     * Get Trades from Kraken
     *
     * @param runDate
     * @throws NumberFormatException
     */
    private void runTrade(Date runDate) throws NumberFormatException, MyException {
        config.setRunProduction(false);

        long runTime = runDate.getTime();
        String last = tradeService.getLastValue();
        long lastTime;

        if ("0".equals(last)) {
            lastTime = Long.parseLong(last);
        } else {
            lastTime = Long.parseLong(last.substring(0, 13));
        }

        Log.info("runTime: " + runTime + " lastTime: " + lastTime);

        // Get Trades
        while (runTime > lastTime) {
            tradeService.callKrakenTrade(last);
            last = tradeService.getLastValue();
            lastTime = Long.parseLong(last.substring(0, 13));
        }
    }
}
