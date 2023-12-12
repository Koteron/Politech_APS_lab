import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Controller
{
    private final List<Source> sources;
    private int requestAmount;
    private final DispatchOutput dispatchOutput;
    private final Buffer buffer;
    private final List<Request> processedRequests;
    private double currentTime = 0.0;
    private final PriorityQueue<Source> sourceQueue;
    private final PriorityQueue<Source> sourceSendingQueue;
    private final PriorityQueue<Device> deviceQueue;
    private int stepNumber = 0;

    public Controller(List<Source> soursarr, int reqAmount, Buffer buf, DispatchOutput dispatchOut, double startTime)
    {
        currentTime = startTime;
        sources = soursarr;
        requestAmount = reqAmount;
        buffer = buf;
        dispatchOutput = dispatchOut;
        processedRequests = new ArrayList<>();
        sourceQueue = new PriorityQueue<>(sources.size(), Comparator.comparingDouble(
                Source::getNextGenerationTime));
        sourceQueue.addAll(soursarr);
        sourceSendingQueue = new PriorityQueue<>(sources.size(), Comparator.comparingDouble(
                Source::getSendingTime));
        sourceSendingQueue.addAll(soursarr);
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
        String format = "| %-15d | %-15f | %-15d | %-15d |%n";
        System.out.format("| %-15s | %-15s | %-15s | %-15s |%n", "SourceNumber",  "Time",   "RequestAmount",   "RejectedAmount");
        for (Source source : sources)
        {
            System.out.format(format, source.getSourceNumber(),
                    source.getLastGenerationTime(),
                    source.getRequestAmount(),
                    source.getRejectedRequestAmount());
        }
        System.out.println("\n");

        // Display buffer state
        var bufferState = buffer.getState(currentTime);
        System.out.println("Buffer:\n");
        System.out.format("| %-15s | %-15s | %-15s | %-15s |%n", "Position",  "Time",   "SourceNumber",   "RequestNumber");
        for (var quartet : bufferState)
        {
            System.out.format(format, quartet.getValue0(),
                    quartet.getValue1(),
                    quartet.getValue2(),
                    quartet.getValue3());
        }
        System.out.println("\n");

        // Display DispatchOutput
        if (!dispatchOutput.isQueueEmpty()) {
            System.out.println("DispatchOutput Next Request To Send: SourceNumber " + dispatchOutput.getNextSendingRequest().getSourceNumber() + " " +
                    "RequestNumber " + dispatchOutput.getNextSendingRequest().getRequestNumber());
        }
        else
        {
            System.out.println("DispatchOutput: Empty");
        }
        System.out.println("\n");

        // Display devices state
        System.out.println("Devices:\n");
        format = "| %-15d | %-15f | %-15s |%n";
        System.out.format("| %-15s | %-15s | %-15s |%n", "DeviceNumber",  "Time", "State");
        for (var device : dispatchOutput.getDeviceArray())
        {
            System.out.format(format, device.getDeviceNumber(),
                    device.getLastEventTime(),
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
                source.generateRequest(currentTime);
                sourceQueue.add(source);
                displayStepStats("Source №" + source.getSourceNumber() +
                        " Generated Request №" + source.getLastRequestNumber());
                sourceSendingQueue.poll();
                sourceSendingQueue.add(source);
            }

            // Sending a Request to Buffer
            source = sourceSendingQueue.peek();
            if (currentTime >= source.getSendingTime() && !source.hasRequest())
            {
                sourceSendingQueue.poll();
                int reqNum = source.getLastRequestNumber();
                --requestAmount;
                if (!source.sendRequest(currentTime))
                {
                    source.increaseRejected();
                    displayStepStats("Rejected Request №" + reqNum);
                }
                else
                {
                    displayStepStats("Sent Request №" + reqNum +" To Buffer");
                }
                sourceSendingQueue.add(source);
            }

            // Sending a Request from Buffer to DispatchOutput
            if (currentTime >= buffer.getSendingTime() && !buffer.isEmpty() && !dispatchOutput.isQueueFull())
            {
                dispatchOutput.getRequestFromBuffer(currentTime);
                displayStepStats("Sent Request №"+ buffer.getLastSentRequestNumber() +" To DispatchOutput");
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
        System.out.println("Overall rejection probability: " + overallRejected/overallRequestAmount + "\n");

        // Displaying source characteristics
        System.out.println("Source characteristics:\n");
        System.out.format("| %-15s | %-15s | %-15s | %-15s | %-15s | %-15s | %-20s | %-20s |%n",
                "SourceNumber", "RequestAmount", "RejectionProb", "AvgTimeInSystem", "AvgBufferTime", "AvgProcTime",
                "BufferTimeDispersion", "ProcTimeDispersion");
        String format = "| %-15d | %-15d | %-15f | %-15f | %-15f | %-15f | %-20f | %-20f |%n";
        for (int i = 0; i < sources.size(); ++i)
        {
            var source = sources.get(i);
            System.out.format(format, source.getSourceNumber(),
                    source.getRequestAmount(),
                    (double)source.getRejectedRequestAmount() / (double)source.getRequestAmount(),
                    resultArray.get(i).get(0),
                    resultArray.get(i).get(1),
                    resultArray.get(i).get(2),
                    resultArray.get(i).get(3),
                    resultArray.get(i).get(4));
        }


        // Evaluating and displaying device coefficients
        System.out.println("\n\nDevice usage coefficients:\n");
        System.out.format("| %-15s | %-17s |%n", "DeviceNumber", "UsageCoefficient");
        format = "| %-15d | %-17f |%n";
        for (var device : dispatchOutput.getDeviceArray())
        {
            System.out.format(format, device.getDeviceNumber(),
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
                source.generateRequest(currentTime);
                sourceQueue.add(source);
                sourceSendingQueue.remove(source);
                sourceSendingQueue.add(source);
            }

            // Sending a Request to Buffer
            source = sourceSendingQueue.peek();
            if (currentTime >= source.getSendingTime() && !source.hasRequest())
            {
                sourceSendingQueue.poll();
                --requestAmount;
                if (!source.sendRequest(currentTime))
                {
                    source.increaseRejected();
                }
                sourceSendingQueue.add(source);
            }

            // Sending a Request from Buffer to DispatchOutput
            if (currentTime >= buffer.getSendingTime() && !buffer.isEmpty() && !dispatchOutput.isQueueFull())
            {
                dispatchOutput.getRequestFromBuffer(currentTime);
                //dispatchOutput.setSendingTime(currentTime + Math.random() / 10);
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
