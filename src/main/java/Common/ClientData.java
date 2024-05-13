package Common;

import java.util.Set;

public class ClientData {
    public String clientID;
    public Set<String> currencies;
    public boolean positionCheck;
    public int rating;

    public ClientData(String clientID, Set<String> currencies, boolean positionCheck, int rating){
        this.clientID = clientID;
        this.currencies = currencies;
        this.positionCheck = positionCheck;
        this.rating = rating;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public Set<String> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(Set<String> currencies) {
        this.currencies = currencies;
    }

    public boolean isPositionCheck() {
        return positionCheck;
    }

    public void setPositionCheck(boolean positionCheck) {
        this.positionCheck = positionCheck;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }


}
