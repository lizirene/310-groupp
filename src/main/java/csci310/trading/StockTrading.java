package csci310.trading;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

public class StockTrading {
    private static final String TOKEN = "c3ab62ad8727523a7af1ad342f3570c4b544c5f3";
    private static final String TINGO_HISTORICAL_PRICES_ENDPOINT = "https://api.tiingo.com/tiingo/daily/%s/prices";
    private static final String TINGO_STOCK_INFO_ENDPOINT = "https://api.tiingo.com/tiingo/daily/%s";

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public enum ResampleFreq{
        DAILY("daily"), WEEKLY("weekly"), MONTHLY("monthly"), ANNUALLY("annually");
        String value;
        ResampleFreq(String value) {
            this.value = value;
        }
    }

    // Internal in-memory cache
    static class HistoricalPriceQuery {
        String ticker;
        String startDate;
        String endDate;
        ResampleFreq freq;

        HistoricalPriceQuery(String ticker, String startDate, String endDate, ResampleFreq freq) {
            this.ticker = ticker.toUpperCase();
            this.startDate = startDate;
            this.endDate = endDate;
            this.freq = freq;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj.getClass() != HistoricalPriceQuery.class) {
                return false;
            }
            HistoricalPriceQuery other = (HistoricalPriceQuery) obj;
            return this.ticker.equals(other.ticker) &&
                    this.startDate.equals(other.startDate) &&
                    this.endDate.equals(other.endDate) &&
                    this.freq == other.freq;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.ticker, this.startDate, this.endDate, this.freq.value);
        }
    }

    static final Map<HistoricalPriceQuery, String> historicalPriceCache = new HashMap<>();
    static final Map<String, String> metadataCache = new HashMap<>();

    static int cacheHits = 0;
    static int cacheMiss = 0;

    /**
     * execute the given targetURL and return the response
     * @param {String} targetURL
     * @return {String} response
     */
    public String executeGet(String targetURL) {
        HttpURLConnection connection = null;

        StringBuilder content = null;

        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String line;
            content = new StringBuilder();

            while ((line = in.readLine()) != null) {

                content.append(line);
                content.append(System.lineSeparator());
            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (connection != null) {
                connection.disconnect();
            } else {
                System.out.println();
            }
        }

        return content != null ? content.toString() : null;
    }

    /**
     * get historical prices of a stock between a start and end date with a given frequency as JSON string
     * @param {String} symbol
     * @param {String} start
     * @param {String} end
     * @param {ResampleFreq} freq
     * @return {String} historical prices
     * @throws IOException
     */
    public String getHistoricalPricesJsonString(String ticker, String startDate, String endDate, ResampleFreq freq) throws IOException {
//        String urlParameters = "?token=" + URLEncoder.encode(TOKEN, "UTF-8") +
//                "&startDate=" + URLEncoder.encode(start, "UTF-8") +
//                "&endDate=" + URLEncoder.encode(end, "UTF-8") +
//                "&resampleFreq=" + URLEncoder.encode(freq.value, "UTF-8");
//        		System.out.println("requesting...");
//
//        String targetURL = String.format(TINGO_HISTORICAL_PRICES_ENDPOINT, symbol).concat(urlParameters);
        HistoricalPriceQuery newQuery = new HistoricalPriceQuery(
                ticker, startDate, endDate, freq
        );
        if (historicalPriceCache.containsKey(newQuery)) {
            cacheHits++;
            System.out.println("Requesting historical prices. Cache HIT: " + cacheHits);
            return historicalPriceCache.get(newQuery);
        } else {
            cacheMiss++;
            System.out.println("Requesting historical prices. Cache MISS: " + cacheMiss);
            // Get result using API
            String urlParameters = "?token=" + URLEncoder.encode(TOKEN, "UTF-8") +
                    "&startDate=" + URLEncoder.encode(startDate, "UTF-8") +
                    "&endDate=" + URLEncoder.encode(endDate, "UTF-8") +
                    "&resampleFreq=" + URLEncoder.encode(freq.value, "UTF-8");

            String targetURL = String.format(TINGO_HISTORICAL_PRICES_ENDPOINT, ticker).concat(urlParameters);
            String result = executeGet(targetURL);

            // Put result into cache
            historicalPriceCache.put(newQuery, result);

            return result;
        }
    }

    /**
     * get historical prices of a stock between a start and end date with a given frequency as a list of StockEndpoints
     * @param {String} symbol
     * @param {String} start
     * @param {String} end
     * @param {ResampleFreq} freq
     * @return {List<StockEndpoint>} historical prices if history exists; Otherwise, return null
     * @throws IOException
     */
    public List<StockEndpoint> getHistoricalPrices(String symbol, String start, String end, ResampleFreq freq) throws IOException {
        // sample url string:
        // https://api.tiingo.com/tiingo/daily/<ticker>/prices?startDate=2012-1-1&endDate=2016-1-1&format=csv&resampleFreq=monthly
        String response = getHistoricalPricesJsonString(symbol, start, end, freq);
        StockEndpointOriginal[] endpoints = gson.fromJson(response, StockEndpointOriginal[].class);

        // Protect if returned data is null due to e.g. invalid ticker
        if (endpoints == null) {
            return null;
        }

        List<StockEndpoint> simplified = StockEndpoint.parseOriginalList(Arrays.asList(endpoints));
        return simplified;
    }

    public String getStockMetadata(String ticker) throws UnsupportedEncodingException {
        if (metadataCache.containsKey(ticker)) {
            cacheHits++;
            System.out.println("Requesting stock metadata. Cache HIT: " + cacheHits);
            return metadataCache.get(ticker);
        }

        cacheMiss++;
        System.out.println("Requesting stock metadata. Cache MISS: " + cacheMiss);

        String urlParameters = "?token=" + URLEncoder.encode(TOKEN, "UTF-8");
        String targetURL = String.format(TINGO_STOCK_INFO_ENDPOINT, ticker).concat(urlParameters);
        String result = executeGet(targetURL);

        metadataCache.put(ticker, result);
        return result;
    }

    /**
     * get stock's exchangeCode given a ticker
     * @param {String} ticker
     * @return {String} exchangeCode
     * @throws UnsupportedEncodingException
     */
    public String getStockExchangeCode(String ticker) throws UnsupportedEncodingException {
        String result = getStockMetadata(ticker);
        if (result == null) {
            return "";
        }
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> jsonResult = gson.fromJson(result, type);
        return jsonResult.get("exchangeCode").toUpperCase();
    }
}
