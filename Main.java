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

    static final Object lockObj = new Object(); //锁

    static DatagramSocket getSocket() {
        return socket;
    }

    public static void main(String[] args) {
        System.out.println("Starting DNS relay...");
        System.out.println("Usage: dnsrelay [-d | -dd] [<dns-server>] [<db-file>]");

        //根据输入参数配置环境

        if(args.length==0){//什么都不输入
            Config.DebuggerMode=3;
        }
        else if(args[0].equals("-d")){
            Config.DebuggerMode=1;
            Config.serverIP=args[1];
        }
        else if (args[0].equals("-dd")) {
            Config.DebuggerMode=2;
            Config.serverIP=args[1];
        }
        else{ //输入ip地址设置
            Config.DebuggerMode=3;
            Config.serverIP=args[0];
        }

        if(args.length==3){//修改文件必须 使用 -d/-dd DNSip 新文件路径
            Config.dataBasePath=args[2];
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


        System.out.print("Try to load  "+Config.dataBasePath+"...");
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
