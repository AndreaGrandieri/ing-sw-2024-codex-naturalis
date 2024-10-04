package it.polimi.ingsw.util;

public class TextValidator {
    // Used for server-side validation of username, both Socket and RMI
    public final static String usernameValidator = "^(?!(.*[_-]){2})[a-zA-Z0-9_-]{3,10}$";

    // Used in GUI for client-side validation of username
    public final static String usernameWritingValidator = "^(?!(.*[_-]){2})[a-zA-Z0-9_-]{0,10}$";

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Used for server-side validation of lobby name, both Socket and RMI
    public final static String lobbyNameValidator = "^(?!(.*[_-]){2})[a-zA-Z0-9_-]{3,10}$";

    // Used in GUI for client-side validation of lobby name
    public final static String lobbyNameWritingValidator = "^(?!(.*[_\\- ]){2})[a-zA-Z0-9_ \\-]{0,20}$";

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Used for server-side validation of lobby max number of players, both Socket and RMI
    public final static String lobbyPlayersNumValidator = "^[2-4]$";

    // Used in GUI for client-side validation of lobby max number of players
    public final static String lobbyPlayersNumWritingValidator = "^(?:[2-4]|)$";

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Used for server-side validation of chat messages, both Socket and RMI
    // Used in CLI for client-side validation of chat messages
    /*
    This regular expression works as follows:
    ^ asserts the start of the line.
    \\p{ASCII} matches any ASCII character.
    {1,1024} specifies that the length of the string should be between 1 and 1024 characters.
    $ asserts the end of the line.
     */
    public final static String chatMessageValidator = "^\\p{ASCII}{1,1024}$";
}
