package it.polimi.ingsw.client.network.state;

import it.polimi.ingsw.client.network.liveloop.UserOfClient;

/**
 * This class represents the ChooseCreateJoinState of the User in the Server.
 * The User is in this State when it is choosing whether to create or join a Lobby.
 */
public class ChooseCreateJoinStateOfClient extends StateOfClient {
    public ChooseCreateJoinStateOfClient(UserOfClient userOfClient) {
        super(userOfClient);
    }
}
