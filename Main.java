import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {
    private static DatagramSocket socket;

    public static Hashtable cachedName = new Hashtable();

    static final Object lockObj = new Object();

    static DatagramSocket getSocket() {
        return socket;
    }

    public static void main(String[] args) {
        System.out.println("Starting DNS relay...");
        System.out.println("Usage: dnsrelay [-d | -dd] [<dns-server>] [<db-file>]");

        System.out.println("输入参数长度:"+args.length);
        for(String x:args){
            System.out.print("arg:");
            System.out.println(x);
        }

        //initialize setting
        if(args[0].equals("-d")){
            Config.DebuggerMode=1;
            Config.serverIP=args[1];
        }
        else if (args[0].equals("-dd")) {
            Config.DebuggerMode=2;
            Config.serverIP=args[1];
        }
        else{
            Config.DebuggerMode=3;
            Config.serverIP=args[0];
        }

        System.out.println("Name server "+Config.serverIP+":53");
        System.out.println("Debug level "+Config.DebuggerMode);

        //initialize socket
        System.out.print("Bind UDP port 53 ...");
        try {
            socket = new DatagramSocket(53); //创建中继服务器端的socket
            System.out.println("OK!");
        } catch (SocketException e) {
            System.out.println("ERROR!");
            e.printStackTrace();
        }
        byte[] data = new byte[1024]; //构造中继服务器端的udp Packet 空盒子
        DatagramPacket packet = new DatagramPacket(data, data.length);


        System.out.print("Try to load table"+Config.dataBasePath+"...");
        try{
            File file = new File(Config.dataBasePath);
            BufferedReader br = new BufferedReader(new FileReader(file));

            String s = null;
            int linenum=0;
            String recordString="";
            while((s = br.readLine())!=null){//使用readLine方法，一次读一行
                String[] tmpString=s.split(" ");
                cachedName.put(tmpString[1],tmpString[0]);
                recordString=s+recordString;
                linenum++;
            }
            br.close();
            System.out.println("OK!");
            if (Config.DebuggerMode==2){ //使用-dd调试时，还将打印所有的信息
                Enumeration<String> names=cachedName.keys();
                while(names.hasMoreElements()){
                    recordString=names.nextElement();
                    System.out.println(cachedName.get(recordString)+"    "+recordString);
                }
            }
            System.out.println(linenum+" names,occupy "+recordString.getBytes(StandardCharsets.UTF_8).length+" bytes memory");
        }catch(Exception e){
            System.out.println("Ignored!");
            e.printStackTrace();
        }

        System.out.println("Successfully started DNS relay service!");

        ExecutorService servicePool = Executors.newFixedThreadPool(10);  // 容纳10个线程的线程池
        while (true) {
            try {
                socket.receive(packet); //阻塞等待，接收数据
                new Debugger(packet,true); //根据调试模式显示调试信息
            } catch (IOException e) {
                e.printStackTrace();
            }
            servicePool.execute(new PacketProcess(packet));
        }
    }
}
