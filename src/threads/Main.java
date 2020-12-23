package threads;

import org.apache.log4j.LogManager;
import org.apache.log4j.xml.DOMConfigurator;
import threads.initialization.Initializer;
import threads.ship.Ship;

import java.util.ArrayList;

public class Main {
    private final static int COUNT_SHIPS = 10;

    static {
        new DOMConfigurator().doConfigure("log4jv2.xml", LogManager.getLoggerRepository());
    }


    public static void main(String[] args) {

        ArrayList<Ship> ships = Initializer.initializeShips(COUNT_SHIPS);
        Initializer.initializePort(ships.size());
        ships.forEach(Ship::start);

    }
}
