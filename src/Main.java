import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class Main
{
    private final static int BUFFER_SIZE = 2,
            DISPATCH_OUTPUT_QUEUE_SIZE = 1,
            SOURCES_AMOUNT = 7,
            DEVICES_AMOUNT = 5;
    private static final double START_TIME = 0.0,
            STUDENT_COEFFICIENT = 1.643,
            ACCURACY = 0.1,
            DELTA = 0.1*0.1;
    private static int requestAmount = 151;
    public static Controller createController()
    {
        ArrayList<Source> sources = new ArrayList<>();
        ArrayList<Device> devices = new ArrayList<>();
        Buffer buffer = new Buffer(BUFFER_SIZE);
        DispatchOutput dispatchOutput = new DispatchOutput(devices, buffer, DISPATCH_OUTPUT_QUEUE_SIZE);

        for (int i = 1; i <= SOURCES_AMOUNT; ++i)
        {
            sources.add(new Source(i, buffer, START_TIME));
        }
        for(int i = 1; i <= DEVICES_AMOUNT; ++i)
        {
            devices.add(new Device(i));
        }

        return new Controller(sources, requestAmount, buffer, dispatchOutput, START_TIME);
    }
    public static void main(String[] args)
    {
        System.out.println("Evaluating request amount starting from: " + requestAmount);
        Controller controller = createController();
        double lastP = controller.startAutoMode();
        requestAmount = (int)Math.round((STUDENT_COEFFICIENT*STUDENT_COEFFICIENT*(1 - lastP))/(lastP*DELTA));
        controller = createController();
        double nextP = controller.startAutoMode();
        while (Math.abs(lastP-nextP) >= ACCURACY * lastP)
        {
            lastP = nextP;
            requestAmount = (int)Math.round((STUDENT_COEFFICIENT*STUDENT_COEFFICIENT*(1 - lastP))/(lastP*DELTA));
            controller = createController();
            nextP = controller.startAutoMode();
        }
        System.out.println("Evaluated optimal request amount: " + requestAmount);
        Scanner in = new Scanner(System.in);
        System.out.println("Enter:\n" +
                "1 - to start Step Mode\n" +
                "2 - to start Auto Mode\n" +
                "3 - to change request amount manually");

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
            else if (Objects.equals(input, "3"))
            {
                while (true)
                {
                    System.out.println("Enter new request amount:");
                    input = in.nextLine();
                    try
                    {
                        requestAmount = Integer.parseInt(input);
                        System.out.println("Request amount changed to " + requestAmount);
                        controller = createController();
                        controller.startAutoMode();
                        System.out.println("Enter:\n" +
                                "1 - to start Step Mode\n" +
                                "2 - to start Auto Mode\n" +
                                "3 - to change request amount manually");
                        break;
                    }
                    catch(NumberFormatException e)
                    {
                        System.out.println("Input is not an integer number!");
                    }
                }
            }
            else
            {
                System.out.println("Unexpected Input! Enter 1 or 2: ");
            }
        }
    }
}