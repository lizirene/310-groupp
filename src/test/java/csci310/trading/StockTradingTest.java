package csci310.trading;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class StockTradingTest extends Mockito {

    @Spy
    private StockTrading st;

    private boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    @Test(expected = Exception.class)
    public void testExecuteGetException() {
//        HttpURLConnection connection = mock(HttpURLConnection.class);
//        when(connection.getInputStream()).thenReturn(null);
        when(st.executeGet(any())).thenThrow(new Exception());
        assertNull(st.executeGet("https://www.google.com/"));
    }

    @Test
    public void testExecuteGet() {
        assertNull(st.executeGet("https://github.com/CSCI310/project-20203-group03-20203"));
    }

    @Test
    public void testgetHistoricalPricesJsonString() throws IOException {
        String symbol = "AAPL";
        String start = "2020-09-21";
        String end = "2020-09-23";
        String response = st.getHistoricalPricesJsonString(symbol, start, end, StockTrading.ResampleFreq.DAILY);

        // Test that the response is indeed a valid JSON string
        assertTrue(isJSONValid(response));
    }

    @Test
    public void testParseStockEndpointJson() throws IOException {
        String response = "[\n" +
                "  {\n" +
                "    \"date\": \"2020-09-21T00:00:00.000Z\",\n" +
                "    \"close\": 110.08,\n" +
                "    \"high\": 110.19,\n" +
                "    \"low\": 103.1,\n" +
                "    \"open\": 104.54,\n" +
                "    \"volume\": 195713815,\n" +
                "    \"adjClose\": 110.08,\n" +
                "    \"adjHigh\": 110.19,\n" +
                "    \"adjLow\": 103.1,\n" +
                "    \"adjOpen\": 104.54,\n" +
                "    \"adjVolume\": 195713815,\n" +
                "    \"divCash\": 0.0,\n" +
                "    \"splitFactor\": 1.0\n" +
                "  }\n" +
                "]";

        Mockito.doReturn(response).when(st).getHistoricalPricesJsonString(anyString(), anyString(), anyString(), any());

        // Provide dummy argument input
        List<StockEndpoint> endpoints = st.getHistoricalPrices("test", "test", "test", StockTrading.ResampleFreq.DAILY);

        assertEquals(endpoints.size(), 1);
        assertEquals(endpoints.get(0).date, "2020-09-21T00:00:00.000Z");
        assertEquals(endpoints.get(0).close, 110.1, 0.1);
    }

    @Test
    public void testGetHistoricalPrices() throws IOException {
        String symbol = "AAPL";
        String start = "2020-09-21";
        String end = "2020-09-23";
        List<StockEndpoint> historicalPrices = st.getHistoricalPrices(symbol, start, end, StockTrading.ResampleFreq.DAILY);
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        int days = Days.daysBetween(startDate, endDate).getDays();
        assertEquals(days + 1, historicalPrices.size());
        for (StockEndpoint historicalPrice : historicalPrices) {
            String date = historicalPrice.date;
            LocalDate currentDate = LocalDate.parse(date, DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
            assertEquals(currentDate, startDate);
            startDate = startDate.plusDays(1);
        }
    }

    @Test
    public void testGetHistoricalPricesCacheHit() throws IOException {
        // Make one request
        String symbol = "AAPL";
        String start = "2020-09-21";
        String end = "2020-09-23";
        StockTrading.ResampleFreq freq = StockTrading.ResampleFreq.DAILY;
        st.getHistoricalPrices(symbol, start, end, freq);

        int cacheHits = StockTrading.cacheHits;

        // Make same request again
        st.getHistoricalPrices(symbol, start, end, freq);

        assertEquals(cacheHits + 1, StockTrading.cacheHits);
    }

    @Test
    public void testGetHistoricalPricesInvalidTicker() throws IOException {
        String symbol = "AAPLE";
        String start = "2020-09-21";
        String end = "2020-09-23";
        List<StockEndpoint> historicalPrices = st.getHistoricalPrices(symbol, start, end, StockTrading.ResampleFreq.DAILY);
        assertNull(historicalPrices);
    }

    @Test
    public void testGetStockExchangeCode() throws IOException {
        String exchangeCode = st.getStockExchangeCode("aapl");
        assertEquals(exchangeCode, "NASDAQ");

        exchangeCode = st.getStockExchangeCode("aa");
        assertEquals(exchangeCode, "NYSE");

        exchangeCode = st.getStockExchangeCode("asdf");
        assertEquals(exchangeCode, "");
    }

    @Test
    public void testEqualsTrue() {
        // Test data
        String ticker = "aapl";
        String startDate = "10/01/2019";
        String endDate = "10/01/2020";
        StockTrading.ResampleFreq freq = StockTrading.ResampleFreq.DAILY;
        StockTrading.HistoricalPriceQuery query1 = new StockTrading.HistoricalPriceQuery(ticker, startDate, endDate, freq);
        StockTrading.HistoricalPriceQuery query2 = new StockTrading.HistoricalPriceQuery(ticker, startDate, endDate, freq);
        assertTrue(query1.equals(query2));
    }

    @Test
    public void testEqualsFalse1() {
        // Test data
        String ticker1 = "aapl";
        String ticker2 = "goog";
        String startDate = "10/01/2019";
        String endDate = "10/01/2020";
        StockTrading.ResampleFreq freq = StockTrading.ResampleFreq.DAILY;
        StockTrading.HistoricalPriceQuery query1 = new StockTrading.HistoricalPriceQuery(ticker1, startDate, endDate, freq);
        StockTrading.HistoricalPriceQuery query2 = new StockTrading.HistoricalPriceQuery(ticker2, startDate, endDate, freq);
        assertFalse(query1.equals(query2));
    }

    @Test
    public void testEqualsFalse2() {
        // Test data
        String ticker = "aapl";
        String startDate1 = "10/01/2019";
        String startDate2 = "10/02/2019";
        String endDate = "10/01/2020";
        StockTrading.ResampleFreq freq = StockTrading.ResampleFreq.DAILY;
        StockTrading.HistoricalPriceQuery query1 = new StockTrading.HistoricalPriceQuery(ticker, startDate1, endDate, freq);
        StockTrading.HistoricalPriceQuery query2 = new StockTrading.HistoricalPriceQuery(ticker, startDate2, endDate, freq);
        assertFalse(query1.equals(query2));
    }

    @Test
    public void testEqualsFalse3() {
        // Test data
        String ticker = "aapl";
        String startDate = "10/01/2019";
        String endDate1 = "10/01/2020";
        String endDate2 = "10/02/2020";
        StockTrading.ResampleFreq freq = StockTrading.ResampleFreq.DAILY;
        StockTrading.HistoricalPriceQuery query1 = new StockTrading.HistoricalPriceQuery(ticker, startDate, endDate1, freq);
        StockTrading.HistoricalPriceQuery query2 = new StockTrading.HistoricalPriceQuery(ticker, startDate, endDate2, freq);
        assertFalse(query1.equals(query2));
    }

    @Test
    public void testEqualsFalse4() {
        // Test data
        String ticker = "aapl";
        String startDate = "10/01/2019";
        String endDate = "10/01/2020";
        StockTrading.ResampleFreq freq1 = StockTrading.ResampleFreq.DAILY;
        StockTrading.ResampleFreq freq2 = StockTrading.ResampleFreq.WEEKLY;
        StockTrading.HistoricalPriceQuery query1 = new StockTrading.HistoricalPriceQuery(ticker, startDate, endDate, freq1);
        StockTrading.HistoricalPriceQuery query2 = new StockTrading.HistoricalPriceQuery(ticker, startDate, endDate, freq2);
        assertFalse(query1.equals(query2));
    }

    @Test
    public void testEqualsInvalid() {
        // Test data
        String ticker = "aapl";
        String startDate = "10/01/2019";
        String endDate1 = "10/01/2020";
        StockTrading.ResampleFreq freq = StockTrading.ResampleFreq.DAILY;
        StockTrading.HistoricalPriceQuery query1 = new StockTrading.HistoricalPriceQuery(ticker, startDate, endDate1, freq);
        assertFalse(query1.equals("something else"));
    }

    @Test
    public void testHashCodeEquals() {
        String ticker = "aapl";
        String startDate = "10/01/2019";
        String endDate = "10/01/2020";
        StockTrading.ResampleFreq freq = StockTrading.ResampleFreq.DAILY;
        StockTrading.HistoricalPriceQuery query1 = new StockTrading.HistoricalPriceQuery(ticker, startDate, endDate, freq);
        StockTrading.HistoricalPriceQuery query2 = new StockTrading.HistoricalPriceQuery(ticker, startDate, endDate, freq);

        assertEquals(query1.hashCode(), query2.hashCode());
    }

    @Test
    public void testHashCodeNotEquals() {
        // Test data
        String ticker1 = "aapl";
        String ticker2 = "goog";
        String startDate = "10/01/2019";
        String endDate = "10/01/2020";
        StockTrading.ResampleFreq freq = StockTrading.ResampleFreq.DAILY;
        StockTrading.HistoricalPriceQuery query1 = new StockTrading.HistoricalPriceQuery(ticker1, startDate, endDate, freq);
        StockTrading.HistoricalPriceQuery query2 = new StockTrading.HistoricalPriceQuery(ticker2, startDate, endDate, freq);

        assertNotEquals(query1.hashCode(), query2.hashCode());
    }

    @Test
    public void testGetStockMetadata() throws UnsupportedEncodingException {
        String result = st.getStockMetadata("goog");
        assertNotNull(result);
        assertNotEquals(result, "");
    }

    @Test
    public void testGetStockMetadataCacheHit() throws UnsupportedEncodingException {
        st.getStockMetadata("goog");
        int cacheHits = StockTrading.cacheHits;
        st.getStockMetadata("goog");
        assertEquals(cacheHits + 1, StockTrading.cacheHits);
    }
}