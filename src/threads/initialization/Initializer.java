package threads.initialization;

import threads.port.Port;
import threads.ship.Ship;
import threads.storage.Container;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Initializer {
    private final static int COUNT_SHIP_CONTAINERS = 5;
    private final static int COUNT_PORT_CONTAINERS = 20;

    public static ArrayList<Ship> initializeShips(int countShips) {
        Random random = new Random();
        ArrayList<Ship> ships = new ArrayList<>();
        for (int i = 0; i < countShips; i++) {
            ArrayList<Container> containers = new ArrayList<>();
            for (int j = 0; j < COUNT_SHIP_CONTAINERS; j++) {
                int idContainer = i * 10 + j;
                containers.add(new Container(idContainer));
            }

            Ship ship = new Ship(i, containers, random.nextInt(10) + 1, random.nextInt(2000));
            ships.add(ship);

        }

        return ships;
    }

    public static void initializePort(int countShips) {
        List<Container> containers = new ArrayList<>();
        int id;
        for (int i = 0; i < COUNT_PORT_CONTAINERS; i++) {
            id = (countShips * 10) + i;
            containers.add(new Container(id));
        }
        Port.initPortStorage(containers);
    }
}


class CallingMethodsInSameClass {
    private static int COUNT_METHODS = 5;
    private List<Integer> list = new ArrayList<>();

    public static void main(String[] args) {
        int b = 5;
        printOne();
        printOne();
        printTwo();
    }



    public static void printOne() {
        System.out.println("Hello World");
    }

    public static void printTwo() {
        printOne();
        printOne();
    }

    interface  a{

    }
}

