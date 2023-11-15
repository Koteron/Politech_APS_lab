import java.util.ArrayList;
import java.util.Scanner;

public class Main
{
    public static void main(String[] args)
    {
        ArrayList<Source> sources = new ArrayList<>();
        ArrayList<Device> devices = new ArrayList<>();

        Buffer buffer = new Buffer(5);
        DispatchInput dispatchInput = new DispatchInput(buffer,15);
        DispatchOutput dispatchOutput = new DispatchOutput(devices, buffer, 1);

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

        Controller controller = new Controller(sources, 151, dispatchInput, buffer, dispatchOutput);
        Scanner in = new Scanner(System.in);
        System.out.println("Enter 1 to start Step Mode and 2 to start Auto Mode: ");
        while (true)
        {
            try
            {
                int input = in.nextInt();
                if (input == 1)
                {
                    controller.startStepMode();
                    break;
                }
                else if (input == 2)
                {
                    controller.startAutoMode();
                    break;
                }
                else
                {
                    System.out.println("Wrong number was entered!");
                }
            }
            catch (Exception e)
            {
                in.nextLine();
                System.out.println("Not an integer was entered!");
            }
        }
    }
}