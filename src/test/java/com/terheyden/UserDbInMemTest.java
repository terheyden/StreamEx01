package com.terheyden;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDbInMemTest
{

    private UserDbInMem userDb;
    private User user1;

    @BeforeEach
    void setUp()
    {
        user1 = new User("Cora", 8);
        userDb = new UserDbInMem();
    }

    @Test
    void saveUser()
    {
        userDb.saveUser(user1);

        assertEquals(1, userDb.getUsers().size());
        assertEquals(user1, userDb.getUsers().get(user1.getId()));
    }

    @Test
    void loadUser()
    {
        userDb.saveUser(user1);
        Optional<User> user2 = userDb.loadUser(user1.getId());

        assertTrue(user2.isPresent());
        assertEquals(user1, user2.get());
    }
}
