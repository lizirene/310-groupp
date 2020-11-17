package csci310.trading;

import java.util.ArrayList;
import java.util.List;

public class StockEndpoint {
    public String date;
    public double close;

    /**
     * Construct a StockEndpoint
     * @param {String} date
     * @param {double} close
     */
    public StockEndpoint(String date, double close) {
        this.date = date;
        this.close = close;
    }

    /**
     * getter for StockEndpoint given a StockEndpointOriginal
     * @param {StockEndpointOriginal} endpoint
     * @return StockEndpoint corresponding to the given StockEndpointOriginal
     */
    public static StockEndpoint parseOriginal(StockEndpointOriginal endpoint) {
        return new StockEndpoint(endpoint.date, endpoint.close);
    }

    /**
     * getter for a list of StockEndpoints given a list of StockEndpointOriginals
     * @param {List<StockEndpointOriginal>} endpoint
     * @return {List<StockEndpoint>} StockEndpoint corresponding to the given {List<StockEndpointOriginal>} StockEndpointOriginal
     */
    public static List<StockEndpoint> parseOriginalList(List<StockEndpointOriginal> endpoints) {
        List<StockEndpoint> simplified = new ArrayList<>();
        for (StockEndpointOriginal endpoint : endpoints) {
            simplified.add(parseOriginal(endpoint));
        }
        return simplified;
    }
}
