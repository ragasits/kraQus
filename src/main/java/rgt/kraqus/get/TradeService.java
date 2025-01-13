package rgt.kraqus.get;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import rgt.kraqus.Config;
import rgt.kraqus.MyException;

/**
 *
 * @author rgt
 */
@ApplicationScoped
public class TradeService {

    @RestClient
    private KrakenClientService krakenClient;
    
    @Inject
    private Config config;
    

    private int pairTradeSize = 0;

    /**
     * Call trades from Kraken Rest API
     *
     * @param last
     */
    public void callKrakenTrade(String last) {

        //config.setRunTrade(false);
        try {
            JsonObject tradeJson = this.getRestTrade(last);
            List<TradePairDTO> pairList = this.convertToDTO(tradeJson);

            if (!pairList.isEmpty()) {
                this.pairTradeSize = pairList.size();
                config.getTradePairColl().insertMany(pairList);

                Log.info("Trade Fired .... " + this.pairTradeSize + " " + pairList.get(0).getLastDate());
            } else {
                this.logTradeInfo(tradeJson);
            }

            //config.setRunTrade(true);
        } catch (MyException ex) {
            Log.error(ex.getMessage());
        }
    }

    /**
     * Logging tradeJson.toString() Solving Some warning: Conditionally
     * executed code should be reachable:
     *
     * @param tradeJson
     */
    private void logTradeInfo(JsonObject tradeJson) {
        if (tradeJson == null) {
            Log.info("Trade Fired....tradeJson is null");
        } else {
            String tradeJsonStr = tradeJson.toString();
            Log.info("Trade Fired.... Error "+tradeJsonStr);
        }
    }

    /**
     * Convert JSON to DTO
     *
     * @param ob
     * @return
     */
    private List<TradePairDTO> convertToDTO(JsonObject ob) {
        List<TradePairDTO> tradePairList = new ArrayList<>();
        String last = null;
        try {
            last = ob.asJsonObject().getJsonObject("result").getString("last");
        } catch (NullPointerException e) {
            Log.error(e.getMessage());
            return tradePairList;
        }

        JsonArray errors = ob.asJsonObject().getJsonArray("error");
        StringBuilder sb = new StringBuilder();
        for (JsonValue e : errors) {
            sb = sb.append(" ").append(e.toString());
        }
        String error = sb.toString().trim();
        String pair = "XXBTZEUR";

        JsonArray jsonPairs = ob.asJsonObject().getJsonObject("result").getJsonArray(pair);
        for (JsonValue p : jsonPairs) {
            tradePairList.add(
                    new TradePairDTO(
                            new BigDecimal(p.asJsonArray().getString(0)),
                            new BigDecimal(p.asJsonArray().getString(1)),
                            p.asJsonArray().getJsonNumber(2).bigDecimalValue(),
                            p.asJsonArray().getString(3),
                            p.asJsonArray().getString(4),
                            p.asJsonArray().getString(5),
                            error, last, pair)
            );
        }
        return tradePairList;
    }

    /**
     * REST client, get data from Kraken Get https cert on the fly
     *
     * @return
     */
    private JsonObject getRestTrade(String last) throws MyException {

        Response response = krakenClient.getTrade("XBTEUR", last);
        Log.info(response.getStatus());

        if (response.getStatus() != 200) {
            throw new MyException("getRestTrade error:" + response.getStatus());
        }

        InputStream inputStream = response.readEntity(InputStream.class);
        InputStreamReader in = new InputStreamReader(inputStream);
        JsonReader reader = Json.createReader(in);
        return reader.readObject();
    }

}
