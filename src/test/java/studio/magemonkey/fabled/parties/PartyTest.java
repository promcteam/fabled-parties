package studio.magemonkey.fabled.parties;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import studio.magemonkey.fabled.parties.testutil.MockedTest;
import studio.magemonkey.fabled.parties.event.PartyExpEvent;
import studio.magemonkey.fabled.parties.event.PlayerJoinPartyEvent;
import studio.magemonkey.fabled.parties.event.PlayerLeavePartyEvent;
import studio.magemonkey.fabled.api.enums.ExpSource;
import studio.magemonkey.fabled.api.player.PlayerData;
import lombok.extern.log4j.Log4j2;
import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.verify;

@Log4j2
public class PartyTest extends MockedTest {


    private PlayerMock partyLeader, partyMember;

    @BeforeEach
    public void setup() {
        partyLeader = genPlayer("Travja");
        partyMember = genPlayer("goflish");
        party = new Party(plugin, partyLeader);
        party.addMember(partyMember);

        plugin.addParty(party);
    }

    @AfterEach
    public void tearDown() {
        plugin.removeParty(party);
        server.setPlayers(0);
    }

    @Test
    public void testPartyLeaderLeavingNewLeaderAssigned() {
        partyLeader.performCommand("pt leave");
        partyLeader.assertSaid(ChatColor.translateAlternateColorCodes('&',
                "&6" + partyLeader.getName() + " &2has left the party&r"));
        Assertions.assertEquals(1, party.getPartySize());
        Assertions.assertNotEquals(partyLeader.getName(), party.getLeader().getName());
        log.info("Party ownership is properly reassigned");
    }

    @Test
    public void removeOnQuit() {
        plugin.getConfig().set("remove-on-dc", true);
        plugin.saveConfig();
        plugin.loadConfiguration();

        partyLeader.disconnect();

        assertEventFired(PlayerQuitEvent.class);
        Assertions.assertEquals(1, party.getPartySize());
        Assertions.assertNotEquals(partyLeader.getName(), party.getLeader().getName());
        log.info("Party adjusts properly on disconnect");
    }

    @Test
    public void getLeader() {
        Assertions.assertEquals(partyLeader.getName(), party.getLeader().getName());
    }

    @Test
    public void acceptAddsPlayer() {
        PlayerMock player = genPlayer("Joe");
        party.invite(player);
        party.accept(player);

        assertEventFired(PlayerJoinPartyEvent.class);
        Assertions.assertEquals(3, party.getPartySize());
    }

    @Test
    public void removeRemovesPlayer() {
        party.removeMember(partyMember);

        assertEventFired(PlayerLeavePartyEvent.class);
        Assertions.assertEquals(1, party.getPartySize());
    }

    @Test
    public void giveExp() {
        party.giveExp(partyLeader, 100, ExpSource.MOB);
        PlayerData leaderData = activePlayerData.get(partyLeader.getUniqueId());
        PlayerData memberData = activePlayerData.get(partyMember.getUniqueId());

        assertEventFired(PartyExpEvent.class, e -> {
            verify(leaderData).giveExp(e.getAmount(), e.getExpSource());
            verify(memberData).giveExp(e.getAmount(), e.getExpSource());

            return e.getAmount() != 0;
        });
    }

    @Test
    public void giveExpSoloGetsAll() {
        party.removeMember(partyMember);
        party.giveExp(partyLeader, 100, ExpSource.MOB);
        PlayerData leaderData = activePlayerData.get(partyLeader.getUniqueId());

        assertEventFired(PartyExpEvent.class, e -> {
            verify(leaderData).giveExp(100, e.getExpSource());

            return e.getAmount() != 0;
        });
    }
}