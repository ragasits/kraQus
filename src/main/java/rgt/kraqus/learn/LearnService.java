package rgt.kraqus.learn;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.lte;
import static com.mongodb.client.model.Filters.ne;

import com.mongodb.client.model.Sorts;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Calendar;
import rgt.kraqus.MyConfig;
import rgt.kraqus.calc.CandleDTO;
import rgt.kraqus.calc.CandleService;

/**
 * Manage learning data
 *
 * @author rgt
 */
@ApplicationScoped
public class LearnService {

    private static final String STARTDATE = "startDate";
    private static final String LEARNNAME = "name";
    private static final String TRADE = "trade";
    private static final String CHK_MESSAGE = "chkMessage";

    @Inject
    private MyConfig config;

    @Inject
    private CandleService candleService;

    /**
     * Get all learns
     *
     * @return
     */
    public List<LearnDTO> get() {
        return config.getLearnColl()
                .find()
                .sort(Sorts.ascending(STARTDATE))
                .into(new ArrayList<>());
    }

    /**
     * Get Learns by Candle
     *
     * @param startDate
     * @return
     */
    public List<LearnDTO> get(Date startDate) {
        return config.getLearnColl()
                .find(eq(STARTDATE, startDate))
                .sort(Sorts.ascending(STARTDATE))
                .into(new ArrayList<>());
    }

    /**
     * Get learns filter by Name
     *
     * @param learnName
     * @return
     */
    public List<LearnDTO> get(String learnName) {
        return config.getLearnColl()
                .find(eq(LEARNNAME, learnName))
                .sort(Sorts.ascending(STARTDATE))
                .into(new ArrayList<>());
    }

    /**
     * Get Learns filter by name and check
     * @param learnName
     * @param onlyErrors
     * @return 
     */
    public List<LearnDTO> get(String learnName, boolean onlyErrors) {

        if (onlyErrors) {

            return config.getLearnColl()
                    .find(and(eq(LEARNNAME, learnName), ne(CHK_MESSAGE, null)))
                    .sort(Sorts.ascending(STARTDATE))
                    .into(new ArrayList<>());

        } else {
            return this.get(learnName);
        }
    }

    /**
     * Get learns filter by:
     *
     * @param learnName
     * @param buyDate
     * @param sellDate
     * @return
     */
    public List<LearnDTO> get(String learnName, Date buyDate, Date sellDate) {
        return config.getLearnColl()
                .find(
                        and(
                                eq(LEARNNAME, learnName),
                                gte(STARTDATE, buyDate),
                                lte(STARTDATE, sellDate)
                        )
                )
                .sort(Sorts.ascending(STARTDATE))
                .into(new ArrayList<>());
    }

    /**
     * Get one learn
     *
     * @param learnName
     * @param startDate
     * @return
     */
    public LearnDTO get(String learnName, Date startDate) {
        return config.getLearnColl()
                .find(and(eq(LEARNNAME, learnName), eq(STARTDATE, startDate)))
                .first();
    }

    /**
     * Get first learn
     *
     * @param learnName
     * @return
     */
    public LearnDTO getFirst(String learnName) {
        return config.getLearnColl()
                .find(eq(LEARNNAME, learnName))
                .sort(Sorts.ascending(STARTDATE))
                .first();
    }

    /**
     * Get last learn
     *
     * @param learnName
     * @return
     */
    public LearnDTO getLast(String learnName) {
        return config.getLearnColl()
                .find(eq(LEARNNAME, learnName))
                .sort(Sorts.descending(STARTDATE))
                .first();
    }

    /**
     * Get BUYs
     *
     * @return
     */
    public List<LearnDTO> getBuy() {
        return config.getLearnColl()
                .find(eq(TRADE, "buy"))
                .sort(Sorts.ascending(STARTDATE))
                .into(new ArrayList<>());
    }

    /**
     * Get the last startDate from the last month
     *
     * @param learnName
     * @param year
     * @return
     */
    public Date getThisYearSell(String learnName) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date date = cal.getTime();

        LearnDTO learn = config.getLearnColl()
                .find(
                        and(
                                eq(LEARNNAME, learnName),
                                eq(TRADE, "sell"),
                                lt(STARTDATE, date)
                        )
                )
                .sort(Sorts.descending(STARTDATE))
                .first();

        return learn.getStartDate();
    }

    /**
     * Get the first startDate from this year
     *
     * @param learnName
     * @param year
     * @return
     */
    public Date getThisYearBuy(String learnName, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, 0, 1, 0, 0, 0);
        Date date = cal.getTime();

        LearnDTO learn = config.getLearnColl()
                .find(
                        and(
                                eq(LEARNNAME, learnName),
                                eq(TRADE, "buy"),
                                gte(STARTDATE, date)
                        )
                )
                .sort(Sorts.ascending(STARTDATE))
                .first();

        return learn.getStartDate();

    }

    /**
     * Get the first 'buy' trade start date that is at least one year and one
     * month earlier from current date.
     *
     * This method calculates a date by subtracting one year and one month from
     * the current date, then finds the earliest 'buy' trade on or after this
     * calculated date for the specified learnName.
     *
     * @param learnName the name identifier for the learn
     * @return the start date of the first 'buy' trade after the calculated date
     */
    public Date getLastYearBuy(String learnName) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);    // Subtract 1 year

        // Reset day and time fields
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();

        LearnDTO learn = config.getLearnColl()
                .find(
                        and(
                                eq(LEARNNAME, learnName),
                                eq(TRADE, "buy"),
                                gte(STARTDATE, date)
                        )
                )
                .sort(Sorts.ascending(STARTDATE))
                .first();

        return learn.getStartDate();

    }

    /**
     * Get Buys by learnName
     *
     * @param learnName
     * @return
     */
    public List<LearnDTO> getBuy(String learnName) {
        return config.getLearnColl()
                .find(
                        and(
                                eq(TRADE, "buy"),
                                eq("name", learnName)
                        )
                )
                .sort(Sorts.ascending(STARTDATE))
                .into(new ArrayList<>());
    }

    /**
     * Get SELLs
     *
     * @return
     */
    public List<LearnDTO> getSell() {
        return config.getLearnColl()
                .find(eq(TRADE, "sell"))
                .sort(Sorts.ascending(STARTDATE))
                .into(new ArrayList<>());
    }

    /**
     * Get Sell by learnName
     *
     * @param learnName
     * @return
     */
    public List<LearnDTO> getSell(String learnName) {
        return config.getLearnColl()
                .find(
                        and(
                                eq(TRADE, "sell"),
                                eq("name", learnName)
                        )
                )
                .sort(Sorts.ascending(STARTDATE))
                .into(new ArrayList<>());
    }

    /**
     * Get only unique names
     *
     * @return
     */
    public List<String> getNames() {
        return config.getLearnColl()
                .distinct(LEARNNAME, String.class)
                .into(new ArrayList<>());
    }

    /**
     * Add new learn data
     *
     * @param dto
     */
    public void add(LearnDTO dto) {
        config.getLearnColl().insertOne(dto);
    }

    /**
     * Modify existing learning data
     *
     * @param dto
     */
    public void update(LearnDTO dto) {
        config.getLearnColl().replaceOne(
                eq("_id", dto.getId()), dto);
    }

    /**
     * Delete existing learning data
     *
     * @param dto
     */
    public void delete(LearnDTO dto) {
        config.getLearnColl().deleteOne(eq("_id", dto.getId()));
    }

    /**
     * Delete learning data
     *
     * @param learnName
     */
    public void delete(String learnName) {
        config.getLearnColl().deleteMany(
                eq("name", learnName)
        );
    }

    /**
     * Looking for the best position for the learns
     */
    public void chkLearnPeaks() {

        ArrayList<LearnDTO> learnList = (ArrayList<LearnDTO>) this.get();
        for (LearnDTO learn : learnList) {
            StringBuilder chkMessage = new StringBuilder();

            //Get Current
            CandleDTO current = candleService.get(learn.getStartDate());

            //Before 5
            ArrayList<CandleDTO> beforeList = config.getCandleColl()
                    .find(lt(STARTDATE, learn.getStartDate()))
                    .sort(Sorts.descending(STARTDATE))
                    .limit(5)
                    .into(new ArrayList<>());

            //After 5
            ArrayList<CandleDTO> afterList = config.getCandleColl()
                    .find(gt(STARTDATE, learn.getStartDate()))
                    .sort(Sorts.ascending(STARTDATE))
                    .limit(5)
                    .into(new ArrayList<>());

            //chk it
            beforeList.addAll(afterList);
            for (CandleDTO candle : afterList) {
                if (learn.getTrade().equals("buy")) {
                    if (candle.getClose().compareTo(current.getClose()) == -1) {
                        chkMessage.append(candle.getClose()).append(",");
                    }
                } else {
                    if (candle.getClose().compareTo(current.getClose()) == 1) {
                        chkMessage.append(candle.getClose()).append(",");
                    }
                }
            }

            //Store Result
            if (chkMessage.length() != 0) {
                learn.setChkMessage(chkMessage.toString());
            } else {
                learn.setChkMessage(null);
            }
            this.update(learn);
        }

    }

    /**
     * Looking for wrong learn pairs
     *
     * @param learnName
     */
    public void chkLearnPairs(String learnName) {

        ArrayList<LearnDTO> learnList = (ArrayList<LearnDTO>) this.get(learnName);
        for (int i = 0; i < learnList.size(); i++) {
            StringBuilder chkMessage = new StringBuilder();
            LearnDTO learn = learnList.get(i);

            if (i == 0) {
                //Fist element
                if (!learn.getTrade().equals("buy")) {
                    chkMessage.append("First element not BUY");
                }
            } else {
                //Looking for wrong pairs
                LearnDTO prev = learnList.get(i - 1);

                if (learn.getTrade().equals("buy") && prev.getTrade().equals("buy")) {
                    chkMessage.append("Wrong pairs - buy");
                } else if (learn.getTrade().equals("sell") && prev.getTrade().equals("sell")) {
                    chkMessage.append("Wrong pairs - sell");
                }
            }

            //Last element
            if ((i == learnList.size() - 1) && (!learn.getTrade().equals("sell"))) {
                chkMessage.append("First element not sell");
            }

            //Store Result
            if (chkMessage.length() != 0) {
                learn.setChkMessage(chkMessage.toString());
            } else {
                learn.setChkMessage(null);
            }
            this.update(learn);

        }

    }
}
