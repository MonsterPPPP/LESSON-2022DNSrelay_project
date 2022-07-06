import java.util.ArrayList;

public class DNSQuestion {
    /**
     * Question 查询字段
     0  1  2  3  4  5  6  7  0  1  2  3  4  5  6  7
     +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     |                     ...                       |
     |                    QNAME                      |
     |                     ...                       |
     +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     |                    QTYPE                      |      2 bytes
     +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     |                    QCLASS                     |      2 bytes
     +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     */

    /* QNAME 8bit为单位表示的查询名(广泛的说就是：域名) */
    private String qname;

    private short qtype;

    private short qclass;

    public DNSQuestion(byte[] data){
        String domainName = Tool.extractDomain(data, 12, 0x00);
        this.setQname(domainName);

        int typePos=domainName.length()+2+12;
        byte[] tmpBytes=new byte[2];
        tmpBytes[0]=data[typePos];
        tmpBytes[1]=data[typePos+1];
        this.setQtype(Tool.byteArrayToShort(tmpBytes));

        int classPos=typePos+2;
        tmpBytes[0]=data[classPos];
        tmpBytes[1]=data[classPos+1];
        this.setQclass(Tool.byteArrayToShort(tmpBytes));
    }

    /**
     * 将question 从short转化回byte数组
     * @return byte[]
     */
    public byte[] toByteArray(){
        byte[] questionBytes = new byte[qname.length()+2+4];
        int offset = 0;
        byte[] domainByteArray = Tool.domainToByteArray(this.qname);

        for (int i=0; i<domainByteArray.length; i++) {
            questionBytes[offset++] = domainByteArray[i];
        }
        byte[] tmpbyte;
        tmpbyte = Tool.shortToByteArray(this.qtype);
        questionBytes[offset++]=tmpbyte[0];
        questionBytes[offset++]=tmpbyte[1];
        tmpbyte = Tool.shortToByteArray(this.qclass);
        questionBytes[offset++]=tmpbyte[0];
        questionBytes[offset++]=tmpbyte[1];
        return questionBytes;
    }


    /**********************************************************
     * Getter and Setter
     **********************************************************
     */

    public String getQname() {
        return qname;
    }

    public void setQname(String qname) {
        this.qname = qname;
    }

    public short getQtype() {
        return qtype;
    }

    public void setQtype(short qtype) {
        this.qtype = qtype;
    }

    public short getQclass() {
        return qclass;
    }

    public void setQclass(short qclass) {
        this.qclass = qclass;
    }

}
