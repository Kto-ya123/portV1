package by.bsuir.threads.birth;

import by.bsuir.threads.storage.Container;
import by.bsuir.threads.storage.ShipStorage;
import by.bsuir.threads.storage.Storage;
import org.apache.log4j.Logger;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Berth{

    private static Logger logger = Logger.getLogger(Berth.class);

    private int id;
    private Storage portStorage;
    private static Lock portStorageLock = new ReentrantLock();
    private static Lock lockOfLoadingStorage = new ReentrantLock();
    private static Lock lockOfUploadingStorage = new ReentrantLock();
    private static Queue<ShipStorage> queueLoadingShipStorage = new ArrayDeque<>();
    private static Queue<ShipStorage> queueUploadingShipStorage = new ArrayDeque<>();

    public Berth(int id, Storage storage) {
        this.id = id;
        portStorage = storage;
    }


    public int getId() {
        return id;
    }


    public boolean addContainerToPort(ShipStorage shipStorage) {
        boolean result = false;

        lockOfUploadingStorage.lock();
        try {
            queueUploadingShipStorage.add(shipStorage);
        } finally {
            lockOfUploadingStorage.unlock();
        }


        portStorageLock.lock();
        try {
            shipStorage.takeLock();
            if (shipStorage.getCountRequestedContainers() <= portStorage.getFreeSize()) {
                result = doMoveFromShip(shipStorage);
            }
            shipStorage.giveLock();
        } finally {
            portStorageLock.unlock();
        }
        return result;
    }


    private boolean doMoveFromShip(ShipStorage shipStorage) {
        boolean result = false;
        lockOfUploadingStorage.lock();
        try {
            List<Container> containers = shipStorage.getContainer(shipStorage.getCountRequestedContainers());
            portStorage.addContainer(containers);
            result = true;
        } finally {
            queueUploadingShipStorage.remove(shipStorage);
            lockOfUploadingStorage.unlock();
        }
        return result;
    }

    public boolean getContainersFromPort(ShipStorage shipStorage) {
        boolean result = false;

        lockOfLoadingStorage.lock();
        try {
            queueLoadingShipStorage.add(shipStorage);
        } finally {
            lockOfLoadingStorage.unlock();
        }


        portStorageLock.lock();
        try {
            shipStorage.takeLock();
            if (shipStorage.getCountRequestedContainers() <= portStorage.getRealCapacity()) {
                result = doMoveFromPort(shipStorage);
            }
            shipStorage.giveLock();
        } finally {
            portStorageLock.unlock();
        }
        return result;
    }

    private boolean doMoveFromPort(ShipStorage shipStorage) {
        boolean result = false;
        lockOfLoadingStorage.lock();
        try {
            List<Container> containers = portStorage.getContainer(shipStorage.getCountRequestedContainers());
            shipStorage.addContainer(containers);
            result = true;
        } finally {
            queueLoadingShipStorage.remove(shipStorage);
            lockOfLoadingStorage.unlock();
        }
        return result;
    }
}



