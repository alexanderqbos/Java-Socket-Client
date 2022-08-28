package ca.camosun.ICS226;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.NoSuchElementException;
import java.util.UUID;

/*
* Client class - Controller for Java Socket instance.
*/
public class Client {

  private String host; // host address to connect socket to.
  private int port; // port number to connect socket to.
  private String key; // key used for initial get message

  private final int KEY_SIZE = 8; // fixed key size value

  /**
   * Client(String host, int port, String key) - Client class constructor
   * @param host - Host address (URL/IPV4/IPV6) based on socket connection type
   * @param port - Port number for socket connection
   * @param key - Key for initial get request to connection
   */
  public Client(String host, int port, String key) {
    this.host = host;
    this.port = port;
    this.key = key;
  }

  /**
   * connect() - initiates the connections to the server.
   * This function initiates two thread classes.
   */
  public void connect() {
    send_message sndMsg = new send_message();
    sndMsg.start();
    get_message getMsg = new get_message();
    getMsg.start();
  }

  /**
   * get_by_key(String host, int port, String key) - updates key
   * @param host - Host address (URL/IPV4/IPV6) based on socket connection type
   * @param port - Port number for socket connection
   * @param key - Key for initial get request to connection
   * @return connection response of get
   */
  private String get_by_key(String host, int port, String key)
  {
    String response = "";
    try(
      Socket socket = new Socket(host, port);
      PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    )
    {
      String message = "GET" + key;
      out.println(message);
      String data = in.readLine();
      data = data.strip();
      if(data != "") 
      {
        System.out.println("Received: " + data.substring(0, data.length() - KEY_SIZE));
        this.key = data.substring(data.length() - KEY_SIZE, data.length());
      }
      response = data;
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    
    return response;
  }

  /**
   * send_message Class - extends Thread to run without blocking while waiting for input
   */
  class send_message extends Thread
  {
    /**
     * gen_new_key() - generates new key for end of message
     * @return 8 alphanumeric key.
     */
    private String gen_new_key()
    {
      String nKey = UUID.randomUUID().toString().toUpperCase();
      nKey = nKey.substring(0, KEY_SIZE);
      return nKey;
    }

    /**
     * @Override run() - Thread class method, executes code in function when start() is called.
     * Waits for user input and sends any input received before new line/carrige return.
     */
    @Override
    public void run()
    {
      // Scanner instantiated here due to System.in being closed when in while loop.
      Scanner stdIn = new Scanner(System.in);
      while(true)
      {
        try(
          Socket socket = new Socket(host, port);
          PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
          BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        )
        {
          String message = "";
          try {
              message = stdIn.nextLine();
          }
	        catch(NoSuchElementException e)
          {
            get_by_key(host, port, key);
            System.exit(-1);
          }
          String nKey = gen_new_key();
          synchronized(this) {
            String data = get_by_key(host, port, key);
            while(data != "")
            {
              data = get_by_key(host, port, key);
            }
          }

          message = "PUT" + key + message + nKey;
          out.println(message);
          out.flush();

          String reply = in.readLine();
        }
        catch(IOException e)
        {
          e.printStackTrace();
        }
      }
    }
  }
  /**
   * get_message Class - extends Thread to run without blocking while thread sleeps between calls
   */
  class get_message extends Thread
  {
    
    /**
     * @Override run() - Thread class method, executes code in function when start() is called.
     * Does get command to connection and if response is blank(\n is stripped in get_by_key())
     * wait five seconds to make new call, otherwise, print response.
     */
    @Override
    public void run()
    {
      while(true)
      {
        try(
          Socket socket = new Socket(host, port);
          BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        )
        {
          String data = get_by_key(host, port, key);
          if(data == "")
          {
            try{
              Thread.sleep(5000);
            }
            catch(InterruptedException e)
            {
              e.printStackTrace();
            }
          }
          else
          {
            key = data.substring(data.length() - KEY_SIZE, data.length());
          }
        }
        catch(IOException e)
        {
          e.printStackTrace();
        }
      }
    }
  }
}
