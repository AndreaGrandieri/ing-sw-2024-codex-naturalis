package it.polimi.ingsw.network.state;

import it.polimi.ingsw.logger.Logger;
import it.polimi.ingsw.network.Profiles;
import it.polimi.ingsw.network.ProfilesException;
import it.polimi.ingsw.network.liveloop.User;
import it.polimi.ingsw.network.liveloop.UserException;
import it.polimi.ingsw.network.messages.clienttoserver.UsernameMessage;
import it.polimi.ingsw.network.messages.servertoclient.UnknownErrorMessage;
import it.polimi.ingsw.network.messages.servertoclient.UsernameAlreadyTakenMessage;
import it.polimi.ingsw.network.messages.servertoclient.UsernameConfirmedMessage;
import it.polimi.ingsw.network.messages.servertoclient.UsernameNotValidMessage;

/**
 * This class represents the SettingUsernameState of the User in the Server.
 * The User is in this State when it is choosing its username.
 */
public class SettingUsernameState extends State {
    public SettingUsernameState(User user) {
        super(user, StateType.SETTINGUSERNAME);

        Logger.logInfo("User " + user.getConnectionUUID().substring(0, 3) + " is in the SettingUsernameState.");
    }

    /**
     * Handle the UsernameMessage from the Client.
     * <ul>
     *     <li>
     *          If the username is not taken and is valid, the User is set the username and moved to the ChooseCreateJoinState.
     *          A UsernameConfirmedMessage is sent to the Client.
     *     </li>
     *
     *     <li>
     *         If the username is already taken, a UsernameAlreadyTakenMessage is sent to the Client.
     *     </li>
     *
     *     <li>
     *         If the username is not valid, a UsernameNotValidMessage is sent to the Client.
     *     </li>
     *
     *     <li>
     *          If an unexpected error occurs, an UnknownErrorMessage is sent to the Client.
     *     </li>
     * </ul>
     * @param message the UsernameMessage received from the Client.
     */
    @Override
    public void onUsernameMessage(UsernameMessage message) {
        if (!Profiles.getInstance().isUsernameTaken(message.getUsername())) {
            try {
                Profiles.getInstance().setUserUsername(this.user.getConnectionUUID(), message.getUsername());

                this.user.send(new UsernameConfirmedMessage());
                this.user.setState(new ChooseCreateJoinState(this.user));
            } catch (UserException e) {
                // e.getReason() for UserException is not checked since there is only one possible
                // option
                this.user.send(new UsernameNotValidMessage(e.getMessage()));
            } catch (ProfilesException e) {
                // e.getReason() for ProfilesException is not checked since there is only one possible
                // option.
                this.user.send(new UnknownErrorMessage());
            }
        } else {
            this.user.send(new UsernameAlreadyTakenMessage());
        }
    }
}
