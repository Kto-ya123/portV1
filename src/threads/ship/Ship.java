package threads.ship;

import org.apache.log4j.Logger;
import threads.action.ShipAction;
import threads.birth.Berth;
import threads.exception.PortException;
import threads.exception.ShipException;
import threads.port.Port;
import threads.storage.Container;
import threads.storage.ShipStorage;

import java.util.List;
import java.util.Random;


public class Ship extends Thread {

    private static Logger logger = Logger.getLogger(Ship.class);
    private static final int SIZE_SHIP_STORAGE = 5;
    private Integer id;
    private ShipStorage storage;
    private Port port;
    private Integer importance;


    public Ship(Integer id, List<Container> containers, Integer priority, Integer importance) {
        this.id = id;
        this.setPriority(priority);
        this.importance = importance;
        this.storage = new ShipStorage(SIZE_SHIP_STORAGE, id);
        this.storage.setContainers(containers);
    }

    public int getShipId() {

        return id;
    }

    @Override
    public void run() {
        try {
            port = Port.getInstance();
            while (true) {
                swim();
                goToPort();
            }
        } catch (PortException e) {
            logger.error("Ship was destroyed..", e);
        }
    }


    private void swim() {
        try {
            Thread.sleep(3000 - importance);
            if(port.isInMulctShip(this)){
                Thread.sleep(5000);
            }

        } catch (InterruptedException e) {
            logger.error("Ship was destroyed at time of swim", e);
        }
    }


    private void goToPort() throws PortException {

        boolean isLockedBerth = false;
        Berth berth = null;
        try {
            isLockedBerth = port.lockBerth(this);

            if (isLockedBerth) {
                berth = port.getBerth(this);
                logger.info("The ship " + id + " moored to the pier " + berth.getId());
                ShipAction action = getNewShipAction();
                executeAction(action, berth);
            } else {
                logger.info("Ship " + id + " couldn't moored to berth ");
            }
        } catch (ShipException | InterruptedException e) {
            logger.warn(e.toString());
        } finally {
            if (isLockedBerth) {
                port.unlockBerth(this);
                logger.info("The ship " + id + " moved away from the pier " + berth.getId());
            }
        }

    }


    private void loadToPort(Berth berth) throws ShipException {

        int containersNumberToMove = containersCount(storage.getRealCapacity());
        storage.setCountRequestedContainers(containersNumberToMove);
        boolean result;

        logger.info("The ship " + id + " is going to upload " + containersNumberToMove + " containers to port storage.");

        result = berth.addContainerToPort(storage);

        if (result) {
            logger.info("Port storage  load containers from ship " + id);
        } else {
            logger.warn("Port haven't place to load containers from ship" + id);
        }
    }

    private void loadFromPort(Berth berth) throws ShipException {

        int containersNumberToMove = containersCount(storage.getFreeSize());
        storage.setCountRequestedContainers(containersNumberToMove);

        boolean result;

        logger.info("The ship " + id + " want to load " + containersNumberToMove + " containers from port storage.");

        result = berth.getContainersFromPort(storage);

        if (result) {
            logger.info("The ship " + id + " loaded " + containersNumberToMove + " containers from port.");
        } else {
            logger.warn("Port haven't containers to load ship " + id);
        }

    }

    private void executeAction(ShipAction action, Berth berth) throws ShipException, InterruptedException {
        Random random = new Random();
        int timeInPort = random.nextInt(10);
        long start = System.currentTimeMillis();
        switch (action) {
            case LOAD_TO_PORT:
                loadToPort(berth);
                break;
            case LOAD_FROM_PORT:
                loadFromPort(berth);
                break;
        }
        long end = System.currentTimeMillis();
        if(end - start > timeInPort){
            port.addMulctShip(this);
            logger.error("The ship " + getId() + " out of time on " + berth.getId() + " pier");
        }
    }

    private ShipAction getNewShipAction() {
        Random random = new Random();
        int value = random.nextInt(10000);
        if (value < 5000) {
            return ShipAction.LOAD_TO_PORT;
        } else {
            return ShipAction.LOAD_FROM_PORT;
        }
    }


    private int containersCount(int sizeStorage) throws ShipException {
        if (sizeStorage > 0) {
            Random random = new Random();
            int result = random.nextInt(sizeStorage);
            if (result == 0) {
                result++;
            }
            return result;
        } else {
            throw new ShipException("Invalid action in ship " + id);
        }

    }
}
