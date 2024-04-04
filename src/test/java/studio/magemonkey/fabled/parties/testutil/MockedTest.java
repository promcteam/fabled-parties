package studio.magemonkey.fabled.parties.testutil;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import studio.magemonkey.fabled.parties.FabledParties;
import studio.magemonkey.fabled.parties.Party;
import studio.magemonkey.fabled.parties.inject.Server;
import studio.magemonkey.fabled.api.classes.FabledClass;
import studio.magemonkey.fabled.api.enums.ExpSource;
import studio.magemonkey.fabled.api.player.PlayerClass;
import studio.magemonkey.fabled.api.player.PlayerData;
import studio.magemonkey.codex.mccore.commands.CommandManager;
import lombok.extern.log4j.Log4j2;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.*;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Log4j2
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class MockedTest {
    protected ServerMock            server;
    protected FabledParties         plugin;
    protected Party                 party;
    protected List<PlayerMock>      players          = new ArrayList<>();
    protected Map<UUID, PlayerData> activePlayerData = new HashMap<>();
    MockedStatic<Server> mockedServerStatic;

    @BeforeAll
    public void setupServer() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(FabledParties.class);
        mockedServerStatic = Mockito.mockStatic(Server.class);
        mockedServerStatic.when(() -> Server.getLevel(any(UUID.class)))
                .thenReturn(5);
        mockedServerStatic.when(() -> Server.getPlayerData(any(Player.class)))
                .thenAnswer(a -> generatePlayerData(a.getArgument(0)));
        mockedServerStatic.when(() -> Server.getClass(any(Player.class)))
                .thenAnswer(a -> {
                    Player      player    = a.getArgument(0);
                    PlayerClass classMock = mock(PlayerClass.class);
                    when(classMock.getData())
                            .thenAnswer(b -> {
                                FabledClass fabledClass = mock(FabledClass.class);
                                when(fabledClass.receivesExp(any(ExpSource.class))).thenReturn(true);

                                return fabledClass;
                            });
                    when(classMock.getPlayerData())
                            .thenAnswer((b) -> activePlayerData.containsKey(player.getUniqueId())
                                    ? activePlayerData.get(player.getUniqueId())
                                    : generatePlayerData(player));

                    return classMock;
                });
        assertEquals(5, Server.getLevel(UUID.randomUUID()));

//        MockBukkit.loadJar(DependencyResolver.resolve("studio.magemonkey:promccore:1.0.3.10-SNAPSHOT"));
//        MockBukkit.loadJar(DependencyResolver.resolve("studio.magemonkey:proskillapi:1.1.7.18-SNAPSHOT"));
    }

    @AfterAll
    public void destroy() {
        CommandManager.unregisterAll();
        mockedServerStatic.close();
        MockBukkit.unmock();
    }

    @AfterEach
    public void clearData() {
        activePlayerData.clear();
        clearEvents();
        players.clear();
    }

    public PlayerData generatePlayerData(Player player) {
        PlayerData pd = mock(PlayerData.class);
        activePlayerData.put(player.getUniqueId(), pd);

        when(pd.getPlayer()).thenReturn(player);
        return pd;
    }

    public PlayerMock genPlayer(String name) {
        return genPlayer(name, true);
    }

    public PlayerMock genPlayer(String name, boolean op) {
//        PlayerMock pm = server.addPlayer(name);
        PlayerMock pm = new PlayerMock(server, name, UUID.randomUUID());
        server.addPlayer(pm);
        players.add(pm);
        pm.setOp(op);

        return pm;
    }

    public <T extends Event> void assertEventFired(Class<T> clazz) {
        server.getPluginManager().assertEventFired(clazz);
    }

    public <T extends Event> void assertEventFired(Class<T> clazz, Predicate<T> predicate) {
        server.getPluginManager().assertEventFired(clazz, predicate);
    }

    public void clearEvents() {
        server.getPluginManager().clearEvents();
    }
}
