package it.polimi.ingsw.client.network;

import it.polimi.ingsw.client.network.liveloop.UserOfClient;

/**
 * Represents the profile of the client.
 * It serves as a single point of access to the UserOfClient object.
 * Is a singleton.
 */
public class ProfileOfClient {
    private static ProfileOfClient instance;

    private ProfileOfClient(UserOfClient userOfClient) {
        this.userOfClient = userOfClient;
    }

        public static ProfileOfClient restartSingletonAndGetInstance(UserOfClient userOfClient) {
        instance = new ProfileOfClient(userOfClient);
        return instance;
    }

        public static ProfileOfClient getInstance() throws ProfileOfClientException {
        if (instance == null) {
            throw new ProfileOfClientException("ProfileOfClient has not been initialized yet.",
                    ProfileOfClientException.Reason.PROFILE_OF_CLIENT_NOT_INITIALIZED);
        }
        return instance;
    }

    /* Start of the class implementation */
    private final UserOfClient userOfClient;

        public UserOfClient getUserOfClient() {
        return userOfClient;
    }
}
