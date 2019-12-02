package de.comroid.candybot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import de.comroid.util.Util;
import de.comroid.util.files.FileProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.logging.ExceptionLogger;

import static java.nio.charset.StandardCharsets.UTF_8;
import static de.comroid.util.Util.nodeOf;

public enum CandyBank {
    INSTANCE;

    private final File storageFile = FileProvider.getFile("scores.json");
    private final Map<Server, Map<Integer, List<User>>> scoreMap = new ConcurrentHashMap<>();
    private DiscordApi api;

    public synchronized int increment(Server server, User user) {
        int[] newScore = new int[]{-1};
        scoreMap.compute(server, (srv, subMap) -> {
            if (subMap == null) subMap = new ConcurrentHashMap<>();

            final Map<Integer, List<User>> finalSubMap = subMap;
            for (Map.Entry<Integer, List<User>> entry : subMap.entrySet()) {
                Integer score = entry.getKey();
                List<User> users = entry.getValue();
                if (users.contains(user)) {
                    users.remove(user);
                    finalSubMap.compute((newScore[0] = score + 1), (scr, usrs) -> {
                        if (usrs == null) usrs = new ArrayList<>();

                        usrs.add(user);

                        return usrs;
                    });
                    if (users.size() == 0) subMap.remove(score, users);
                    break;
                }
            }

            if (newScore[0] == -1) {
                subMap.computeIfAbsent(1, scr -> new ArrayList<>())
                        .add(user);
                newScore[0] = 1;
            }

            return subMap;
        });

        return newScore[0];
    }

    public int getScore(Server server, User user) {
        if (scoreMap.containsKey(server)) {
            Map<Integer, List<User>> subMap = scoreMap.get(server);

            for (Map.Entry<Integer, List<User>> entry : subMap.entrySet()) {
                if (entry.getValue().contains(user))
                    return entry.getKey();
            }
        }

        return 0;
    }

    public Map<Integer, List<User>> getBest(Server server) {
        return scoreMap.compute(server, (srv, subMap) -> {
            if (subMap == null) return new HashMap<>();

            TreeMap<Integer, List<User>> treeMap = new TreeMap<>((one, two) -> Integer.compare(two, one));
            treeMap.putAll(subMap);
            return treeMap;
        });
    }

    public void init(DiscordApi api) throws IOException {
        if (!storageFile.exists()) storageFile.createNewFile();

        this.api = api;
        readData();
    }

    public void terminate() throws IOException {
        storeData();
    }

    public void storeData() throws IOException {
        if (!storageFile.exists()) storageFile.createNewFile();
        else if (storageFile.delete()) storageFile.createNewFile();

        ArrayNode data = Util.arrayNode();

        scoreMap.forEach((server, userMap) -> {
            ObjectNode serverNode = data.addObject();

            serverNode.set("id", nodeOf(server.getId()));
            ArrayNode scoresNode = serverNode.putArray("scores");

            userMap.forEach((score, userList) -> {
                ObjectNode scoreNode = scoresNode.addObject();

                scoreNode.set("score", nodeOf(score));
                ArrayNode usersNode = scoreNode.putArray("users");

                userList.forEach(user -> usersNode.add(user.getId()));
            });
        });

        FileOutputStream stream = new FileOutputStream(storageFile);
        stream.write(data.toString().getBytes(UTF_8));
        stream.close();
    }

    private void readData() throws IOException {
        JsonNode data = new ObjectMapper().readTree(new FileInputStream(storageFile));

        if (data != null && data.size() != 0) {
            for (JsonNode serverNode : data) {
                Server server = api.getServerById(serverNode.get("id").asLong()).orElse(null);

                if (server == null) continue;

                for (JsonNode scoresNode : serverNode.get("scores")) {
                    int score = scoresNode.get("score").asInt();

                    for (JsonNode usersNode : scoresNode.get("users")) {
                        User user = api.getUserById(usersNode.asLong()).exceptionally(ExceptionLogger.get()).join();

                        if (user == null) continue;

                        scoreMap.compute(server, (srv, subMap) -> {
                            if (subMap == null) subMap = new ConcurrentHashMap<>();

                            subMap.compute(score, (scr, usrs) -> {
                                if (usrs == null) usrs = new ArrayList<>();

                                usrs.add(user);

                                return usrs;
                            });

                            return subMap;
                        });
                    }
                }
            }
        }
    }
}
