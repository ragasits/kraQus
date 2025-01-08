package rgt.kraqus;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.logging.Log;

/**
 *
 * @author rgt
 */
@ApplicationScoped
public class RunService {
    
    int i = 0;



    @Scheduled(cron = "{cron.expr}")
    void startProcess(ScheduledExecution execution) {
        Log.info("run schedule:"+i++);
    }

}
