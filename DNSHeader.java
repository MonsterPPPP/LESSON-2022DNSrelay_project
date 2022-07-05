import java.util.ArrayList;

public class DNSHeader {
    /**
     * DNS Header
     0  1  2  3  4  5  6  7  0  1  2  3  4  5  6  7
     +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     |                      ID                       |
     +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     |QR|  opcode   |AA|TC|RD|RA|   Z    |   RCODE   |
     +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     |                    QDCOUNT                    |
     +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     |                    ANSWER                    |
     +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     |                    AUTHORITY                  |
     +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     |                    ADDITIONAL                 |
     +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     */
    /* 会话标识（2字节）*/
    private short ID;

    /* Flags（2字节）*/
    private short flags;

    /* QDCOUNT（2字节）*/
    private short qucount;

    /* ANCOUNT（2字节）*/
    private short ancount;

    /* NSCOUNT（2字节）*/
    private short aucount;

    /* ARCOUNT（2字节）*/
    private short adcount;

    /* 记录上面六个值的ArrayList */
    private ArrayList<Short> infoArray;

    /**
     * 通过已知数据报构建header
     * @param packetData
     */
    public  DNSHeader(byte[] packetData){
        infoArray=new ArrayList<Short>();

        int i=0;
        byte[] tmpBytes=new byte[2];
        for(byte tmp:packetData){
            if(i==0){
                tmpBytes[0]=tmp;
            }
            else if(i%2==0){
                infoArray.add(Tool.byteArrayToShort(tmpBytes));
                tmpBytes=new byte[2];
                tmpBytes[0]=tmp;
                if(infoArray.size()==6){ //读取完了6*2 bytes 的header
                    break;
                }
            }
            else{
                tmpBytes[1]=tmp;
            }
            i++;
        }
        this.setID(infoArray.get(0));
        this.setFlags(infoArray.get(1));
        this.setQucount(infoArray.get(2));
        this.setAncount(infoArray.get(3));
        this.setAucount(infoArray.get(4));
        this.setAdcount(infoArray.get(5));

    }

    /**
     * 通过输入header的信息构造header
     */
    public DNSHeader(short transID, short flags, short qdcount, short ancount, short nscount, short arcount) {
        this.ID = transID;
        this.flags = flags;
        this.qucount = qdcount;
        this.ancount = ancount;
        this.aucount = nscount;
        this.adcount = arcount;
        infoArray=new ArrayList<Short>();
        infoArray.add(getID());
        infoArray.add(getFlags());
        infoArray.add(getQucount());
        infoArray.add(getAncount());
        infoArray.add(getAucount());
        infoArray.add(getAdcount());
    }



    /**
     * 将header 从short类型转化回一个12bytes的byte数组
     * @return byte[12]
     */
    public byte[] toByteArray(){


        byte[] headerBytes = new byte[12];
        int i=0;
        for(short tmp:infoArray){
            byte[] tmpByte = new byte[2];
            tmpByte=Tool.shortToByteArray(tmp);
            headerBytes[i++]=tmpByte[0];
            headerBytes[i++]=tmpByte[1];
        }
        return headerBytes;
    }



    /**********************************************************
     * Getter and Setter
     **********************************************************
     */
    public short getID() {
        return ID;
    }

    public void setID(short ID) {
        this.ID = ID;
    }

    public short getFlags() {
        return flags;
    }

    public void setFlags(short flags) {
        this.flags = flags;
    }



    public short getAncount() {
        return ancount;
    }

    public void setAncount(short ancount) {
        this.ancount = ancount;
    }

    public short getQucount() {
        return qucount;
    }

    public void setQucount(short qucount) {
        this.qucount = qucount;
    }

    public short getAucount() {
        return aucount;
    }

    public void setAucount(short aucount) {
        this.aucount = aucount;
    }

    public short getAdcount() {
        return adcount;
    }

    public void setAdcount(short adcount) {
        this.adcount = adcount;
    }




}
