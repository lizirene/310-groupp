package csci310.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class UserLockTest extends Mockito {

    @Spy
    private final UserLock lock = new UserLock(false);

    Instant fromDateString(String date) {
        return LocalDate.parse(date).atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    @Test
    public void testGetCurrentTime() {
         Instant now = Instant.now();
         assertTrue(now.compareTo(lock.getCurrentTime()) <= 0);
    }

    @Test
    public void testRecord() {
        Instant mockInstant = fromDateString("2020-10-01");
        Mockito.doReturn(mockInstant).when(lock).getCurrentTime();

        lock.record();
        assertTrue(true);
    }

    @Test
    public void testRecordThreeCallsLessThan60Seconds() {
        // Mock duration of 10 seconds between each method call
        Mockito.doAnswer(new Answer() {
            private Instant timestamp = fromDateString("2020-10-01");

            @Override
            public Object answer(InvocationOnMock invocation) {
                // Add 10 seconds to timestamp
                timestamp = timestamp.plus(10, ChronoUnit.SECONDS);
                return timestamp;
            }
        }).when(lock).getCurrentTime();

        // Call methods 3 times
        lock.record();
        lock.record();
        lock.record();

        // Verify
        assertTrue(lock.locked);
    }

    @Test
    public void testRecordThreeCalssMoreThan60Seconds() {
        // Mock duration of 21 seconds between each method call
        Mockito.doAnswer(new Answer() {
            private Instant timestamp = fromDateString("2020-10-01");

            @Override
            public Object answer(InvocationOnMock invocation) {
                // Add 10 seconds to timestamp
                timestamp = timestamp.plus(21, ChronoUnit.SECONDS);
                return timestamp;
            }
        }).when(lock).getCurrentTime();

        // Call methods multiple times
        lock.record();
        lock.record();
        lock.record();
        lock.record();
        lock.record();
        lock.record();

        // Verify
        assertFalse(lock.locked);
    }

    @Test
    public void testIsLocked() {
        // Mock duration of 11 seconds between each method call
        Mockito.doAnswer(new Answer() {
            private Instant timestamp = fromDateString("2020-10-01");

            @Override
            public Object answer(InvocationOnMock invocation) {
                // Add 10 seconds to timestamp
                timestamp = timestamp.plus(11, ChronoUnit.SECONDS);
                return timestamp;
            }
        }).when(lock).getCurrentTime();

        // Lock the lock
        lock.record();
        lock.record();
        lock.record();

        // Check isLocked
        assertTrue(lock.isLocked());

        // Make 6 more calls so that a minute has passed
        // and check that the lock has been unlocked
        lock.record();
        lock.record();
        lock.record();
        lock.record();
        lock.record();
        lock.record();

        assertFalse(lock.isLocked());
    }

    @Test
    public void testClear() {
        // Mock duration of 11 seconds between each method call
        Mockito.doAnswer(new Answer() {
            private Instant timestamp = fromDateString("2020-10-01");

            @Override
            public Object answer(InvocationOnMock invocation) {
                // Add 10 seconds to timestamp
                timestamp = timestamp.plus(11, ChronoUnit.SECONDS);
                return timestamp;
            }
        }).when(lock).getCurrentTime();

        // Lock the lock
        lock.record();
        lock.record();
        lock.record();

        // Make 6 more calls so that a minute has passed
        // and check that the lock has been unlocked
        lock.record();
        lock.record();
        lock.record();
        lock.record();
        lock.record();
        lock.record();

        // Check that timestamps are not empty
        assertTrue(lock.timestamps.size() > 0);

        // Call method
        lock.clear();
        assertEquals(0, lock.timestamps.size());
    }
}
