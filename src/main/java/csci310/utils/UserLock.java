package csci310.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

// User login lock
public class UserLock {
    boolean locked;		// If the account is locked;
    final List<Instant> timestamps;	// Queue of attempt timestamps
    final static int MAX_ATTEMPT = 3;
    final static long TIME = 60;

    // Wrap static method to enable stubbing without using PowerMockito
    public Instant getCurrentTime() {
        return Instant.now();
    }

    /**
     * construct a userLock 
     * @param {boolean} toRecord
     */
    public UserLock(boolean toRecord) {
        this.locked = false;
        this.timestamps = new LinkedList<>();
        if (toRecord) {
            this.record();
        }
    }

    /**
     * record the attempt and lock if reaches maximum attempts
     */
    public void record() {
        if (isLocked()) {
            return;
        }
        // Insert current timestamp
        Instant now = getCurrentTime();
        timestamps.add(now);
        // Remove any timestamp that is older than 1 minutes ago
        timestamps.removeIf(t -> Duration.between(t, now).getSeconds() > TIME);
        // If reaches max number of attempts, then locks
        if (timestamps.size() >= MAX_ATTEMPT) {
            locked = true;
        }
    }

    /**
     * update lock status
     * @return Boolean indicating if the lock is locked or not
     */
    public boolean isLocked() {
        // Update lock status
        locked = locked &&
                Duration.between(timestamps.get(timestamps.size() - 1), getCurrentTime()).getSeconds() <= TIME;
        return locked;
    }

    /**
     * reset the lock
     */
    public void clear() {
        locked = false;
        timestamps.clear();
    }
}
