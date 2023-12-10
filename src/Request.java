public class Request
{
    private final int requestNumber, sourceNumber;
    private static int requestNum = 0;
    private double bufferStartTime;
    private double bufferEndTime;
    private double processingStartTime;
    private double processingEndTime;

    public Request(int sourceNum)
    {
        requestNumber = requestNum++;
        sourceNumber = sourceNum;
    }

    public int getSourceNumber()
    {
        return sourceNumber;
    }

    public int getRequestNumber()
    {
        return requestNumber;
    }

    public double getBufferTime()
    {
        return bufferEndTime - bufferStartTime;
    }

    public double getInstantBufferTime(double currentTime) { return currentTime - bufferStartTime; }

    public double getProcessingTime() { return processingEndTime - processingStartTime; }

    public void setProcessingStartTime(double currentTime)
    {
        processingStartTime = currentTime;
    }
    public void setProcessingEndTime(double currentTime)
    {
        processingEndTime = currentTime;
    }

    public void setBufferStartTime(double currentTime)
    {
        bufferStartTime = currentTime;
    }

    public void setBufferEndTime(double currentTime)
    {
        bufferEndTime = currentTime;
    }
    public static void clearRequestNumber() {requestNum = 0;}
}
