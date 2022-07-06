import java.util.Date;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;

/**
 * 在收到或者发送DNS数据报后被调用，根据Config中配置的调试模式打印调试信息
 */
public class Debugger {

    private byte[] data; //
    private InetAddress address;
    private int port;
    private  boolean isReceive;

    private byte[] lastData;

    public Debugger(DatagramPacket packet,boolean isReceive){
        this.address = packet.getAddress();
        this.port = packet.getPort();
        this.data = new byte[packet.getLength()];
        this.isReceive=isReceive;
        //将数据报保存到data数组中
        System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());
        startDebugger();
    }
    public Debugger(DatagramPacket packet,boolean isReceive,DatagramPacket lastPacket){
        this.address = packet.getAddress();
        this.port = packet.getPort();
        this.isReceive=isReceive;
        this.data = new byte[packet.getLength()];
        //将数据报保存到data数组中
        System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());
        this.lastData = new byte[lastPacket.getLength()];
        //将数据报保存到data数组中
        System.arraycopy(lastPacket.getData(), 0, lastData, 0, lastPacket.getLength());
        startDebugger();
    }



    private void startDebugger(){
        if(Config.DebuggerMode==1){ //-d 模式
            if(isReceive){
                dprintReceive();
            }
            else{
                dprintSend();
            }
        } else if (Config.DebuggerMode==2) { //-dd 模式
            if(isReceive){
                ddprintReceive();
            }
            else{
                ddprintSend();
            }
        }else {
            /**
             什么都不做~
             */
        }
    }

    private void ddprintReceive(){
        System.out.println("RECV from "+this.address.getHostAddress()+":"+this.port+" ("+this.data.length+" bytes)");

        //打印完整数据报
        String[] hexPacketString=Tool.bytesToHexString(data);
        for(int i=0;i<this.data.length;i++){
            if(i%16==0 && i!=0){
                System.out.println("");
            }
            System.out.print(hexPacketString[i]+" ");
        }
        System.out.println("");

        //解析并且打印数据报的header
        DNSHeader tmpHeader=new DNSHeader(this.data);
        System.out.print("        ");

        String[] tmpID=Tool.bytesToHexString(Tool.shortToByteArray(tmpHeader.getID())) ;
        System.out.print("ID "+tmpID[0]+tmpID[1]+", ");

        byte[] flags= Tool.shortToByteArray(tmpHeader.getFlags());
        System.out.print("QR "+(((flags[0] & 0xff)& 0x80)>>7)+", "); //取前一个byte 的第一位
        System.out.print("OPCODE "+(((flags[0] & 0xff)& 0x78)>>3)+", ");//取前一个byte 的第二到五
        System.out.print("AA "+(((flags[0] & 0xff) & 0x04)>>2)+", ");//取前一个byte 的第六
        System.out.print("TC "+(((flags[0] & 0xff)& 0x02)>>1)+", ");//取前一个byte 的第七
        System.out.print("RD "+((flags[0] & 0xff)& 0x01)+", ");//取前一个byte 的第八
        System.out.print("RA "+(((flags[1] & 0xff) & 0x80)>>7)+", ");//取第二个byte 的第一
        System.out.print("Z "+(((flags[1] & 0xff)& 0x70)>>4)+", ");//取第二个byte 的第二到四
        System.out.print("RCODE "+((flags[1] & 0xff) & 0x0f)+"\n");//取第二个byte 的第五到八
        System.out.print("        ");
        System.out.print("QDCOUNT "+tmpHeader.getQucount()+", ");
        System.out.print("ANCOUNT "+tmpHeader.getAncount()+", ");
        System.out.print("NSCOUNT "+tmpHeader.getAucount()+", ");
        System.out.print("NSCOUNT "+tmpHeader.getAdcount()+"\n");

        System.out.print(" "+Config.debuggerCounter+": ");
        Config.debuggerCounter++;
        System.out.print(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+" ");
        System.out.print("Client "+this.address.getHostAddress());
        System.out.print("        ");

        //       打印Question部分
        DNSQuestion tmpQuestion=new DNSQuestion(this.data);
        System.out.println(tmpQuestion.getQname()+", TYPE "+tmpQuestion.getQtype()+", CLASS "+tmpQuestion.getQclass());

    }
    private void ddprintSend(){
        System.out.print("SEND to "+this.address.getHostAddress()+":"+this.port+"("+this.data.length+" bytes)");
        //解析并且打印数据报的header
        DNSHeader lastHeader=new DNSHeader(this.lastData);
        DNSHeader tmpHeader=new DNSHeader(this.data);
        String[] tmpID1=Tool.bytesToHexString(Tool.shortToByteArray(lastHeader.getID())) ;
        String[] tmpID2=Tool.bytesToHexString(Tool.shortToByteArray(tmpHeader.getID())) ;
        System.out.println("[ID "+tmpID1[0]+tmpID1[1]+"->"+tmpID2[0]+tmpID2[1]+"]");
    }


    private void dprintReceive(){
        //如果这个数据报是来自server的，那么就把它忽略
        if(this.address.getHostAddress().equals(Config.serverIP)){
            return;

        }
        DNSQuestion tmpQuestion=new DNSQuestion(this.data);
        //首先查询本地数据库这个域名
        String retIP=(String) Main.cachedName.get(tmpQuestion.getQname());
        //如果在本地数据库查不到这个域名，
        if(retIP!=null && tmpQuestion.getQtype()!=28){
            //如果在本地数据库存在，那么就打印*
            System.out.print(" "+Config.debuggerCounter+":*");
        }
        else{
            System.out.print(" "+Config.debuggerCounter+": ");
        }
        Config.debuggerCounter++;
        System.out.print(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+" ");
        System.out.print("Client "+this.address.getHostAddress());
        System.out.print("        ");

        // 打印Question部分
        System.out.println(tmpQuestion.getQname()+", TYPE "+tmpQuestion.getQtype()+", CLASS "+tmpQuestion.getQclass());
    }

    private void dprintSend(){
        //什么都不做
    }

}


