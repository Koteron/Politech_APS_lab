public class Request
{
    private final int requestNumber, sourceNumber, userID;
    private final double generationTime;
    private double bufferStartTime;
    private double bufferEndTime;
    private double processingStartTime;
    private double processingEndTime;

    public Request(int usrID, int requestNum, int sourceNum, double currentTime)
    {
        requestNumber = requestNum;
        userID = usrID;
        sourceNumber = sourceNum;
        generationTime = currentTime;
    }

    public int getSourceNumber()
    {
        return sourceNumber;
    }

    public int getRequestNumber()
    {
        return requestNumber;
    }

    public int getUserID()
    {
        return userID;
    }

    public double getBufferTime()
    {
        return bufferEndTime - bufferStartTime;
    }

    public double getInstantBufferTime(double currentTime) { return currentTime - bufferStartTime; }


    public double getProcessingTime() { return processingEndTime - processingStartTime; }

    public double getGenerationTime()
    {
        return generationTime;
    }

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
}
