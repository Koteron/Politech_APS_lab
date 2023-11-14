public class Device
{
    private final double MAX_PROCESSING_TIME = 100.0;
    private final double MIN_PROCESSING_TIME = 50.0;
    private final double TIME_DISTRIBUTION_INTENSITY = 0.05;
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

        double y = Math.random();
        if (y == 0) {y = 0.0000001;}
        //else if (y == 1) {y = 0.9999999;}
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
        overallWorkTime += currentTime - processingEndTime;
        lastEventTime = processingEndTime;
        processingEndTime = 999999.0;
        processingRequest.setProcessingEndTime(currentTime);
        Request req = processingRequest;
        processingRequest = null;
        return req;
    }

    public double getLastEventTime() {
        return lastEventTime;
    }

    public void setLastEventTime(double lastEventTime) {
        this.lastEventTime = lastEventTime;
    }

    public double getOverallWorkTime() {
        return overallWorkTime;
    }
}