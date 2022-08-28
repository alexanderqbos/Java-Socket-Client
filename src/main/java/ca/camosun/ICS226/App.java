package ca.camosun.ICS226;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        Client client = new Client("localhost", 12345, "12345678");
        client.connect();
    }
}
