package it.polimi.ingsw.model.example;

import it.polimi.ingsw.model.card.factory.GoldCardFactory;
import it.polimi.ingsw.model.card.factory.ResourceCardFactory;
import it.polimi.ingsw.model.card.factory.StarterCardFactory;
import it.polimi.ingsw.model.player.ManuscriptPosition;
import it.polimi.ingsw.model.player.PlayerManuscript;

/**
 * This class provides non-empty ready-to-use instances of {@link PlayerManuscript}
 */
public class PlayerManuscriptExample {

    /**
     * Returns an initialized manuscript that contains the StarterCard, some ResourceCards and some GoldCards
     *
     * @return an initialized manuscript that contains the StarterCard, some ResourceCards and some GoldCards
     */
    public static PlayerManuscript manuscript1() {
        ResourceCardFactory rFactory = new ResourceCardFactory();
        GoldCardFactory gFactory = new GoldCardFactory();

        PlayerManuscript manuscript = new PlayerManuscript((new StarterCardFactory()).generateCard(81));

        manuscript.insertCard(new ManuscriptPosition(0, 1), rFactory.generateCard(33));
        manuscript.insertCard(new ManuscriptPosition(0, -1), gFactory.generateCard(79));
        manuscript.insertCard(new ManuscriptPosition(0, 2), rFactory.generateCard(37));
        manuscript.insertCard(new ManuscriptPosition(-1, -1), gFactory.generateCard(51));
        manuscript.insertCard(new ManuscriptPosition(1, 0), rFactory.generateCard(18));

        return manuscript;
    }

    /**
     * Returns an initialized manuscript that contains a StarterCard and some ResourceCards
     * @return an initialized manuscript that contains a StarterCard and some ResourceCards
     */
    public static PlayerManuscript manuscript2() {
        ResourceCardFactory rFactory = new ResourceCardFactory();

        PlayerManuscript manuscript = new PlayerManuscript((new StarterCardFactory()).generateCard(81));
        manuscript.insertCard(new ManuscriptPosition(0, 1), rFactory.generateCard(28));
        manuscript.insertCard(new ManuscriptPosition(1, 0), rFactory.generateCard(7));
        manuscript.insertCard(new ManuscriptPosition(2, 0), rFactory.generateCard(14));
        manuscript.insertCard(new ManuscriptPosition(2, 1), rFactory.generateCard(25));

        return manuscript;
    }

    /**
     * Returns an initialized manuscript that contains a StarterCard and many ResourceCards
     * @return an initialized manuscript that contains a StarterCard and many ResourceCards
     */
    public static PlayerManuscript manuscript3() {
        ResourceCardFactory rFactory = new ResourceCardFactory();

        PlayerManuscript manuscript = new PlayerManuscript((new StarterCardFactory()).generateCard(81));
        manuscript.insertCard(new ManuscriptPosition(1, 0), rFactory.generateCard(21));
        manuscript.insertCard(new ManuscriptPosition(2, 0), rFactory.generateCard(37));
        manuscript.insertCard(new ManuscriptPosition(3, 0), rFactory.generateCard(11));
        manuscript.insertCard(new ManuscriptPosition(2, -1), rFactory.generateCard(24));
        manuscript.insertCard(new ManuscriptPosition(3, -1), rFactory.generateCard(2));
        manuscript.insertCard(new ManuscriptPosition(4, -1), rFactory.generateCard(12));
        manuscript.insertCard(new ManuscriptPosition(0, 1), rFactory.generateCard(22));
        manuscript.insertCard(new ManuscriptPosition(0, -1), rFactory.generateCard(18));
        manuscript.insertCard(new ManuscriptPosition(-1, -1), rFactory.generateCard(31));
        manuscript.insertCard(new ManuscriptPosition(0, -2), rFactory.generateCard(32));
        manuscript.insertCard(new ManuscriptPosition(1, -2), rFactory.generateCard(13));

        return manuscript;
    }
}
