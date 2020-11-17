package csci310.trading;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class StockEndpointTest extends Mockito {

    @Test
    public void testParseOriginal() {
        StockEndpointOriginal testOriginal = new StockEndpointOriginal();
        testOriginal.date = "2020-10-01";
        testOriginal.close = 32;
        testOriginal.high = 50;

        StockEndpoint parsed = StockEndpoint.parseOriginal(testOriginal);
        assertEquals(parsed.date, testOriginal.date);
        assertEquals(parsed.close, testOriginal.close, 0.0);
    }

    @Test
    public void testParseOriginalList() {
        List<StockEndpointOriginal> testOriginal = new ArrayList<>();
        testOriginal.add(new StockEndpointOriginal());
        testOriginal.add(new StockEndpointOriginal());

        testOriginal.get(0).date = "2020-10-01";
        testOriginal.get(0).close = 32;
        testOriginal.get(1).date = "2020-10-02";
        testOriginal.get(1).close = 64;

        List<StockEndpoint> result = StockEndpoint.parseOriginalList(testOriginal);

        for (int i = 0; i < testOriginal.size(); i++) {
            assertEquals(result.get(i).date, testOriginal.get(i).date);
            assertEquals(result.get(i).close, testOriginal.get(i).close, 0.0);
        }
    }
}