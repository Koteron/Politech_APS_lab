import java.util.ArrayList;

import org.javatuples.Quartet;
import java.util.List;


public class Buffer
{
    private final List<Request> requestArray;
    private final int bufferSize;
    private double sendingTime = 999999.0;
    public Buffer(int bufSize)
    {
        bufferSize = bufSize;
        requestArray = new ArrayList<Request>();
        for (int i = 0; i < bufSize; ++i)
        {
            requestArray.add(null);
        }
    }
    public double getSendingTime() { return sendingTime; }
    public void setSendingTime(double newValue) { sendingTime = newValue; }
    public Request getRequest(double currentTime)
    {
        {

        }
        int sentRequestNumber = 0;
        for (int i = 1; i < bufferSize; ++i)
        {
            if (requestArray.get(sentRequestNumber) == null
                || requestArray.get(i) != null
                && (requestArray.get(i).getSourceNumber() < requestArray.get(i).getSourceNumber()
                    || requestArray.get(i).getSourceNumber() == requestArray.get(i).getSourceNumber()
                        && requestArray.get(i).getBufferTime() > requestArray.get(i).getBufferTime()))
            {
                sentRequestNumber = i;
            }
        }
        Request req = requestArray.get(sentRequestNumber);
        requestArray.set(sentRequestNumber, null);
        req.setBufferEndTime(currentTime);
        return req;
    }

    public boolean addRequest(Request req, double currentTime)
    {
        for (int i = 0; i < bufferSize; ++i)
        {
            if (requestArray.get(i) == null)
            {
                requestArray.set(i, req);
                req.setBufferStartTime(currentTime);
                return true;
            }
        }
        return false;
    }

    public boolean isFull()
    {
        for (int i = 0; i < bufferSize; ++i)
        {
            if (requestArray.get(i) == null)
            {
                return false;
            }
        }
        return true;
    }

    public boolean isEmpty()
    {
        for (Request request : requestArray) {
            if (request != null) {
                return false;
            }
        }
        return true;
    }

    public ArrayList<Quartet<Integer, Double, Integer, Integer>> getState(double currentTime)
    {
        ArrayList<Quartet<Integer, Double, Integer, Integer>> arr = new ArrayList<Quartet<Integer, Double, Integer, Integer>>();
        int i = 1;
        for (Request request : requestArray)
        {
            if (request == null)
            {
                arr.add(Quartet.with(i, 0.0, 0, 0));
            }
            else
            {
                arr.add(Quartet.with(i,
                        request.getInstantBufferTime(currentTime),
                        request.getSourceNumber(),
                        request.getRequestNumber()));
            }
            ++i;
        }
        return arr;
    }
}
