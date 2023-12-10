import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Controller
{
    private final List<Source> sources;
    private int requestAmount;
    private final DispatchInput dispatchInput;
    private final DispatchOutput dispatchOutput;
    private final Buffer buffer;
    private final List<Request> processedRequests;
    private double currentTime = 0.0;
    private final PriorityQueue<Source> sourceQueue;
    private final PriorityQueue<Device> deviceQueue;
    private int stepNumber = 0;

    public Controller(List<Source> soursarr, int reqAmount, DispatchInput dispatchIn, Buffer buf, DispatchOutput dispatchOut, double startTime)
    {
        currentTime = startTime;
        sources = soursarr;
        requestAmount = reqAmount;
        dispatchInput = dispatchIn;
        buffer = buf;
        dispatchOutput = dispatchOut;
        processedRequests = new ArrayList<>();
        sourceQueue = new PriorityQueue<>(sources.size(), Comparator.comparingDouble(
                Source::getNextGenerationTime));
        sourceQueue.addAll(soursarr);
        deviceQueue = new PriorityQueue<>(sources.size(), Comparator.comparingDouble(
                Device::getProcessingEndTime));
        deviceQueue.addAll(dispatchOut.getDeviceArray());
    }
    public void displayStepStats(String stepEvent)
    {
        ++stepNumber;
        System.out.println("______________________________________________");
        System.out.println("Step№" + stepNumber + "  Current time: " + currentTime + "  Event: " + stepEvent + "\n");

        // Display sources state
        System.out.println("Sources:\n");
        System.out.println("SourceNumber  Time   RequestAmount   RejectedAmount");
        for (Source source : sources)
        {
            System.out.println(source.getSourceNumber() + "     " +
                    source.getLastGenerationTime() + "     " +
                    source.getRequestAmount() + "     " +
                    source.getRejectedRequestAmount());
        }
        System.out.println("\n");

        // Display buffer state
        var bufferState = buffer.getState(currentTime);
        System.out.println("Buffer:\n");
        System.out.println("Position  Time   SourceNumber   RequestNumber");
        for (var quartet : bufferState)
        {
            System.out.println(quartet.getValue0() + "     " +
                    quartet.getValue1() + "     " +
                    quartet.getValue2() + "     " +
                    quartet.getValue3());
        }
        System.out.println("\n");

        // Display devices state
        System.out.println("Devices:\n");
        System.out.println("DeviceNumber  Time   State");
        for (var device : dispatchOutput.getDeviceArray())
        {
            System.out.println(device.getDeviceNumber() + "     " +
                    device.getLastEventTime() + "     " +
                    ((device.isRunning()) ? "Running" : "Waiting"));
        }
        System.out.println("Press Enter key to continue...");
        try
        {
            System.in.read();
        }
        catch(Exception ignored)
        {}
    }
    public void startStepMode()
    {
        Request.clearRequestNumber();
        while (requestAmount > 0 || dispatchOutput.isAnyDeviceRunning())
        {
            // Request generation and sending to DispatchInput
            // source always != null, because queue always has something in it
            Source source = sourceQueue.peek();
            if (currentTime >= source.getNextGenerationTime() && requestAmount > 0)
            {
                sourceQueue.poll();
                source.sendRequest(currentTime);
                sourceQueue.add(source);
                dispatchInput.setSendingTime(currentTime + Math.random() / 10);
                displayStepStats("Source №" + source.getSourceNumber() +
                        " Generated Request №" + source.getLastRequestNumber());
            }

            // Sending a Request to Buffer
            if (currentTime >= dispatchInput.getSendingTime() && !dispatchInput.isQueueEmpty())
            {
                --requestAmount;
                if (!dispatchInput.sendRequestToBuffer(currentTime))
                {
                    source.increaseRejected();
                    displayStepStats("Rejected Request №" + dispatchInput.getLastSentRequestNumber());
                }
                else
                {
                    buffer.setSendingTime(currentTime + Math.random() / 10);
                    displayStepStats("Sent Request №" + dispatchInput.getLastSentRequestNumber() +" To Buffer");
                }
            }

            // Sending a Request from Buffer to DispatchOutput
            if (currentTime >= buffer.getSendingTime() && !buffer.isEmpty() && !dispatchOutput.isQueueFull())
            {
                dispatchOutput.getRequestFromBuffer(currentTime);
                dispatchOutput.setSendingTime(currentTime + Math.random() / 10);
                displayStepStats("Sent Request "+ buffer.getLastSentRequestNumber() +" To DispatchOutput");
            }

            // Assigning a Device to process a Request
            if (currentTime >= dispatchOutput.getSendingTime() && !dispatchOutput.isQueueEmpty())
            {
                Device device = dispatchOutput.assignRequestToDevice(currentTime);
                if (device != null)
                {
                    displayStepStats("Assigned Request № " + device.getProcessingRequestNumber() +
                            " To Device №" + device.getDeviceNumber());
                }
            }

            // End processing a Request by a Device
            // device always != null, because queue always has something in it
            if (currentTime >= deviceQueue.peek().getProcessingEndTime())
            {
                Device device = deviceQueue.poll();
                displayStepStats("Device №"+ device.getDeviceNumber() +
                        " Is Done Processing Request №" + device.getProcessingRequestNumber());
                processedRequests.add(device.endProcessing(currentTime));
                deviceQueue.add(device);
            }

            currentTime += 0.001;
        }
    }
    public void displayAutoStats()
    {
        // Evaluating request time characteristics for each source
        ArrayList<ArrayList<Double>> resultArray = new ArrayList<>();
        for (int i = 0; i < sources.size(); ++i)
        {
            var source = sources.get(i);
            double overallSystemTime = 0.0;
            double overallBufferTime = 0.0;
            double overallProcessingTime = 0.0;
            double processingTimeDispersion = 0.0;
            double bufferTimeDispersion = 0.0;
            resultArray.add(new ArrayList<>());
            for (var request : processedRequests)
            {
                if (request.getSourceNumber() == source.getSourceNumber())
                {
                    overallSystemTime += request.getProcessingTime() + request.getBufferTime();
                    overallBufferTime += request.getBufferTime();
                    overallProcessingTime += request.getProcessingTime();
                }
            }
            double averageSystemTime = 0.0;
            double averageBufferTime = 0.0;
            double averageProcessingTime = 0.0;
            if (source.getAcceptedRequestAmount() != 0)
            {
                averageSystemTime = overallSystemTime/source.getAcceptedRequestAmount() ;
                averageBufferTime =  overallBufferTime/source.getAcceptedRequestAmount();
                averageProcessingTime = overallProcessingTime/source.getAcceptedRequestAmount();
            }
            resultArray.get(i).add(averageSystemTime);
            resultArray.get(i).add(averageBufferTime);
            resultArray.get(i).add(averageProcessingTime);
            for (var request : processedRequests)
            {
                if (request.getSourceNumber() == source.getSourceNumber())
                {
                    processingTimeDispersion = (averageProcessingTime - request.getProcessingTime()) *
                            (averageProcessingTime - request.getProcessingTime());
                    bufferTimeDispersion = (averageBufferTime - request.getBufferTime()) *
                            (averageBufferTime - request.getBufferTime());
                }
            }
            resultArray.get(i).add(processingTimeDispersion);
            resultArray.get(i).add(bufferTimeDispersion);
        }

        double overallRejected = 0.0;
        double overallRequestAmount = 0.0;
        for (var source : sources)
        {
            overallRejected += source.getRejectedRequestAmount();
            overallRequestAmount += source.getRequestAmount();
        }
        System.out.println("Overall rejection probability:" + overallRejected/overallRequestAmount + "\n");

        // Displaying source characteristics
        System.out.println("Source characteristics:\n");
        System.out.println("SourceNumber RequestAmount RejectionProb AvgTimeInSystem AvgBufferTime AvgProcTime " +
                "BufferTimeDispersion ProcTimeDispersion");
        for (int i = 0; i < sources.size(); ++i)
        {
            var source = sources.get(i);
            System.out.println(source.getSourceNumber() + "   " +
                    source.getRequestAmount() + "   " +
                    (double)source.getRejectedRequestAmount() / (double)source.getRequestAmount() + "   " +
                    resultArray.get(i).get(0) + "   " +
                    resultArray.get(i).get(1) + "   " +
                    resultArray.get(i).get(2) + "   " +
                    resultArray.get(i).get(3) + "   " +
                    resultArray.get(i).get(4));
        }


        // Evaluating and displaying device coefficients
        System.out.println("\n\nDevice usage coefficients:\n");
        System.out.println("DeviceNumber UsageCoefficient");
        for (var device : dispatchOutput.getDeviceArray())
        {
            System.out.println(device.getDeviceNumber() + "    " +
                    device.getOverallWorkTime() / currentTime);
        }
    }
    public double startAutoMode()
    {
        Request.clearRequestNumber();
        while (requestAmount > 0 || dispatchOutput.isAnyDeviceRunning())
        {
            // Request generation and sending to DispatchInput
            Source source = sourceQueue.peek();
            if (currentTime >= source.getNextGenerationTime() && requestAmount > 0)
            {
                sourceQueue.poll();
                source.sendRequest(currentTime);
                sourceQueue.add(source);
                dispatchInput.setSendingTime(currentTime + Math.random() / 10);
            }

            // Sending a Request to Buffer
            if (currentTime >= dispatchInput.getSendingTime() && !dispatchInput.isQueueEmpty())
            {
                --requestAmount;
                if (!dispatchInput.sendRequestToBuffer(currentTime))
                {
                    source.increaseRejected();
                }
                else
                {
                    buffer.setSendingTime(currentTime + Math.random() / 10);
                }
            }

            // Sending a Request from Buffer to DispatchOutput
            if (currentTime >= buffer.getSendingTime() && !buffer.isEmpty() && !dispatchOutput.isQueueFull())
            {
                dispatchOutput.getRequestFromBuffer(currentTime);
                dispatchOutput.setSendingTime(currentTime + Math.random() / 10);
            }

            // Assigning a Device to process a Request
            if (currentTime >= dispatchOutput.getSendingTime() && !dispatchOutput.isQueueEmpty())
            {
                dispatchOutput.assignRequestToDevice(currentTime);
            }

            // End processing a Request by a Device
            if (currentTime >= deviceQueue.peek().getProcessingEndTime())
            {
                Device device = deviceQueue.poll();
                processedRequests.add(device.endProcessing(currentTime));
                deviceQueue.add(device);
            }
            currentTime += 0.001;
        }
        double overallRejected = 0.0;
        double overallRequestAmount = 0.0;
        for (var source : sources)
        {
            overallRejected += source.getRejectedRequestAmount();
            overallRequestAmount += source.getRequestAmount();
        }
        return overallRejected/overallRequestAmount;
    }
}
