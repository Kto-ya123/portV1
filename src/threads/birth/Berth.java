package threads.birth;

import org.apache.log4j.Logger;
import threads.storage.Container;
import threads.storage.ShipStorage;
import threads.storage.Storage;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class Berth{

    private static Logger logger = Logger.getLogger(Berth.class);

    private int id;
    private Storage portStorage;
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
        synchronized (queueUploadingShipStorage) {
            queueUploadingShipStorage.add(shipStorage);
        }

        if (shipStorage.getCountRequestedContainers() <= portStorage.getFreeSize()) {
            result = doMoveFromShip(shipStorage);
        }

        return result;
    }


    private boolean doMoveFromShip(ShipStorage shipStorage) {
        boolean result = false;

        synchronized (shipStorage) {
            synchronized (portStorage) {
                List<Container> containers = shipStorage.getContainer(shipStorage.getCountRequestedContainers());
                portStorage.addContainer(containers);
                result = true;
                queueUploadingShipStorage.remove(shipStorage);
            }
        }
        return result;
    }

    public boolean getContainersFromPort(ShipStorage shipStorage) {
        boolean result = false;

        synchronized (queueLoadingShipStorage) {
            queueLoadingShipStorage.add(shipStorage);
        }



        synchronized (portStorage){
            synchronized (shipStorage){
                if (shipStorage.getCountRequestedContainers() <= portStorage.getRealCapacity()) {
                    result = doMoveFromPort(shipStorage);
                }
            }

        }
        return result;
    }

    private boolean doMoveFromPort(ShipStorage shipStorage) {
        boolean result = false;

        synchronized (shipStorage){
            List<Container> containers = portStorage.getContainer(shipStorage.getCountRequestedContainers());
            shipStorage.addContainer(containers);
            result = true;
        }
        queueLoadingShipStorage.remove(shipStorage);

        return result;
    }

}



