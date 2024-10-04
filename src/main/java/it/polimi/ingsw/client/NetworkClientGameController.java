package it.polimi.ingsw.client;

import it.polimi.ingsw.client.network.liveloop.UserOfClient;
import it.polimi.ingsw.client.network.rmi.DefaultRMIExceptionsHandlerOfClient;
import it.polimi.ingsw.controller.EventHandler;
import it.polimi.ingsw.controller.GameFlow;
import it.polimi.ingsw.controller.event.Event;
import it.polimi.ingsw.controller.event.EventType;
import it.polimi.ingsw.controller.interfaces.ClientGameController;
import it.polimi.ingsw.model.card.GoalCard;
import it.polimi.ingsw.model.card.StarterCard;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.card.properties.CardFace;
import it.polimi.ingsw.model.game.CardType;
import it.polimi.ingsw.model.game.CommonBoard;
import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerColor;
import it.polimi.ingsw.model.player.PlayerData;
import it.polimi.ingsw.model.player.PlayerManuscript;
import it.polimi.ingsw.network.messages.DoubleArgMessage;
import it.polimi.ingsw.network.messages.SingleArgMessage;
import it.polimi.ingsw.network.messages.TripleArgMessage;
import it.polimi.ingsw.network.messages.ZeroArgMessage;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static it.polimi.ingsw.network.messages.MessageType.*;
import static it.polimi.ingsw.network.messages.util.Casting.singleCastAndSend;

public class NetworkClientGameController implements ClientGameController {
    private final String myUsername;
    private final UserOfClient userOfClient;
    private final Map<EventType<? extends Event>, List<EventHandler<? extends Event>>> eventHandlers = new HashMap<>();
    private final Boolean RMI;

    public NetworkClientGameController(String username, UserOfClient user, Boolean RMI) {
        this.myUsername = username;
        this.userOfClient = user;
        this.RMI = RMI;
    }

    @Override
    public String getMyUsername() {
        return myUsername;
    }

        public synchronized StarterCard getStarterCard(String username) {
        if (!this.RMI) {
            try {
                SingleArgMessage<StarterCard> message = singleCastAndSend(userOfClient,
                        new SingleArgMessage<>(GET_STARTER_CARD, username),
                        ANSWER_GET_STARTER_CARD
                );
                return message.get();
            } catch (ClassCastException e) {
                // Got a different message (probably UNKNOWN_ERROR)
                return null;
            }
        } else {
            try {
                return this.userOfClient.getUserStub().getStarterCard(username);
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
    }

    public synchronized boolean setStarterCard(CardFace face) {
        if (!this.RMI) {
            try {
                SingleArgMessage<Boolean> message = singleCastAndSend(userOfClient,
                        new DoubleArgMessage<>(SET_STARTER_CARD,
                                myUsername, face), ANSWER_SET_STARTER_CARD
                );
                return message.get();
            } catch (ClassCastException e) {
                return false;
            }
        } else {
            try {
                return this.userOfClient.getUserStub().setStarterCard(myUsername, face);
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
    }

        public synchronized List<PlayerColor> getAvailableColors() {
        if (!this.RMI) {
            try {
                SingleArgMessage<List<PlayerColor>> message = singleCastAndSend(userOfClient,
                        new ZeroArgMessage(GET_AVAILABLE_COLORS),
                        ANSWER_GET_AVAILABLE_COLORS
                );
                return message.get();
            } catch (ClassCastException e) {
                return null;
            }
        } else {
            try {
                return this.userOfClient.getUserStub().getAvailableColors();
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
    }

    public synchronized boolean setPlayerColor(PlayerColor color) {
        if (!this.RMI) {
            try {
                SingleArgMessage<Boolean> message = singleCastAndSend(userOfClient,
                        new DoubleArgMessage<>(SET_PLAYER_COLOR, myUsername, color),
                        ANSWER_SET_PLAYER_COLOR
                );
                return message.get();
            } catch (ClassCastException e) {
                return false;
            }
        } else {
            try {
                return this.userOfClient.getUserStub().setPlayerColor(myUsername, color);
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
    }

        public synchronized List<GoalCard> getProposedPrivateGoals() {
        if (!this.RMI) {
            try {
                SingleArgMessage<List<GoalCard>> message = singleCastAndSend(userOfClient,
                        new SingleArgMessage<>(GET_PROPOSED_PRIVATE_GOALS, myUsername),
                        ANSWER_GET_PROPOSED_PRIVATE_GOALS
                );
                return message.get();
            } catch (ClassCastException e) {
                return null;
            }
        } else {
            try {
                return this.userOfClient.getUserStub().getProposedPrivateGoals(myUsername);
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
    }

    public synchronized boolean choosePrivateGoal(GoalCard goal) {
        if (!this.RMI) {
            try {
                SingleArgMessage<Boolean> message = singleCastAndSend(userOfClient,
                        new DoubleArgMessage<>(CHOOSE_PRIVATE_GOAL, myUsername, goal),
                        ANSWER_CHOOSE_PRIVATE_GOAL
                );
                return message.get();
            } catch (ClassCastException e) {
                return false;
            }
        } else {
            try {
                return this.userOfClient.getUserStub().choosePrivateGoal(myUsername, goal);
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
    }

        public synchronized PlayerData getPlayerData(String username) {
        if (!this.RMI) {
            try {
                SingleArgMessage<PlayerData> message = singleCastAndSend(userOfClient,
                        new SingleArgMessage<>(GET_PLAYER_DATA, username),
                        ANSWER_GET_PLAYER_DATA
                );
                return message.get();
            } catch (ClassCastException e) {
                return  null;
            }
        } else {
            try {
                return this.userOfClient.getUserStub().getPlayerData(username);
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
    }

        public synchronized List<PlayerData> getAllPlayerData(String username) {
        if (!this.RMI) {
            try {
                SingleArgMessage<List<PlayerData>> message = singleCastAndSend(userOfClient,
                        new SingleArgMessage<>(GET_ALL_PLAYER_DATA, username),
                        ANSWER_GET_ALL_PLAYER_DATA
                );
                return message.get();
            } catch (ClassCastException e) {
                return null;
            }
        } else {
            try {
                return this.userOfClient.getUserStub().getAllPlayerData(username);
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
    }

        public synchronized PlayerManuscript getManuscript(String username) {
        if (!this.RMI) {
            try {
                SingleArgMessage<PlayerManuscript> message = singleCastAndSend(userOfClient,
                        new SingleArgMessage<>(GET_MANUSCRIPT, username),
                        ANSWER_GET_MANUSCRIPT
                );
                return message.get();
            } catch (ClassCastException e) {
                return null;
            }
        } else {
            try {
                return this.userOfClient.getUserStub().getManuscript(username);
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
    }

        public synchronized GameFlow getGameFlow() {
        if (!this.RMI) {
            try {
                SingleArgMessage<GameFlow> message = singleCastAndSend(userOfClient,
                        new ZeroArgMessage(GET_GAMEFLOW),
                        ANSWER_GET_GAMEFLOW
                );
                return message.get();
            } catch (ClassCastException e) {
                return null;
            }
        } else {
            try {
                return this.userOfClient.getUserStub().getGameFlow();
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
    }

        public synchronized CommonBoard getGameBoard() {
        if (!this.RMI) {
            try {
                SingleArgMessage<CommonBoard> message = singleCastAndSend(userOfClient,
                        new ZeroArgMessage(GET_GAMEBOARD),
                        ANSWER_GET_GAMEBOARD
                );
                return message.get();
            } catch (ClassCastException e) {
                return null;
            }
        } else {
            try {
                return this.userOfClient.getUserStub().getGameBoard();
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
    }

    public synchronized boolean placeCard(TypedCard card, ManuscriptPosition position) {
        if (!this.RMI) {
            try {
                SingleArgMessage<Boolean> message = singleCastAndSend(userOfClient,
                        new TripleArgMessage<>(PLACE_CARD, myUsername, card, position),
                        ANSWER_PLACE_CARD
                );
                return message.get();
            } catch (ClassCastException e) {
                return false;
            }
        } else {
            try {
                return this.userOfClient.getUserStub().placeCard(myUsername, card, position);
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
    }

        public synchronized TypedCard drawVisibleCard(CardType type, int index) {
        if (!this.RMI) {
            try {
                SingleArgMessage<TypedCard> message = singleCastAndSend(userOfClient,
                        new TripleArgMessage<>(DRAW_VISIBLE_CARD, myUsername, type, index),
                        ANSWER_DRAW_VISIBLE_CARD
                );
                return message.get();
            } catch (ClassCastException e) {
                return null;
            }
        } else {
            try {
                return this.userOfClient.getUserStub().drawVisibleCard(myUsername, type, index);
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
    }

        public synchronized TypedCard drawCoveredCard(CardType type) {
        if (!this.RMI) {
            try {
                SingleArgMessage<TypedCard> message = singleCastAndSend(userOfClient,
                        new DoubleArgMessage<>(DRAW_COVERED_CARD, myUsername, type),
                        ANSWER_DRAW_COVERED_CARD
                );
                return message.get();
            } catch (ClassCastException e) {
                return null;
            }
        } else {
            try {
                return this.userOfClient.getUserStub().drawCoveredCard(myUsername, type);
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
    }

    public synchronized boolean gameEnded() {
        if (!this.RMI) {
            try {
                SingleArgMessage<Boolean> message = singleCastAndSend(userOfClient,
                        new ZeroArgMessage(GET_GAME_ENDED),
                        ANSWER_GET_GAME_ENDED
                );
                return message.get();
            } catch (ClassCastException e) {
                return false;
            }
        } else {
            try {
                return this.userOfClient.getUserStub().getGameEnded();
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
    }

        public synchronized String getWinner() {
        if (!this.RMI) {
            try {
                SingleArgMessage<String> message = singleCastAndSend(userOfClient,
                        new ZeroArgMessage(GET_WINNER),
                        ANSWER_GET_WINNER
                );
                return message.get();
            } catch (ClassCastException e) {
                return null;
            }
        } else {
            try {
                return this.userOfClient.getUserStub().getWinner();
            } catch (RemoteException e) {
                throw DefaultRMIExceptionsHandlerOfClient.clientCriticalError;
            }
        }
    }

    /**
     * Adds the given {@link Consumer} to {@code listeners} of the specified {@link EventType}
     *
     * @param type     type of event to which add the given {@code Consumer}
     * @param consumer {@code Consumer} to add
     */
    public <T extends Event> void addEventHandler(EventType<T> type, Consumer<T> consumer) {
        eventHandlers.computeIfAbsent(type, k -> new ArrayList<>());
        eventHandlers.get(type).add(new EventHandler<>(consumer, myUsername));
    }

    /**
     * Removes the given {@link Consumer} to {@code listeners} of the specified {@link EventType}
     *
     * @param type     type of event from which remove the given {@code Consumer}
     * @param consumer {@code Consumer} to remove
     */
    public <T extends Event> void removeEventHandler(EventType<T> type, Consumer<T> consumer) {
        List<EventHandler<? extends Event>> presentConsumers = eventHandlers.get(type);
        List<EventHandler<? extends Event>> toDelete = presentConsumers
                .stream()
                .filter(h -> h.handler().equals(consumer)).toList();
        presentConsumers.removeAll(toDelete);
    }

    private <T extends Event> List<Consumer<T>> getEventHandlers(EventType<T> type) {
        List<EventHandler<?>> presentConsumers = eventHandlers.get(type);
        if (presentConsumers != null) {
            return presentConsumers
                    .stream()
                    .map(h -> (EventHandler<T>) h)
                    .map(EventHandler::handler)
                    .toList();
        }
        return new ArrayList<>();
    }

    public <T extends Event> void executeHandlers(EventType<T> type, T info) {
        getEventHandlers(type).forEach(consumer -> {
            (new Thread(() -> consumer.accept(info))).start();
//                consumer.accept(info);
        });
    }
}
