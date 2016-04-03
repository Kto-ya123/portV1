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


    public boolean getContainerFromOtherShip(ShipStorage storage) {
        boolean result = false;
        lockOfUploadingStorage.lock();
        try {
            if (!queueUploadingShipStorage.isEmpty()) {
                ShipStorage uploadingStorage = queueUploadingShipStorage.poll();
                logger.info("Ship " + storage.getIdShip() + " get ship " + uploadingStorage.getIdShip() + " for loading");
                result = doMoveFromOtherShip(storage, uploadingStorage);
                queueUploadingShipStorage.add(uploadingStorage);
            } else {
                logger.warn("Ship " + storage.getIdShip() + " doesn't get ship for loading");
            }
        } finally {
            lockOfUploadingStorage.unlock();
        }
        return result;
    }


    private boolean doMoveFromOtherShip(ShipStorage loadingShipStorage, ShipStorage uploadingShipStorage) {
        boolean result = false;
        boolean IsShipLocked = uploadingShipStorage.takeLockForOtherShip();
        if (IsShipLocked) {
            try {
                if (loadingShipStorage.getCountRequestedContainers() <= uploadingShipStorage.getCountRequestedContainers()) {
                    loadingShipStorage.addContainer(uploadingShipStorage.getContainer(loadingShipStorage.getCountRequestedContainers()));
                    logger.info("Ship " + loadingShipStorage.getIdShip() + " loaded all containers from " + uploadingShipStorage.getIdShip());
                    result = true;
                } else {
                    loadingShipStorage.addContainer(uploadingShipStorage.getContainer(uploadingShipStorage.getCountRequestedContainers()));
                    logger.info("Ship " + loadingShipStorage.getIdShip() + " loaded only part containers from " + uploadingShipStorage.getIdShip());

                }
            } finally {
                uploadingShipStorage.giveLock();
            }
            return result;

        } else {
            return result;
        }
    }

    public boolean addContainerToOtherShip(ShipStorage shipStorage) {
        boolean result = false;
        lockOfLoadingStorage.lock();
        try {
            if (!queueLoadingShipStorage.isEmpty()) {
                ShipStorage loadingShipStorage = queueLoadingShipStorage.poll();
                logger.info("Ship " + shipStorage.getIdShip() + " get ship " + loadingShipStorage.getIdShip() + " for uploading");
                result = doMoveToOtherShip(shipStorage, loadingShipStorage);
                queueLoadingShipStorage.add(loadingShipStorage);
            } else {
                logger.warn("Ship " + shipStorage.getIdShip() + " doesn't get ship for uploading");
            }
        } finally {
            lockOfLoadingStorage.unlock();
        }
        return result;
    }

    private boolean doMoveToOtherShip(ShipStorage uploadingShipStorage, ShipStorage loadingShipStorage) {
        boolean result = false;
        boolean IsShipLocked = loadingShipStorage.takeLockForOtherShip();
        if (IsShipLocked) {
            try {
                if (loadingShipStorage.getCountRequestedContainers() >= uploadingShipStorage.getCountRequestedContainers()) {
                    loadingShipStorage.addContainer(uploadingShipStorage.getContainer(uploadingShipStorage.getCountRequestedContainers()));
                    logger.info("Ship " + uploadingShipStorage.getIdShip() + " uploaded all containers to ship " + loadingShipStorage.getIdShip());
                    result = true;
                } else {
                    loadingShipStorage.addContainer(uploadingShipStorage.getContainer(loadingShipStorage.getCountRequestedContainers()));
                    logger.info("Ship " + uploadingShipStorage.getIdShip() + " uploaded only part of containers to ship" + loadingShipStorage.getIdShip());

                }
            } finally {
                loadingShipStorage.giveLock();
            }
            return result;

        } else {
            return result;
        }
    }

}



