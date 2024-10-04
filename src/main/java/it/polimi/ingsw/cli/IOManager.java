package it.polimi.ingsw.cli;

import it.polimi.ingsw.model.card.GoalCard;
import it.polimi.ingsw.model.card.GoldCard;
import it.polimi.ingsw.model.card.StarterCard;
import it.polimi.ingsw.model.card.TypedCard;
import it.polimi.ingsw.model.card.bonus.BonusCorners;
import it.polimi.ingsw.model.card.bonus.BonusObjects;
import it.polimi.ingsw.model.card.bonus.CardBonus;
import it.polimi.ingsw.model.card.goal.GoalRequirementItems;
import it.polimi.ingsw.model.card.goal.GoalRequirementPattern;
import it.polimi.ingsw.model.card.properties.*;
import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerColor;
import it.polimi.ingsw.model.player.PlayerManuscript;
import org.fusesource.jansi.AnsiConsole;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import static org.fusesource.jansi.Ansi.Color;
import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * Offers various methods to pretty print objects on CLI.
 */
public class IOManager {
    private final Scanner scanner;

    public IOManager() {
        scanner = new Scanner(System.in);
        AnsiConsole.systemInstall();
    }

    public void positions(Set<ManuscriptPosition> positions) {
        for (ManuscriptPosition p : positions) {
            println(p.toString());
        }
    }

    public void goal(GoalCard goal) {
        println(goal.toString());
        if (goal.getRequirement() instanceof GoalRequirementItems gi) {
            System.out.println(gi.getRequirement());
        } else if (goal.getRequirement() instanceof GoalRequirementPattern gp) {
            System.out.println(gp.getRequirement());
        }
    }

    public void colors(List<PlayerColor> colors) {
        int index = 0;
        for (PlayerColor color : colors) {
            println("ID: " + index + ", color: " + color.toString());
            index++;
        }
    }

    public void hand(List<TypedCard> hand) {
        int index = 0;
        for (TypedCard card : hand) {
            println("ID: " + index);
            typed(card);
            index++;
        }
    }

    public void typed(TypedCard card) {
        if (card != null) {
            int width = 13;
            println("---------------");

            print("| ");
            printCorner(card.getCorner(CornerPosition.TOP_LEFT));
            print("    ");
            if (card.getFace() == CardFace.FRONT) {
                print(Integer.toString(card.getBonus().getPoints()));
                if (card instanceof GoldCard gc) {
                    CardBonus bonus = gc.getBonus();
                    if (bonus instanceof BonusCorners) {
                        print("C");
                    } else if (bonus instanceof BonusObjects) {
                        printItem(((BonusObjects) bonus).getObject());
                    } else {
                        print(" ");
                    }
                } else {
                    print(" ");
                }
            } else {
                print("  ");
            }
            print("   ");
            printCorner(card.getCorner(CornerPosition.TOP_RIGHT));
            println(" |");

            print("|      ");
            printItem(card.getKingdom());
            println("      |");

            print("| ");
            printCorner(card.getCorner(CornerPosition.BOTTOM_LEFT));
            if (card instanceof GoldCard) {
                print("  ");
                int n = requirements((GoldCard) card);
                for (int i = n; i < 5; i++)     // 5 is the maximum number of requirements
                    print(" ");
                print("  ");
            } else {
                print("         ");
            }
            printCorner(card.getCorner(CornerPosition.BOTTOM_RIGHT));
            println(" |");

            println("---------------");
        } else {
            println("Card not available");
        }
    }

    public void coveredCard(CardKingdom kingdom) {
        if (kingdom != null) {
            println("---------------");
            println("|             |");
            print("|      ");
            printItem(kingdom);
            println("      |");
            println("|             |");
            println("---------------");
        } else {
            println("Card not available");
        }
    }

    public void items(Map<CardItem, Integer> map) {
        for (CardItem i : map.keySet()) {
            printItem(i);
            print(": ");
            println(map.get(i).toString());
        }
    }

    private int requirements(GoldCard card) {
        int n = 0;

        for (CardKingdom kingdom : card.getCost().keySet()) {
            for (int i = 0; i < card.getCost().get(kingdom); i++) {
                printItem(kingdom);
                n++;
            }
        }

        return n;
    }

    private void printCorner(CornerItem item) {
        if (item == null) {
            print(" ");
        } else {
            printItem(item.item());
        }
    }

    private void printItem(CardItem item) {
        if (item == null) {
            printBackgroundColor(" ", WHITE);
        } else {
            if (item instanceof CardKingdom kingdom) {
                if (kingdom == CardKingdom.FUNGI) {
                    printColor("F", RED);
                } else if (kingdom == CardKingdom.PLANT) {
                    printColor("P", GREEN);
                } else if (kingdom == CardKingdom.ANIMAL) {
                    printColor("A", CYAN);
                } else if (kingdom == CardKingdom.INSECT) {
                    printColor("I", MAGENTA);
                }
            } else if (item instanceof CardObject object) {
                if (object == CardObject.QUILL) {
                    printColor("Q", YELLOW);
                } else if (object == CardObject.MANUSCRIPT) {
                    printColor("M", YELLOW);
                } else if (object == CardObject.INKWELL) {
                    printColor("W", YELLOW);
                }
            }
        }
    }

    public String manuscript(PlayerManuscript manuscript) {
        return manuscript.toString();
    }

    public void starter(StarterCard card) {
        println(card.toString());
    }

    public synchronized static void println(String str) {
        System.out.println(str);
    }

    public synchronized static void print(String str) {
        System.out.print(str);
    }

    public synchronized static void printColor(String str, Color color) {
        System.out.print(ansi().fg(color).a(str).reset());
    }

    public synchronized static void printBackgroundColor(String str, Color color) {
        System.out.print(ansi().bg(color).a(str).reset());
    }

    public synchronized static void printPrompt(CliState state) {
        String PROMPT = "> ";
        Color color = switch (state) {
            case PRE_LOBBY -> BLUE;
            case LOBBY -> YELLOW;
            case INIT -> BLUE;
            case WAIT -> YELLOW;
            case PLAY -> MAGENTA;
            case DRAW -> CYAN;
            case END -> DEFAULT;
        };
        printColor(state + PROMPT, color);
    }

    public String read() {
        return scanner.nextLine();
    }

    public String readPrintPrompt(CliState state) {
        printPrompt(state);
        return read();
    }

    public static void error(String message) {
        printColor("[Error]: ", RED);
        printColor(message, RED);
        println("");
    }

    public static void splashScreen() {
        printColor("""
                 ▄▄▄▄▄▄▄ ▄▄▄▄▄▄▄ ▄▄▄▄▄▄  ▄▄▄▄▄▄▄ ▄▄   ▄▄    ▄▄    ▄ ▄▄▄▄▄▄ ▄▄▄▄▄▄▄ ▄▄   ▄▄ ▄▄▄▄▄▄   ▄▄▄▄▄▄ ▄▄▄     ▄▄▄ ▄▄▄▄▄▄▄\s
                █       █       █      ██       █  █▄█  █  █  █  █ █      █       █  █ █  █   ▄  █ █      █   █   █   █       █
                █       █   ▄   █  ▄    █    ▄▄▄█       █  █   █▄█ █  ▄   █▄     ▄█  █ █  █  █ █ █ █  ▄   █   █   █   █  ▄▄▄▄▄█
                █     ▄▄█  █ █  █ █ █   █   █▄▄▄█       █  █       █ █▄█  █ █   █ █  █▄█  █   █▄▄█▄█ █▄█  █   █   █   █ █▄▄▄▄▄\s
                █    █  █  █▄█  █ █▄█   █    ▄▄▄██     █   █  ▄    █      █ █   █ █       █    ▄▄  █      █   █▄▄▄█   █▄▄▄▄▄  █
                █    █▄▄█       █       █   █▄▄▄█   ▄   █  █ █ █   █  ▄   █ █   █ █       █   █  █ █  ▄   █       █   █▄▄▄▄▄█ █
                █▄▄▄▄▄▄▄█▄▄▄▄▄▄▄█▄▄▄▄▄▄██▄▄▄▄▄▄▄█▄▄█ █▄▄█  █▄█  █▄▄█▄█ █▄▄█ █▄▄▄█ █▄▄▄▄▄▄▄█▄▄▄█  █▄█▄█ █▄▄█▄▄▄▄▄▄▄█▄▄▄█▄▄▄▄▄▄▄█

                """, CYAN);
    }

    public static void cleanTerminal() {
        System.out.println(ansi().eraseScreen().reset());
    }
}
