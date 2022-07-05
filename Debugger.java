import java.net.DatagramPacket;
import java.net.InetAddress;

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

        short flags= tmpHeader.getFlags();


        System.out.print("");
        System.out.print("");
        System.out.print("");


    }
    private void dprintReceive(){

    }
    private void ddprintSend(){

    }
    private void dprintSend(){

    }


}


