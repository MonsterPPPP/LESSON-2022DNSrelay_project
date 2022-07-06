
public class DNSRR {
    /**
     * Answer/Authority/Additional
     0  1  2  3  4  5  6  7  0  1  2  3  4  5  6  7
     +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     |					   ... 						  |
     |                    NAME                       |      2
     |                    ...                        |
     +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     |                    TYPE                       |      2
     +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     |                    CLASS                      |      2
     +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     |                    TTL                        |      4
     |                                               |
     +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     |                    RDLENGTH                   |      2
     +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     |                    ...                        |
     |                    RDATA                      |      0 or 4
     |                    ...                        |
     +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     */

    private short aname;

    private short atype;

    private short aclass;

    private int ttl;

    private short rdlength;

    private String rdata;

    public DNSRR() {}

    public DNSRR(short aname, short atype, short aclass, int ttl, short rdlength, String rdata) {
        this.aname = aname;
        this.atype = atype;
        this.aclass = aclass;
        this.ttl = ttl;
        this.rdlength = rdlength;
        this.rdata = rdata;
    }

    /**
     * 输出包含DNS RR所有信息的字节数组
     */
    public byte[] toByteArray() {
        byte[] data = new byte[12 + rdlength];
        int offset = 0;
        byte[] tmpbyte ;
        tmpbyte = Tool.shortToByteArray(aname);
        data[offset++] = tmpbyte[0];
        data[offset++] = tmpbyte[1];
        tmpbyte = Tool.shortToByteArray(atype);
        data[offset++] = tmpbyte[0];
        data[offset++] = tmpbyte[1];
        tmpbyte = Tool.shortToByteArray(aclass);
        data[offset++] = tmpbyte[0];
        data[offset++] = tmpbyte[1];
        tmpbyte = Tool.intToByteArray(ttl);
        data[offset++] = tmpbyte[0];
        data[offset++] = tmpbyte[1];
        data[offset++] = tmpbyte[2];
        data[offset++] = tmpbyte[3];
        tmpbyte = Tool.shortToByteArray(rdlength);
        data[offset++] = tmpbyte[0];
        data[offset++] = tmpbyte[1];
        //十二个固定字符
        //+可能存在的回复内容，ip地址
        if (rdlength == 4) {
            tmpbyte = Tool.ipv4ToByteArray(rdata);
            for (int i=0; i<4; i++) {
                data[offset++] = tmpbyte[i];
            }
        }
        return data;
    }


    /***********************************************
     * Getter and Setter
     * *********************************************
     */

    public short getAname() {
        return aname;
    }

    public void setAname(short aname) {
        this.aname = aname;
    }

    public short getAtype() {
        return atype;
    }

    public void setAtype(short atype) {
        this.atype = atype;
    }

    public short getAclass() {
        return aclass;
    }

    public void setAclass(short aclass) {
        this.aclass = aclass;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public short getRdlength() {
        return rdlength;
    }

    public void setRdlength(short rdlength) {
        this.rdlength = rdlength;
    }

    public String getRdata() {
        return rdata;
    }

    public void setRdata(String rdata) {
        this.rdata = rdata;
    }
}
