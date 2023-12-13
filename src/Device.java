import java.util.Objects;

public class Device
{
    private static final double TIME_DISTRIBUTION_INTENSITY = 0.03;
    private final int deviceNumber;
    private boolean isRunning = false;
    private Request processingRequest = null;
    private double overallWorkTime;
    private double lastEventTime;
    private double processingEndTime = 999999.0;

    public Device(int deviceNum)
    {
        deviceNumber = deviceNum;
    }
    public boolean isRunning()
    {
        return isRunning;
    }
    public int getDeviceNumber()
    {
        return deviceNumber;
    }
    public boolean processRequest(Request req, double currentTime)
    {
        if (isRunning)
        {
            return false;
        }

        double y = 1 - Math.random();
        if (y == 0) {y = 0.0000001;}
        if (y == 1) {y = 0.9999999;}
        processingEndTime = currentTime + (-1/TIME_DISTRIBUTION_INTENSITY)*Math.log(y);

        lastEventTime = currentTime;
        req.setProcessingStartTime(currentTime);
        processingRequest = req;
        isRunning = true;
        return true;
    }
    public double getProcessingEndTime()
    {
        return processingEndTime;
    }
    public Request endProcessing(double currentTime)
    {
        isRunning = false;
        overallWorkTime += currentTime - lastEventTime;
        lastEventTime = currentTime;
        processingEndTime = 999999.0;
        processingRequest.setProcessingEndTime(currentTime);
        Request req = processingRequest;
        processingRequest = null;
        return req;
    }

    public double getLastEventTime() {
        return lastEventTime;
    }

    public double getOverallWorkTime() {
        return overallWorkTime;
    }

    public int getProcessingRequestNumber(){
        if (processingRequest != null ) {
        return processingRequest.getRequestNumber();
        }
        return 0;
    }
    public int getProcessingRequestSourceNumber(){
        if (processingRequest != null ) {
            return processingRequest.getSourceNumber();
        }
        return 0;
    }
}
