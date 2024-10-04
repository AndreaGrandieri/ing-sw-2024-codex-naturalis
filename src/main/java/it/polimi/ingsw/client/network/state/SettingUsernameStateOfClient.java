package it.polimi.ingsw.client.network.state;

import it.polimi.ingsw.client.network.liveloop.UserOfClient;

/**
 * This class represents the SettingUsernameState of the User.
 * The User is in this State when it is choosing its username.
 */
public class SettingUsernameStateOfClient extends StateOfClient {
    public SettingUsernameStateOfClient(UserOfClient userOfClient) {
        super(userOfClient);
    }
}
