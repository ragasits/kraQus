package rgt.kraqus.prod;

import java.util.Date;
import org.bson.types.ObjectId;

/**
 *
 * @author rgt
 */
public class ProdLogDTO {
    
    private ObjectId id;
    private final Date runDate;
    private final boolean runProduction;
    private final boolean runTrade;
    private final boolean runCandle;
    private final Date stopdate;
    private final long duration;

    public ProdLogDTO(Date runDate, boolean runProduction, boolean runTrade, boolean runCandle, Date stopdate) {
        this.runDate = runDate;
        this.runProduction = runProduction;
        this.runTrade = runTrade;
        this.runCandle = runCandle;
        this.stopdate = stopdate;
        this.duration = stopdate.getTime()-runDate.getTime();
    }

    public ObjectId getId() {
        return id;
    }

    public Date getRunDate() {
        return runDate;
    }

    public boolean isRunProduction() {
        return runProduction;
    }

    public boolean isRunTrade() {
        return runTrade;
    }

    public boolean isRunCandle() {
        return runCandle;
    }

    public Date getStopdate() {
        return stopdate;
    }

    public long getDuration() {
        return duration;
    }
}
