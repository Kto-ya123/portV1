package by.bsuir.threads.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class ShipStorage extends Storage {
    private Lock lock;
    private int countRequestedContainers;
    private final int idShip;


    public ShipStorage(int capacity, int idShip) {
        super(capacity);
        lock = new ReentrantLock();
        this.idShip = idShip;
    }

    public int getIdShip() {
        return idShip;
    }

    public void takeLock() {
        lock.lock();
    }

    public void giveLock() {
        lock.unlock();
    }

    public boolean takeLockForOtherShip() {
        return lock.tryLock();
    }

    public int getCountRequestedContainers() {
        return countRequestedContainers;
    }

    public void setCountRequestedContainers(int countRequestedContainers) {
        this.countRequestedContainers = countRequestedContainers;
    }


    @Override
    public void addContainer(List<Container> containers) {
        countRequestedContainers -= containers.size();
        this.containers.addAll(containers);
    }

    @Override
    public List<Container> getContainer(int numberOfContainers) {
        countRequestedContainers -= numberOfContainers;
        List<Container> gettingContainers = new ArrayList<>(containers.subList(0, numberOfContainers));
        containers.removeAll(gettingContainers);
        return gettingContainers;
    }


}
