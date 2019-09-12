package com.terheyden;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface UserDb
{
    void saveUser(User user);

    @Nonnull
    Optional<User> loadUser(UUID id);
}
