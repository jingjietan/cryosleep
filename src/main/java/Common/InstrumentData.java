package Common;

public class InstrumentData {
    public String instrumentID;
    public String currency;
    public int lotSize;

    public InstrumentData(String instrumentID, String currency, int lotSize) {
        this.instrumentID = instrumentID;
        this.currency = currency;
        this.lotSize = lotSize;
    }

    public String getInstrumentID() {
        return instrumentID;
    }

    public void setInstrumentID(String instrumentID) {
        this.instrumentID = instrumentID;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getLotSize() {
        return lotSize;
    }

    public void setLotSize(int lotSize) {
        this.lotSize = lotSize;
    }

    @Override
    public String toString() {
        return "InstrumentData{" +
                "instrumentID='" + instrumentID + '\'' +
                ", currency='" + currency + '\'' +
                ", lotSize=" + lotSize +
                '}';
    }
}
