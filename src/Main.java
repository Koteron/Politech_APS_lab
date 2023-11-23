import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class Main
{
    private final static int BUFFER_SIZE = 5,
            DISPATCH_INPUT_QUEUE_SIZE = 15,
            DISPATCH_OUTPUT_QUEUE_SIZE = 1,
            SOURCES_AMOUNT = 7,
            DEVICES_AMOUNT = 14;
    private static int requestAmount = 151;
    public static Controller createController()
    {
        ArrayList<Source> sources = new ArrayList<>();
        ArrayList<Device> devices = new ArrayList<>();
        Buffer buffer = new Buffer(BUFFER_SIZE);
        DispatchInput dispatchInput = new DispatchInput(buffer,DISPATCH_INPUT_QUEUE_SIZE);
        DispatchOutput dispatchOutput = new DispatchOutput(devices, buffer, DISPATCH_OUTPUT_QUEUE_SIZE);

        for (int i = 1; i <= SOURCES_AMOUNT; ++i)
        {
            sources.add(new Source(i, dispatchInput, 0.0));
        }
        for(int i = 0; i <= DEVICES_AMOUNT; ++i)
        {
            devices.add(new Device(i));
        }

        return new Controller(sources, requestAmount, dispatchInput, buffer, dispatchOutput);
    }
    public static void main(String[] args)
    {
        System.out.println("Evaluating request amount starting from: " + requestAmount);
        Controller controller = createController();
        double lastP = controller.startAutoMode();
        requestAmount = (int)Math.round((1.643*1.643*(1 - lastP))/(lastP*0.01));
        controller = createController();
        double nextP = controller.startAutoMode();
        while (Math.abs(lastP-nextP) >= 0.1 * lastP)
        {
            lastP = nextP;
            requestAmount = (int)Math.round((1.643*1.643*(1 - lastP))/(lastP*0.01));
            controller = createController();
            nextP = controller.startAutoMode();
        }
        System.out.println("Evaluated request amount: " + requestAmount);

        Scanner in = new Scanner(System.in);
        System.out.println("Enter 1 to start Step Mode and 2 to start Auto Mode: ");

        while (true)
        {
            String input = in.nextLine();
            if (Objects.equals(input, "1"))
            {
                controller = createController();
                controller.startStepMode();
                break;
            }
            else if (Objects.equals(input, "2"))
            {
                controller.displayAutoStats();
                break;
            }
            else
            {
                System.out.println("Unexpected Input! Enter 1 or 2: ");
            }
        }
    }
}