public class Source
{
    private static final double MAX_GENERATION_TIME = 10.0;
    private static final double MIN_GENERATION_TIME = 2.0; // MUST BE GREATER THAN SENDING TIME
    private static final double SENDING_TIME_DIVIDER = 10;
    private int sourceNumber = -1;
    private int rejectedRequestAmount = 0;
    private int requestAmount = 0;
    private double nextGenerationTime;
    private int lastRequestNumber = -1;
    private double sendingTime = 99999.0;
    private final Buffer buffer;
    private Request generatedRequest;
    private double lastGenerationTime = 0.0;

    public Source(int sourceNum, Buffer buf, double currentTime)
    {
        nextGenerationTime = calculateNextGenerationTime(currentTime);
        sourceNumber = sourceNum;
        buffer = buf;
    }
    public int getSourceNumber()
    {
        return sourceNumber;
    }
    public int getRequestAmount()
    {
        return requestAmount;
    }
    public int getAcceptedRequestAmount()
    {
        return requestAmount - rejectedRequestAmount;
    }
    public int getRejectedRequestAmount()
    {
        return rejectedRequestAmount;
    }
    public void increaseRejected() { ++rejectedRequestAmount; }
    public double calculateNextGenerationTime(double currentTime)
    {
        nextGenerationTime = currentTime + Math.random()*(MAX_GENERATION_TIME-MIN_GENERATION_TIME)+MIN_GENERATION_TIME;
        return nextGenerationTime;
    }
    public double calculateSendingTime(double currentTime)
    {
        sendingTime = currentTime + Math.random() / SENDING_TIME_DIVIDER;
        return nextGenerationTime;
    }
    public double getNextGenerationTime() { return nextGenerationTime; }
    public double getLastGenerationTime() { return lastGenerationTime; }
    public boolean hasRequest() {return generatedRequest == null;}
    public boolean sendRequest(double currentTime)
    {
        sendingTime = 99999.0;
        return generatedRequest != null && buffer.addRequest(generatedRequest, currentTime);
    }

    public void generateRequest(double currentTime)
    {
        calculateNextGenerationTime(currentTime);
        calculateSendingTime(currentTime);
        generatedRequest = new Request(sourceNumber);
        lastRequestNumber = generatedRequest.getRequestNumber();
        lastGenerationTime = currentTime;
        ++requestAmount;
    }

    public int getLastRequestNumber() {
        return lastRequestNumber;
    }

    public double getSendingTime() {
        return sendingTime;
    }
}
