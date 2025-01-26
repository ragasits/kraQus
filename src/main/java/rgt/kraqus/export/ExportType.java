package rgt.kraqus.export;

/**
 * Add export types
 */
public enum ExportType {
    OneCandle, //One Candle Row
    HistRSIDiff, //10 Hist CCi in one row
    
    
    OneCandleBin, //One candle for binary classification

    HistCandle, //10 Hist candle in one row
    HistCCi //10 Hist CCi in one row
}
