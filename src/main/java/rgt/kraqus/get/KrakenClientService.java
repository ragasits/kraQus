package rgt.kraqus.get;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Kraken Trades REST client definition
 * @author rgt
 */
@Path("/Trades")
@RegisterRestClient(configKey="kraken-api")
public interface KrakenClientService {

    @GET
    Response getTrade(@QueryParam("pair") String pair, @QueryParam("since") String since);
}
    