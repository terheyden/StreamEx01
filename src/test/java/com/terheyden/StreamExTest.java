package com.terheyden;

import static io.vavr.control.Try.run;
import static java.lang.String.format;
import static java.lang.System.out;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

/**
 * https://github.com/amaembo/streamex
 * https://github.com/amaembo/streamex/blob/master/CHEATSHEET.md
 */
@ParametersAreNonnullByDefault
public class StreamExTest
{
    private static final Logger LOG = getLogger(StreamExTest.class);

    @Test
    public void test() throws IOException
    {
        User user1 = new User("Cora", 8);
        User user2 = new User("Mika", 12);
        User user3 = new User("Tashi", 11);

        List<User> users = Arrays.asList(user1, user2, user3);

        ////////////////////////////////////////
        // CREATING STREAMS:

        // Stream items along with their index numbers:

        EntryStream
            .of(users)
            .mapKeyValue((num, user) -> format("User %d is: %s", num, user))
            .toList();

        // For loop:

        StreamEx
            .iterate(0, n -> n < 3, n -> n + 1)
            .map(users::get);

        // Produce items while condition is true:

        AtomicInteger counter = new AtomicInteger(0);

        Set<User> producedUsers = StreamEx.<User>produce(stream ->
        {
            if (counter.get() >= users.size())
            {
                return false;
            }

            stream.accept(users.get(counter.getAndIncrement()));
            return true;
        })
            .toImmutableSet();

        assertEquals(3, producedUsers.size());

        // Emit items in a custom way:

        StreamEx.Emitter<User> emitUsers = listener ->
        {
            for (User user : users)
            {
                // Keep giving the consumer items.
                listener.accept(user);
            }

            // Return null when done.
            return null;
        };

        List<User> emittedUsers = emitUsers.stream().toList();
        assertEquals(3, emittedUsers.size());

        // StreamEx.ofLines  = stream a file
        // StreamEx.ofKeys   = stream a map's keys
        // StreamEx.ofValues = stream a map's values
        // EntryStream.of    = stream a map

        ////////////////////////////////////////
        // COLLECT:

        // Collect to list, set, sorted, immutable, etc:

        List<User> javaUsers = users
            .stream()
            .collect(Collectors.toList());

        List<User> exUsers = StreamEx
            .of(users)
            .toList();

        assertEquals(3, javaUsers.size());
        assertEquals(3, exUsers.size());

        // Collect (drain) into an existing list:

        List<User> anotherList = new ArrayList<>();

        StreamEx
            .of(users)
            .into(anotherList);

        assertEquals(3, anotherList.size());

        ////////////////////////////////////////
        // JOIN STRINGS:

        String javaJoin = users
            .stream()
            .map(User::getName)
            .collect(Collectors.joining("; "));

        String exJoin = StreamEx
            .of(users)
            .map(User::getName)
            .joining("; ");

        assertEquals(javaJoin, exJoin);

        ////////////////////////////////////////
        // JOIN STREAMS:

        Set<String> javaStreams = Stream.concat(
            Stream.of("first"),
            Stream.concat(
                users.stream()
                    .map(User::getName),
                Stream.of("last")))
            .collect(Collectors.toSet());

        Set<String> exStreams = StreamEx.of(users)
            .map(User::getName)
            .prepend("first")
            .append("last")
            .toSet();

        assertEquals(5, javaStreams.size());
        assertEquals(5, exStreams.size());

        ////////////////////////////////////////
        // ZIP:

        // Let's say we have some results.
        List<String> results = Arrays.asList("PASS", "FAIL", "UNKNOWN");

        // We can do a 1:1 mapping with arrays or lists:

        List<String> resultStrings = StreamEx
            .zip(
                users,
                results,
                (user, result) -> format("User %s = %s", user.getName(), result))
            .toList();

        // We can also zip streams:

        StreamEx
            .of(users)
            .zipWith(results.stream())
            .toMap();

        ////////////////////////////////////////
        // NULLS AND BLANKS:

        users
            .stream()
            .filter(u -> isNotBlank(u.getEmail()));

        StreamEx
            .of(users)
            .nonNull()
            .filter(u -> isNotBlank(u.getEmail()));

        ////////////////////////////////////////
        // MAPS:

        // Grouping by:

        Map<Integer, List<User>> javaGroup = users
            .stream()
            .collect(Collectors.groupingBy(User::getAge));

        Map<Integer, List<User>> exGroup = StreamEx
            .of(users)
            .groupingBy(User::getAge);

        assertEquals(3, javaGroup.size());
        assertEquals(3, exGroup.size());

        Map<String, User> map = Map.of("Cora", user1, "Mika", user2, "Tashi", user3);

        // Convert map values, and collect sorted:

        // https://stackoverflow.com/questions/31004899/java-8-collectors-tomap-sortedmap
        SortedMap<String, String> javaMapValues = map
            .entrySet()
            .stream()
            .map(e -> Map.entry(e.getKey(), e.getValue().getEmail()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (v1, v2) -> { throw new RuntimeException(format("Duplicate key for values %s and %s", v1, v2)); },
                TreeMap::new));

        SortedMap<String, String> exMapValues = EntryStream
            .of(map)
            .mapValues(User::getEmail)
            .toSortedMap();

        // Convert a collection to a map:

        // https://stackoverflow.com/questions/28032827/java-8-lambdas-function-identity-or-t-t
        Map<String, User> javaMapEntry = users
            .stream()
            .collect(Collectors.toMap(
                User::getName,
                Function.identity()));

        Map<String, User> exMapEntry = StreamEx
            .of(users)
            .mapToEntry(User::getName, Function.identity())
            .toImmutableMap();

        // Can drain into an existing map.

        Map<String, String> map2 = new HashMap<>();

        StreamEx
            .of(users)
            .mapToEntry(User::getName, User::getEmail)
            .into(map2);

        ////////////////////////////////////////
        // FILTER AND REMOVE:

        // Filter decides what items stay in the collection.
        StreamEx
            .of(users)
            .filter(u -> u.getEmail() != null);

        // Remove is the opposite - it decides what to remove.
        StreamEx
            .of(users)
            .remove(u -> u.getEmail() == null);

        // filterBy and removeBy do: "remove if value == X".

        // Keep users who are 12.
        StreamEx
            .of(users)
            .filterBy(User::getAge, 12);

        // Remove users who are 8.
        StreamEx
            .of(users)
            .removeBy(User::getAge, 8)
            .toList();

        ////////////////////////////////////////
        // FIND:

        // Find the index num - returns an Optional.
        int coraNum = Math.toIntExact(StreamEx
            .of(users)
            .indexOf(u -> u.getName().equals("Cora"))
            .getAsLong());

        assertEquals("Cora", users.get(coraNum).getName());

        ////////////////////////////////////////
        // PARALLEL:

        ForkJoinPool ioPool = new ForkJoinPool(10);

        StreamEx
            .of(users)
            .parallel(ioPool)
            .map(User::getEmail);

        ////////////////////////////////////////
        // MULTI-THREADING:

        StreamEx
            .of(users)
            .map(StreamExTest::sleepWith)
            .forEach(StreamExTest::logUser);


        ////////////////////////////////////////
        // CHAINING STREAMS:

        Map<Boolean, User> sendEmailResults = StreamEx
            .of(users)
            .chain(StreamExTest::checkPrefs)
            .chain(StreamExTest::loadEmails)
            .chain(StreamExTest::sendEmails)
            .toMap();
    }

    /**
     * Load user emails.
     */
    private static StreamEx<User> loadEmails(StreamEx<User> users)
    {
        return users.filter(u -> isBlank(u.getEmail()));
    }

    /**
     * Check prefs and filter users that don't want emails.
     */
    private static StreamEx<User> checkPrefs(StreamEx<User> users)
    {
        return users.select(User.class);
    }

    /**
     * Try to send emails. Returns entries where the key is true or false
     * indicating success or fail.
     */
    private static EntryStream<Boolean, User> sendEmails(StreamEx<User> users)
    {
        return users
            .mapToEntry(
                u -> u.getAge() % 2 == 0,
                Function.identity());
    }

    @Test
    public void testing()
    {
        User user1 = new User("Cora", 8);
        User user2 = new User("Mika", 12);
        User user3 = new User("Tashi", 11);

        List<User> users = Arrays.asList(user1, user2, user3);

        StreamEx
            .of(users)
            .parallel()
            .map(StreamExTest::sleepWith)
            .forEach(u -> out.println(u.getName()));
    }

    private static User sleepWith(User u)
    {
        LOG.info("I sleep user: {}", u.getName());
        sleep(5000);
        return u;
    }

    private static void logUser(User u)
    {
        LOG.info("I log user: {}", u.getName());
        sleep(5000);
    }



    /**
     * Unchecked version of {@link Thread#sleep(long)}.
     */
    private static void sleep(int ms)
    {
        run(() -> Thread.sleep(ms));
    }
}
