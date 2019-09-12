package com.terheyden;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class User implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private String name;
    private int age;
    private String email;

    public User(String name, int age)
    {
        this.id = UUID.randomUUID();
        this.name = name;
        this.age = age;
        this.email = name + "@email.com";
    }

    @Nonnull
    public UUID getId()
    {
        return id;
    }

    @Nonnull
    public String getName()
    {
        return name;
    }

    @Nonnull
    public User setName(String name)
    {
        this.name = name;
        return this;
    }

    public int getAge()
    {
        return age;
    }

    @Nonnull
    public User setAge(int age)
    {
        this.age = age;
        return this;
    }

    public String getEmail()
    {
        return email;
    }

    public User setEmail(String email)
    {
        this.email = email;
        return this;
    }

    @Override
    public String toString()
    {
        return String.format("%s = %s, age %d", id, name, age);
    }

    @Override
    public boolean equals(Object obj)
    {

        if (this == obj)
        {
            return true;
        }

        if (obj == null || !getClass().equals(obj.getClass()))
        {
            return false;
        }

        User user = (User) obj;

        return age == user.age &&
            id.equals(user.id) &&
            name.equals(user.name) &&
            email.equals(user.email);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, name, age, email);
    }
}
