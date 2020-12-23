package threads.storage;

import java.util.ArrayList;
import java.util.List;


public class Storage {
    private int capacity;
    List<Container> containers;


    public Storage(int capacity) {
        this.capacity = capacity;
        containers = new ArrayList<>(capacity);
    }


    public int getRealCapacity() {
        return containers.size();
    }

    public void setContainers(List<Container> containers){
        this.containers = containers;
    }

    public void addContainer(List<Container> containers) {
        this.containers.addAll(containers);
    }

    public List<Container> getContainer(int numberOfContainers) {
        List<Container> gettingContainers = new ArrayList<>(containers.subList(0, numberOfContainers));
        containers.removeAll(gettingContainers);
        return gettingContainers;
    }

    public Integer getFreeSize() {
        return capacity - containers.size();
    }


}
