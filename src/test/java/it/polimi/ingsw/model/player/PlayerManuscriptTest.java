package it.polimi.ingsw.model.player;

import it.polimi.ingsw.model.card.GoldCard;
import it.polimi.ingsw.model.card.StarterCard;
import it.polimi.ingsw.model.card.factory.GoldCardFactory;
import it.polimi.ingsw.model.card.factory.ResourceCardFactory;
import it.polimi.ingsw.model.card.factory.StarterCardFactory;
import it.polimi.ingsw.model.example.PlayerManuscriptExample;
import org.junit.Test;

import java.util.Set;

import static it.polimi.ingsw.model.card.properties.CardKingdom.*;
import static it.polimi.ingsw.model.card.properties.CardObject.*;
import static org.junit.Assert.*;

public class PlayerManuscriptTest {

    @Test
    public void exampleManuscriptsAreLegalTest() {
        PlayerManuscriptExample.manuscript1();
        PlayerManuscriptExample.manuscript2();
        PlayerManuscriptExample.manuscript3();
        // test passed if we got here without throwing exceptions
    }

    @Test
    public void isEmptyTest() {
        PlayerManuscript m1 = new PlayerManuscript();
        assertTrue(m1.isEmpty());

        PlayerManuscript m2 = new PlayerManuscript(new StarterCardFactory().generateCard(82));
        assertFalse(m2.isEmpty());

        PlayerManuscript m3 = new PlayerManuscript(m2);
        assertFalse(m3.isEmpty());
    }

    @Test
    public void getCardAtPositionTest() {
        PlayerManuscript manuscript = PlayerManuscriptExample.manuscript1();

        assertNull(manuscript.getCardAt(new ManuscriptPosition(1, 1)));

        assertEquals(
                manuscript.getCardAt(new ManuscriptPosition(0, 1)),
                (new ResourceCardFactory()).generateCard(33)
        );
        assertEquals(
                manuscript.getCardAt(new ManuscriptPosition(0, -1)),
                (new GoldCardFactory()).generateCard(79)
        );
        assertEquals(
                manuscript.getCardAt(new ManuscriptPosition(0, 2)),
                (new ResourceCardFactory()).generateCard(37)
        );
        assertEquals(
                manuscript.getCardAt(new ManuscriptPosition(-1, -1)),
                (new GoldCardFactory()).generateCard(51)
        );
        assertEquals(
                manuscript.getCardAt(new ManuscriptPosition(1, 0)),
                (new ResourceCardFactory()).generateCard(18)
        );

        assertNull(manuscript.getCardAt(new ManuscriptPosition(5, -1)));
    }

    @Test
    public void getAllAvailablePositionsTest() {
        PlayerManuscript manuscript = PlayerManuscriptExample.manuscript1();
        Set<ManuscriptPosition> positions = manuscript.getAllAvailablePositions();

        assertEquals(6, positions.size());

        assertTrue(positions.contains(new ManuscriptPosition(-1, -2)));
        assertTrue(positions.contains(new ManuscriptPosition(-1, 1)));
        assertTrue(positions.contains(new ManuscriptPosition(-2, -1)));
        assertTrue(positions.contains(new ManuscriptPosition(-1, 2)));
        assertTrue(positions.contains(new ManuscriptPosition(1, 2)));
        assertTrue(positions.contains(new ManuscriptPosition(2, 0)));

        assertFalse(positions.contains(new ManuscriptPosition(0, 0)));
        assertFalse(positions.contains(new ManuscriptPosition(-1, 0)));
        assertFalse(positions.contains(new ManuscriptPosition(1, -1)));
        assertFalse(positions.contains(new ManuscriptPosition(0, 2)));
        assertFalse(positions.contains(new ManuscriptPosition(5, 5)));

        manuscript = PlayerManuscriptExample.manuscript2();
        positions = manuscript.getAllAvailablePositions();
        assertEquals(9, positions.size());
    }

    @Test
    public void getAllOccupiedPositionsTest() {
        PlayerManuscript manuscript = PlayerManuscriptExample.manuscript1();
        Set<ManuscriptPosition> positions = manuscript.getAllOccupiedPositions();
        assertEquals(6, positions.size());
        assertTrue(positions.contains(new ManuscriptPosition(0, 0)));
        assertTrue(positions.contains(new ManuscriptPosition(0, 1)));
        assertFalse(positions.contains(new ManuscriptPosition(5, 0)));
    }

    @Test
    public void cannotPlaceNullCardOrPositionToManuscriptTest() {
        PlayerManuscript m = PlayerManuscriptExample.manuscript1();

        assertFalse(m.isPlaceable(null, null));
        assertFalse(m.isPlaceable(null, new ResourceCardFactory().generateCard(5)));
        assertFalse(m.isPlaceable(new ManuscriptPosition(-1, -2), null));
    }

    @Test
    public void cannotPlaceCardInOccupiedPositionTest() {
        PlayerManuscript m = PlayerManuscriptExample.manuscript1();

        assertFalse(m.isPlaceable(new ManuscriptPosition(0, 0), new ResourceCardFactory().generateCard(6)));
    }

    @Test
    public void cannotPlaceCardWithoutCostBeingSatisfiedTest() {
        PlayerManuscript m = PlayerManuscriptExample.manuscript1();
        GoldCard card = new GoldCardFactory().generateCard(60);

        assertFalse(m.isPlaceable(new ManuscriptPosition(-2, -1), card));

        // However, if we flip it we can always place it
        card.flip();
        assertTrue(m.isPlaceable(new ManuscriptPosition(-2, -1), card));

    }

    @Test
    public void isPlaceableTest() {
        PlayerManuscript m = PlayerManuscriptExample.manuscript1();

        assertTrue(m.isPlaceable(new ManuscriptPosition(-1, -2), new ResourceCardFactory().generateCard(5)));

    }

    @Test
    public void cannotPlaceSameCardTwice() {
        PlayerManuscript m = PlayerManuscriptExample.manuscript1();

        assertTrue(m.isPlaceable(new ManuscriptPosition(-1, -2), new ResourceCardFactory().generateCard(5)));
        m.insertCard(new ManuscriptPosition(-1, -2), new ResourceCardFactory().generateCard(5));
        assertFalse(m.isPlaceable(new ManuscriptPosition(2, 0), new ResourceCardFactory().generateCard(5)));

    }

    @Test
    public void insertCardTest() {
        PlayerManuscript manuscript = PlayerManuscriptExample.manuscript2();

        assertThrows(InvalidPlacingException.class, () -> manuscript.insertCard(new ManuscriptPosition(0, 0), (new ResourceCardFactory()).generateCard(15)));
        try {
            manuscript.insertCard(new ManuscriptPosition(0, 0), (new ResourceCardFactory()).generateCard(14));
            fail();
        } catch (InvalidPlacingException ignored) {
        }

        int oldCards = manuscript.getAllOccupiedPositions().size();
        manuscript.insertCard(new ManuscriptPosition(2, 2), (new ResourceCardFactory()).generateCard(13));
        assertEquals(oldCards +1, manuscript.getAllOccupiedPositions().size());
        assertEquals(1, (int) manuscript.getNumberOfItem(ANIMAL));
    }

    @Test
    public void insertCardReturnsPointsScoredTest() {
        PlayerManuscript manuscript = PlayerManuscriptExample.manuscript1();
        GoldCard card = new GoldCardFactory().generateCard(78);

        assertEquals(3, manuscript.insertCard(
                new ManuscriptPosition(1, 2),
                card
        ));

        manuscript = PlayerManuscriptExample.manuscript1();
        card.flip();

        assertEquals(0, manuscript.insertCard(
                new ManuscriptPosition(1, 2),
                card
        ));
    }

    @Test
    public void placingCardManagesItemsNumberTest() {
        PlayerManuscript m1 = PlayerManuscriptExample.manuscript1();
        PlayerManuscript m2 = PlayerManuscriptExample.manuscript1();

        m2.insertCard(
                new ManuscriptPosition(1, 2),
                new ResourceCardFactory().generateCard(36)
        );

        assertEquals(m1.getNumberOfItem(FUNGI) + 1, (int) m2.getNumberOfItem(FUNGI));
        assertEquals(m1.getNumberOfItem(PLANT) - 1, (int) m2.getNumberOfItem(PLANT));
        assertEquals((int) m1.getNumberOfItem(ANIMAL), (int) m2.getNumberOfItem(ANIMAL));
        assertEquals(m1.getNumberOfItem(INSECT) + 1, (int) m2.getNumberOfItem(INSECT));
        assertEquals(m1.getNumberOfItem(MANUSCRIPT) + 1, (int) m2.getNumberOfItem(MANUSCRIPT));
        assertEquals((int) m1.getNumberOfItem(QUILL), (int) m2.getNumberOfItem(QUILL));
        assertEquals((int) m1.getNumberOfItem(INKWELL), (int) m2.getNumberOfItem(INKWELL));

        StarterCard starter = new StarterCardFactory().generateCard(81);
        starter.flip();

        PlayerManuscript man = new PlayerManuscript(starter);

        man.insertCard(
                new ManuscriptPosition(1, 0),
                new ResourceCardFactory().generateCard(36)
        );

        assertEquals( 2, (int) man.getNumberOfItem(FUNGI));
        assertEquals( 0, (int) man.getNumberOfItem(PLANT));
        assertEquals(1, (int) man.getNumberOfItem(ANIMAL));
        assertEquals(2, (int) man.getNumberOfItem(INSECT));
        assertEquals(1, (int) man.getNumberOfItem(MANUSCRIPT));
        assertEquals(0, (int) man.getNumberOfItem(QUILL));
        assertEquals(0, (int) man.getNumberOfItem(INKWELL));
    }

    @Test
    public void starterCardItemsAreCorrectlyDeployedTest() {
        StarterCard starter = new StarterCardFactory().generateCard(81);

        PlayerManuscript man = new PlayerManuscript(starter);

        assertEquals( 0, (int) man.getNumberOfItem(FUNGI));
        assertEquals( 1, (int) man.getNumberOfItem(PLANT));
        assertEquals(0, (int) man.getNumberOfItem(ANIMAL));
        assertEquals(2, (int) man.getNumberOfItem(INSECT));
        assertEquals(0, (int) man.getNumberOfItem(MANUSCRIPT));
        assertEquals(0, (int) man.getNumberOfItem(QUILL));
        assertEquals(0, (int) man.getNumberOfItem(INKWELL));

        starter.flip();

        PlayerManuscript m2 = new PlayerManuscript(starter);

        assertEquals( 1, (int) m2.getNumberOfItem(FUNGI));
        assertEquals( 1, (int) m2.getNumberOfItem(PLANT));
        assertEquals(1, (int) m2.getNumberOfItem(ANIMAL));
        assertEquals(1, (int) m2.getNumberOfItem(INSECT));
        assertEquals(0, (int) m2.getNumberOfItem(MANUSCRIPT));
        assertEquals(0, (int) m2.getNumberOfItem(QUILL));
        assertEquals(0, (int) m2.getNumberOfItem(INKWELL));
    }
}
