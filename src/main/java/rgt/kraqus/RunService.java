package rgt.kraqus;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.logging.Log;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import rgt.kraqus.get.KrakenClientService;

/**
 *
 * @author rgt
 */
@ApplicationScoped
public class RunService {

    @RestClient
    KrakenClientService krakenClient;

    int i = 0;

    @Scheduled(cron = "{cron.expr}")
    void startProcess(ScheduledExecution execution) {

        Log.info("run schedule:" + i++);

        Response response = krakenClient.getTrade("XBTEUR", null);
        Log.info(response.getStatus());
        

        
        
    }

}
