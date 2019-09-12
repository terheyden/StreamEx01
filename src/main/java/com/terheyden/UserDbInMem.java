package com.terheyden;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class UserDbInMem implements UserDb
{
    private final Map<UUID, User> users = new HashMap<>();

    @Override
    public void saveUser(User user)
    {
        users.put(user.getId(), user);
    }

    @Nonnull
    @Override
    public Optional<User> loadUser(UUID id)
    {
        return Optional.ofNullable(users.get(id));
    }

    /**
     * For internal / tests.
     */
    Map<UUID, User> getUsers()
    {
        return users;
    }
}
