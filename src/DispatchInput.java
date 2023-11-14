import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class DispatchInput
{
    private final Queue<Request> inputQueue;
    private final Buffer buffer;
    private double sendingTime = 999999.0;
    public DispatchInput(Buffer buf, int queueSize)
    {
        buffer = buf;
        inputQueue = new ArrayBlockingQueue<Request>(queueSize);
    }
    public boolean queueRequest(Request req)
    {
        try
        {
            return inputQueue.add(req);
        }
        catch (IllegalStateException e)
        {
            return false;
        }
    }
    public boolean isQueueEmpty() {return inputQueue.isEmpty();}
    public double getSendingTime() { return sendingTime; }
    public void setSendingTime(double newValue) { sendingTime = newValue; }
    public boolean sendRequestToBuffer(double currentTime)
    {
        if (inputQueue.peek() != null)
        {
            return buffer.addRequest(inputQueue.poll(), currentTime);
        }
        else
        {
            return false;
        }
    }
}
