package Week5.protocol;

import Week5.client.IRoutingProtocol;
import Week5.client.LinkLayer;
import Week5.client.RoutingChallengeClient;
import Week5.client.RoutingChallengeClient.SimulationState;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
public class Program {
    // Change to your group number (e.g. use a student number)
    private static int groupId = 1880241;

    // Change to your group password (doesn't matter what it is,
    // as long as everyone in the group uses the same string)
    private static String password = "pass1212";

    // Change to your protocol implementation
    private static Class<? extends IRoutingProtocol> protocolImpl = OurProtocol.class;

    // Challenge server address
    private static String serverAddress = "netsys.student.utwente.nl";

    // Challenge server port
    private static int serverPort = 8002;



    // *                                                          *
    // **                                                        **
    // ***             DO NOT EDIT BELOW THIS LINE!             ***
    // ****                                                    ****
    // ************************************************************
    // ************************************************************

    public static void main(String[] args) {

        int group = groupId;

        if (args.length > 0) {
            group = Integer.parseInt(args[0]);
        }

        try {
            // Initialize communication with the emulation server
            RoutingChallengeClient client = new RoutingChallengeClient(serverAddress, serverPort, group, password);

            LinkLayer linkLayer = new LinkLayer(client);

            // Wait for cue to start simulation
            System.out.println("Press a key to start the simulation...");
            System.out.println("(Simulation will also be started automatically using the web interface, "
                    + "or if another client in the group issues the start command)");

            boolean startCommand = false;
            InputStream inputStream = new BufferedInputStream(System.in);
            while (!client.IsSimulationRunning() && client.getSimulationState() != SimulationState.Finished) {
                if (!startCommand && inputStream.available() > 0) {
                    client.RequestStart();
                    startCommand = true;
                }
                Thread.sleep(10);
            }

            System.out.println("Simulation started!");

            // Run the client and the protocol
            while (client.getSimulationState() != SimulationState.Finished) {

                // Wait until we start running a test
                while (client.getSimulationState() != SimulationState.TestRunning) {
                    if (client.getSimulationState() == SimulationState.Finished)
                        break;
                    Thread.sleep(1);
                }

                if (client.getSimulationState() != SimulationState.Finished) {

                    System.out.println("Running test " + client.getTestID() + "...");

                    // Create a new instance of the protocol
                    IRoutingProtocol protocol = createProtocol();
                    protocol.init(linkLayer);

                    // Pass the protocol to the challenge client
                    client.setRoutingProtocolAndTock(protocol);

                    while (client.getSimulationState() == SimulationState.TestRunning) {
                        //wait until finished
                        Thread.sleep(10);
                    }

                    System.out.println("Test completed.");

                    while (client.getSimulationState() == SimulationState.TestComplete) {
                        Thread.sleep(10);
                    }
                }
            }

            System.out.println("Simulation finished! Check your performance on the server web interface.");

        } catch (IOException e) {
            System.out.print("Could not start the client because: ");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("Operation interrupted");
            e.printStackTrace();
        }
    }


    private static IRoutingProtocol createProtocol() {
        try {
            return (IRoutingProtocol) protocolImpl.getConstructor(new Class[0])
                    .newInstance(new Object[0]);
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        return null;
    }
}
