package by.bsuir.threads.port;

import by.bsuir.threads.birth.Berth;
import by.bsuir.threads.exception.PortException;
import by.bsuir.threads.ship.Ship;
import by.bsuir.threads.storage.Container;
import by.bsuir.threads.storage.Storage;
import org.apache.log4j.Logger;


import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Port {

    private static Logger logger = Logger.getLogger(Port.class);

    private static Port instance;
    private static AtomicBoolean instanceCreated = new AtomicBoolean(false);
    private static Lock lock = new ReentrantLock();
    private static Condition notEmpty = lock.newCondition();

    private final static int CAPACITY_PORT_STORAGE = 200;
    private final static int COUNT_BERTH = 4;

    private static Storage storage = new Storage(CAPACITY_PORT_STORAGE);
    private Queue<Berth> berthQueue;

    private Map<Ship, Berth> usedBirth;


    private Port() {

        berthQueue = new ArrayDeque<>(COUNT_BERTH);
        for (int i = 0; i < COUNT_BERTH; i++) {
            berthQueue.add(new Berth(i, storage));
        }

        usedBirth = new HashMap<>();

    }


    public static void initPortStorage(List<Container> containers) {
        storage.setContainers(containers);
    }

    public static Port getInstance() {

        if (!instanceCreated.get()) {
            lock.lock();
            try {
                if (!instanceCreated.get()) {
                    instance = new Port();
                    instanceCreated.set(true);
                }
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }


    public boolean lockBerth(Ship ship) {
        Berth berth;
        boolean result = false;
        lock.lock();
        try {
            while (!result) {
                if (berthQueue.size() > 0) {
                    berth = berthQueue.element();
                    berthQueue.remove(berth);
                    usedBirth.put(ship, berth);
                    result = true;
                } else {
                    notEmpty.await();
                }
            }
        } catch (InterruptedException e) {
            logger.error("Ship " + ship.getShipId() + " couldn't lock birth");
        } finally {
            lock.unlock();

        }
        return result;
    }


    public void unlockBerth(Ship ship) {
        lock.lock();
        Berth berth;
        try {
            berth = usedBirth.get(ship);
            berthQueue.add(berth);
            usedBirth.remove(ship);
            notEmpty.signal();
        } catch (Exception e) {
            logger.error("Ship " + ship.getShipId() + " couldn't moore from port");
        } finally {
            lock.unlock();
        }
    }


    public Berth getBerth(Ship ship) throws PortException {
        lock.lock();
        Berth berth;
        try {
            berth = usedBirth.get(ship);
            if (berth == null) {
                throw new PortException("Try to use Berth without blocking.");
            }
        } finally {
            lock.unlock();
        }
        return berth;
    }

}
