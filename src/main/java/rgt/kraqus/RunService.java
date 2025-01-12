package rgt.kraqus;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import rgt.kraqus.get.TradeService;

/**
 *
 * @author rgt
 */
@ApplicationScoped
public class RunService {

    @Inject
    TradeService trade;

    int i = 0;

    @Scheduled(cron = "{cron.expr}")
    void startProcess(ScheduledExecution execution) {

        Log.info("run schedule:" + i++);

        trade.callKrakenTrade(null);

    }

}
