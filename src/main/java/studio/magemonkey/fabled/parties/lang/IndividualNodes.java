package studio.magemonkey.fabled.parties.lang;

/**
 * Configuration keys for the language config,
 * specifically the Individual section of it
 */
public class IndividualNodes {

    public static final String

            /************************
             Base Configuration Key
             ************************/

            BASE = "Individual.",

    /***********************
     Individual Value Keys
     ***********************/

    CHAT_ON               = BASE + "chat-on",
            CHAT_OFF      = BASE + "chat-off",
            PARTY_LEADER  = BASE + "party-leader",
            DECLINED      = BASE + "declined",
            NO_RESPONSE   = BASE + "no-response",
            INVITED       = BASE + "invited",
            PLAYER_KICKED = BASE + "player-kicked",
            KICKED        = BASE + "kicked",
            INFO          = BASE + "info";

}
