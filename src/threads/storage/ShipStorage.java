package threads.storage;

import java.util.ArrayList;
import java.util.List;


public class ShipStorage extends Storage {
    private int countRequestedContainers;
    private final int idShip;


    public ShipStorage(int capacity, int idShip) {
        super(capacity);
        this.idShip = idShip;
    }

    public int getIdShip() {
        return idShip;
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
