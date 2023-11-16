import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class Main
{
    private final static int BUFFER_SIZE = 5,
            DISPATCH_INPUT_QUEUE_SIZE = 15,
            DISPATCH_OUTPUT_QUEUE_SIZE = 1,
            REQUEST_AMOUNT = 151;
    public static void main(String[] args)
    {
        ArrayList<Source> sources = new ArrayList<>();
        ArrayList<Device> devices = new ArrayList<>();
        Buffer buffer = new Buffer(BUFFER_SIZE);
        DispatchInput dispatchInput = new DispatchInput(buffer,DISPATCH_INPUT_QUEUE_SIZE);
        DispatchOutput dispatchOutput = new DispatchOutput(devices, buffer, DISPATCH_OUTPUT_QUEUE_SIZE);

        sources.add(new Source(1, dispatchInput, 0.0));
        sources.add(new Source(2, dispatchInput, 0.0));
        sources.add(new Source(3, dispatchInput, 0.0));
        devices.add(new Device(1));
        devices.add(new Device(2));
        devices.add(new Device(3));
        devices.add(new Device(4));
        devices.add(new Device(5));
        devices.add(new Device(6));
        devices.add(new Device(7));

        Controller controller = new Controller(sources, REQUEST_AMOUNT, dispatchInput, buffer, dispatchOutput);
        Scanner in = new Scanner(System.in);
        System.out.println("Enter 1 to start Step Mode and 2 to start Auto Mode: ");
        while (true)
        {
            String input = in.nextLine();
            if (Objects.equals(input, "1"))
            {
                controller.startStepMode();
                break;
            }
            else if (Objects.equals(input, "2"))
            {
                controller.startAutoMode();
                break;
            }
            else
            {
                System.out.println("Unexpected Input! Enter 1 or 2: ");
            }
        }
    }
}