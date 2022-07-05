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

    public Debugger(DatagramPacket packet,boolean isReceive){
        address = packet.getAddress();
        port = packet.getPort();
        data = new byte[packet.getLength()];
        //将数据报保存到data数组中
        System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());

        if(Config.DebuggerMode==1){
            if(isReceive){
                ddprintReceive();
            }
            else{
                dprintReceive();
            }
        } else if (Config.DebuggerMode==2) {
            if(isReceive){
                ddprintSend();
            }
            else{
                dprintSend();
            }
        }else {
            /**
            需要再确认！！！！！！！！！！！！！！！！！！！！！
             */
        }
    }
    private void ddprintReceive(){
        System.out.println("RECV from"+this.address.getAddress()+":"+this.port+"("+this.data.length+" bytes)");

        //打印完整数据报
        String[] hexPacketString=Tool.bytesToHexString(data);
        for(int i=0;i<this.data.length;i++){
            if(i%16==0){
                System.out.println("");
            }
            System.out.print(hexPacketString[i]+" ");
        }
        System.out.println("");

        //解析并且打印数据报的header
        DNSHeader tmpHeader=new DNSHeader(this.data);
        System.out.print("        ");
        System.out.print("ID"+tmpHeader.getID()+", ");
        byte[] flags= Tool.shortToByteArray(tmpHeader.getFlags());
        System.out.print("QR "+(flags[0] & 0x01)+", "); //取前一个byte 的第一位
        System.out.print("OPCODE "+(flags[0] & 0x17)+", ");//取前一个byte 的第二到五
        System.out.print("AA "+(flags[0] & 0x20)+", ");//取前一个byte 的第六
        System.out.print("TC "+(flags[0] & 0x40)+", ");//取前一个byte 的第七
        System.out.print("RD "+(flags[0] & 0x80)+", ");//取前一个byte 的第八
        System.out.print("RA "+(flags[1] & 0x01)+", ");//取第二个byte 的第一
        System.out.print("Z "+(flags[1] & 0x07)+", ");//取第二个byte 的第二到四
        System.out.print("RCODE "+(flags[1] & 0xf0)+"\n");//取第二个byte 的第五到八
        System.out.print("        ");
        System.out.print("QDCOUNT "+tmpHeader.getQucount()+", ");
        System.out.print("ANCOUNT "+tmpHeader.getAncount()+", ");
        System.out.print("NSCOUNT "+tmpHeader.getAucount()+", ");
        System.out.print("NSCOUNT "+tmpHeader.getAdcount()+"\n");


        System.out.print(" "+Config.debuggerCounter+": ");
        System.out.print(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+" ");
        System.out.print("Client"+this.address.getAddress());
        DNSQuestion tmpQuestion=new DNSQuestion(this.data);
        int headerLen=tmpHeader.toByteArray().length;
        int QuestionLen=tmpQuestion.toByteArray().length;
        DNSRR tmpRR=new DNSRR(this.data,headerLen+QuestionLen);


        //       打印RR部分
        //

    }
    private void dprintReceive(){

    }
    private void ddprintSend(){

    }
    private void dprintSend(){

    }


}


