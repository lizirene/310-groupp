package csci310.model;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserTest extends Mockito {

    @Test
    public void testEqualPassword() {
        String username = "test_name";
        String password = "test_pwd";
        User user = new User(username, password);
        // Hash password
        user.setPasswordHash();
        assertTrue(user.equalPassword(password));
        assertFalse(user.equalPassword("test_pwd_1"));
    }
}