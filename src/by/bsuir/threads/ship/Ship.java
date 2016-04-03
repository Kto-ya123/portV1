package by.bsuir.threads.ship;

import by.bsuir.threads.action.ShipAction;
import by.bsuir.threads.birth.Berth;
import by.bsuir.threads.exception.ShipException;
import by.bsuir.threads.port.Port;
import by.bsuir.threads.exception.PortException;
import by.bsuir.threads.storage.Container;
import by.bsuir.threads.storage.ShipStorage;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class Ship extends Thread {

    private static Logger logger = Logger.getLogger(Ship.class);
    private static final int SIZE_SHIP_STORAGE = 5;
    private Integer id;
    private ShipStorage storage;
    private Port port;


    public Ship(Integer id, List<Container> containers) {
        this.id = id;
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
            TimeUnit.SECONDS.sleep(5);
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
        } catch (ShipException e) {
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

    private void loadToOtherShip(Berth berth) throws ShipException {
        boolean result;
        int containersNumberToMove = containersCount(storage.getRealCapacity());
        storage.setCountRequestedContainers(containersNumberToMove);
        logger.info("Ship " + id + " want to upload " + containersNumberToMove + " on other ship.");
        result = berth.addContainerToOtherShip(storage);

        if (!result) {
            logger.info("The ship " + id + " is going to upload " + storage.getCountRequestedContainers() + " containers to port storage.");
            if (berth.addContainerToPort(storage)) {
                logger.info("Port storage  load containers from ship " + id);
            } else {
                logger.warn("Port haven't place to load containers from ship" + id);
            }
        }

    }

    private void loadFromOtherShip(Berth berth) throws ShipException {
        int containersNumberToMove = containersCount(storage.getFreeSize());
        storage.setCountRequestedContainers(containersNumberToMove);
        logger.info("Ship " + id + " want to load " + containersNumberToMove + " from other ship.");

        if (!berth.getContainerFromOtherShip(storage)) {
            logger.info("Ship " + id + " want to load " + storage.getCountRequestedContainers() + " from port.");
            if (!berth.getContainersFromPort(storage)) {
                logger.info("Port load containers to ship " + id);
            } else {
                logger.warn("Port haven't containers to load ship " + id);
            }
        }
    }


    private void executeAction(ShipAction action, Berth berth) throws ShipException {
        switch (action) {
            case LOAD_TO_PORT:
                loadToPort(berth);
                break;
            case LOAD_FROM_PORT:
                loadFromPort(berth);
                break;
            case LOAD_TO_OTHER_SHIP:
                loadToOtherShip(berth);
                break;
            case LOAD_FROM_OTHER_SHIP:
                loadFromOtherShip(berth);
                break;
        }
    }

    private ShipAction getNewShipAction() {
        Random random = new Random();
        int value = random.nextInt(10000);
        if (value < 1000) {
            return ShipAction.LOAD_TO_PORT;
        } else if (value < 3000) {
            return ShipAction.LOAD_FROM_PORT;
        } else if (value < 6000)
            return ShipAction.LOAD_FROM_OTHER_SHIP;
        return ShipAction.LOAD_TO_OTHER_SHIP;
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
