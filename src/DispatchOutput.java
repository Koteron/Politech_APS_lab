import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class DispatchOutput
{
    private ArrayBlockingQueue<Request> inputQueue;
    private Buffer buffer;
    private ArrayList<Device> deviceArray;
    private double sendingTime = 999999.0;
    public DispatchOutput(ArrayList<Device> dev, Buffer buf, int queueSize)
    {
        buffer = buf;
        deviceArray = dev;
        inputQueue = new ArrayBlockingQueue<Request>(queueSize);
    }
    public ArrayList<Device> getDeviceArray()
    {
        return deviceArray;
    }
    public double getSendingTime() { return sendingTime; }
    public void setSendingTime(double newValue) { sendingTime = newValue; }
    public boolean getRequestFromBuffer(double currentTime)
    {
        Request req = buffer.getRequest(currentTime);
        if (req == null)
        {
            return false;
        }
        return inputQueue.add(req);
    }
    public boolean isAnyDeviceRunning()
    {
        for (var device : deviceArray)
        {
            if (device.isRunning())
            {
                return true;
            }
        }
        return false;
    }
    public boolean queueIsFull()
    {
        return inputQueue.remainingCapacity() == 0;
    }
    public boolean queueIsEmpty()
    {
        return inputQueue.isEmpty();
    }
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
                    return deviceArray.get(deviceNum);
                }
            }
        }
        return null;
    }
}
