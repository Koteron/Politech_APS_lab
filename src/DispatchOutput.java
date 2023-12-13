import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class DispatchOutput {
    private static final double SENDING_TIME_DIVIDER = 10000;
    private final ArrayBlockingQueue<Request> inputQueue;
    private final Buffer buffer;
    private final ArrayList<Device> deviceArray;
    private double sendingTime = 999999.0;

    public DispatchOutput(ArrayList<Device> dev, Buffer buf, int queueSize) {
        buffer = buf;
        deviceArray = dev;
        inputQueue = new ArrayBlockingQueue<>(queueSize);
    }

    public ArrayList<Device> getDeviceArray() {
        return deviceArray;
    }

    public double getSendingTime() {
        return sendingTime;
    }

    public boolean getRequestFromBuffer(double currentTime) {
        sendingTime = currentTime + Math.random() / SENDING_TIME_DIVIDER;
        Request req = buffer.getRequest(currentTime);
        if (req == null) {
            return false;
        }
        return inputQueue.add(req);
    }

    public boolean isAnyDeviceRunning() {
        for (var device : deviceArray) {
            if (device.isRunning()) {
                return true;
            }
        }
        return false;
    }

    public boolean isQueueFull() {
        return inputQueue.remainingCapacity() == 0;
    }

    public boolean isQueueEmpty() {
        return inputQueue.isEmpty();
    }

    public Request getNextSendingRequest() {return inputQueue.peek();}
    public Device assignRequestToDevice(double currentTime)
    {
        int deviceNum = 0;
        for (; deviceNum < deviceArray.size(); ++deviceNum)
        {
            if (!deviceArray.get(deviceNum).isRunning())
            {
                break;
            }
        }
        if (deviceNum < deviceArray.size() && !deviceArray.get(deviceNum).isRunning())
        {
            Request req = inputQueue.peek();
            if (req != null)
            {
                if (deviceArray.get(deviceNum).processRequest(req, currentTime)) //chosen device add
                {
                    inputQueue.poll();
                    sendingTime = 99999.0;
                    return deviceArray.get(deviceNum);
                }
            }
        }
        return null;
    }
}
