package studio.magemonkey.fabled.parties;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import studio.magemonkey.fabled.parties.testutil.MockedTest;
import studio.magemonkey.fabled.parties.inject.Server;
import studio.magemonkey.fabled.api.enums.ExpSource;
import studio.magemonkey.fabled.api.event.PlayerExperienceGainEvent;
import studio.magemonkey.fabled.api.player.PlayerData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class EventsTest extends MockedTest {
    private PlayerMock partyLeader, partyMember;

    @BeforeEach
    public void setup() {
        partyLeader = genPlayer("Travja");
        partyMember = genPlayer("goflish");
        party = spy(new Party(plugin, partyLeader));
        party.addMember(partyMember);

        plugin.addParty(party);
        reset(party);
    }

    @AfterEach
    public void tearDown() {
        plugin.removeParty(party);
        server.setPlayers(0);
    }

    @Test
    public void experienceGainEventIsShared() {
        new PlayerExperienceGainEvent(
                Server.getClass(partyLeader), 80, ExpSource.EXP_BOTTLE
        ).callEvent();
        PlayerData pd  = activePlayerData.get(partyLeader.getUniqueId());
        PlayerData pd2 = activePlayerData.get(partyMember.getUniqueId());

        verify(party, times(1))
                .giveExp(partyLeader, 80, ExpSource.EXP_BOTTLE);
        verify(pd, times(1)).giveExp(anyDouble(), any(ExpSource.class));
        verify(pd2, times(1)).giveExp(anyDouble(), any(ExpSource.class));
    }

}
