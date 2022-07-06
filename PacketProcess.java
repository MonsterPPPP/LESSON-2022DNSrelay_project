import java.io.IOException;
import java.net.*;

public class PacketProcess implements Runnable{
    private byte[] data; //
    private int dataLength;
    private InetAddress clientAddress;
    private int clientPort;

    private DatagramPacket thisPacket;
    public PacketProcess(DatagramPacket packet){
        thisPacket=packet;
        dataLength = packet.getLength();
        data = new byte[packet.getLength()];
        //将数据报保存到data数组中
        System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());
        clientAddress = packet.getAddress();
        clientPort = packet.getPort();
    }

    /**
     +---------------------+
    |        Header       | 报文头  dnsHeader
    +---------------------+
    |       Question      | 查询的问题 dnsQuestion
    +---------------------+
    |        Answer       | 应答
    +---------------------+
    |      Authority      | 授权应答  dnsRR
    +---------------------+
    |      Additional     | 附加信息
    +---------------------+
     */

    public void run(){
        DNSHeader dnsHeader = new DNSHeader(data); //创建DNSHealder对象
        DNSQuestion dnsQuestion=new DNSQuestion(data);//创建DNSQuestion对象

        //首先查询本地数据库这个域名
        String retIP=(String) Main.cachedName.get(dnsQuestion.getQname());
        //如果在本地数据库查不到这个域名，
        if(retIP==null){
            // 那就转发给设定好的DNSserver，接收请求之后再返回给请求的机器
            repostToInServer();
        }
        //如果在本机找到，那么就首先构建packet，然后返回查询结果
        else{
            sendBackfromThere(retIP,dnsHeader,dnsQuestion);
        }

    }

    private void repostToInServer(){
        try {
            InetAddress dnsServer = InetAddress.getByName(Config.serverIP);

            //更改 转发给server的packet id
            byte[] tmpBytes= new byte[data.length];
            System.arraycopy(data, 0, tmpBytes, 0, data.length);
            DNSHeader tmpHeader=new DNSHeader(tmpBytes);
            tmpHeader.setID(tmpHeader.getID()+128);//在原来id的基础上偏移10
            byte[] tmpBytes2=tmpHeader.toByteArray();
            byte[] toServerdata=new byte[data.length];
            toServerdata[0]=tmpBytes2[0];
            toServerdata[1]=tmpBytes2[1];
            for(int i=2;i<data.length;i++){
                toServerdata[i]=tmpBytes[i];
            }

            DatagramPacket sendtoServerPacket = new DatagramPacket(toServerdata, dataLength, dnsServer, 53);
            DatagramSocket toServerSocket = new DatagramSocket();

            toServerSocket.send(sendtoServerPacket);//发送给DNS Server
            new Debugger(sendtoServerPacket,false,thisPacket);

            byte[] receivedData = new byte[1024];
            DatagramPacket receivedServerPacket = new DatagramPacket(receivedData, receivedData.length);

            toServerSocket.receive(receivedServerPacket);//从DNS Server接收到
            new Debugger(receivedServerPacket,true);


            // 回复给请求机的packet
            //将之前改的id给改回来
            byte[] responseData=new byte[receivedData.length];
            responseData[0]=tmpBytes[0];
            responseData[1]=tmpBytes[1];
            for(int i=2;i<receivedData.length;i++){
                responseData[i]=receivedData[i];
            }

            DatagramPacket responsePacket = new DatagramPacket(responseData, receivedServerPacket.getLength(), clientAddress, clientPort);
            toServerSocket.close();
            synchronized (Main.lockObj) {//调用socket时加锁，只有一条线程可以使用中继服务器的socket
                try {
                    //System.out.println(Thread.currentThread().getName() + " 获得socket，响应" + dnsQuestion.getQname());
                    Main.getSocket().send(responsePacket);
                    new Debugger(responsePacket,false,receivedServerPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendBackfromThere(String retIP,DNSHeader dnsHeader,DNSQuestion dnsQuestion){
        // Header
        short flags = 0;
        if (!retIP.equals("0.0.0.0")) {
            flags = (short) 0x8580;// rcode=0 一切正常
        } else {
            flags = (short) 0x8583;// rcode=3 拦截
        }

        DNSHeader dnsResponseHeader = new DNSHeader(dnsHeader.getID(), flags, dnsHeader.getQucount(), (short) 1, (short) 1, (short) 0);
        byte[] responseHeaderByteArray = dnsResponseHeader.toByteArray();

        // Question
        byte[] responseQuestionByteArray = dnsQuestion.toByteArray();

        // Answers
        DNSRR responseAnDNSRR = new DNSRR((short) 0xc00c, dnsQuestion.getQtype(), dnsQuestion.getQclass(), 3600*24, (short) 4, retIP);
        byte[] responseAnDNSRRByteArray = responseAnDNSRR.toByteArray();


        // Authoritative nameservers
        DNSRR responseNsDNSRR = new DNSRR((short) 0xc00c, (short) 6, dnsQuestion.getQclass(), 3600*24, (short) 0 , null);
        byte[] responseNsDNSRRByteArray = responseNsDNSRR.toByteArray();

        byte[] response_data = new byte[responseHeaderByteArray.length + responseQuestionByteArray.length + responseAnDNSRRByteArray.length + responseNsDNSRRByteArray.length];
        int offset = 0;
        for (int i = 0; i < responseHeaderByteArray.length; i++) {
            response_data[offset++] = responseHeaderByteArray[i];
        }
        for (int i = 0; i < responseQuestionByteArray.length; i++) {
            response_data[offset++] = responseQuestionByteArray[i];
        }
        if (!retIP.equals("0.0.0.0")) {
            for (int i = 0; i < responseAnDNSRRByteArray.length; i++) {
                response_data[offset++] = responseAnDNSRRByteArray[i];
            }
        }
        for (int i = 0; i < responseNsDNSRRByteArray.length; i++) {
            response_data[offset++] = responseNsDNSRRByteArray[i];
        }
        // 回复响应数据包
        DatagramPacket responsePacket = new DatagramPacket(response_data, response_data.length, clientAddress, clientPort);
        synchronized (Main.lockObj) {//调用socket时加锁，只有一条线程可以使用中继服务器的socket
            try {
                Main.getSocket().send(responsePacket);
                new Debugger(responsePacket,false,thisPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
