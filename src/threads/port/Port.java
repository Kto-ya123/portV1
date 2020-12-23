package threads.port;

import org.apache.log4j.Logger;
import threads.birth.Berth;
import threads.exception.PortException;
import threads.ship.Ship;
import threads.storage.Container;
import threads.storage.Storage;

import java.util.*;


public class Port {

    private static Logger logger = Logger.getLogger(Port.class);

    private static Port instance;

    private final static int CAPACITY_PORT_STORAGE = 200;
    private final static int COUNT_BERTH = 4;

    private static Storage storage = new Storage(CAPACITY_PORT_STORAGE);
    private final Queue<Berth> berthQueue;
    private Set<Ship> mulctShips;

    private Map<Ship, Berth> usedBirth;


    private Port() {
        mulctShips = new HashSet<>();
        berthQueue = new ArrayDeque<>(COUNT_BERTH);
        for (int i = 0; i < COUNT_BERTH; i++) {
            berthQueue.add(new Berth(i, storage));
        }

        usedBirth = new HashMap<>();

    }


    public static void initPortStorage(List<Container> containers) {
        storage.setContainers(containers);
    }

    public static synchronized Port getInstance() {

        if(instance == null){
            instance = new Port();
        }
        return instance;
    }


    public boolean lockBerth(Ship ship) {
        Berth berth;
        boolean result = false;
        try {
            while (!result) {
                synchronized (berthQueue) {
                    synchronized (usedBirth) {
                        if (berthQueue.size() > 0) {
                            berth = berthQueue.element();
                            berthQueue.remove(berth);
                            usedBirth.put(ship, berth);
                            result = true;
                        }
                    }
                }
                if(!result){
                    Thread.sleep(1000);
                }
            }
        } catch (InterruptedException e) {
            logger.error("Ship " + ship.getShipId() + " couldn't lock birth");
        }
        return result;
    }


    public void unlockBerth(Ship ship) {
        Berth berth;
        try {
            synchronized (berthQueue) {
                berth = usedBirth.get(ship);
                berthQueue.add(berth);
                usedBirth.remove(ship);
            }
        } catch (Exception e) {
            logger.error("Ship " + ship.getShipId() + " couldn't moore from port");
        }
    }


    public Berth getBerth(Ship ship) throws PortException {
        Berth berth;
        synchronized (usedBirth) {
            berth = usedBirth.get(ship);
            if (berth == null) {
                throw new PortException("Try to use Berth without blocking.");
            }
        }

        return berth;
    }

    public synchronized void addMulctShip(Ship ship){
        mulctShips.add(ship);
    }

    public boolean isInMulctShip(Ship ship){
        return mulctShips.contains(ship);
    }

}
