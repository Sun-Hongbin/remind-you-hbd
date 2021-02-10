package sunhongbin.entity;

/**
 * {"BaseRequest":{"DeviceID":"e878871051103203","Skey":"@crypt_1af1bef4_ce420e20a91faba907fd24b254d414a0","Uin":"1469893904","Sid":"O0Uy9JLwXSVEFXY4"}}
 */
public class BaseRequest {

    private String Skey;

    private String Sid;

    private String Uin;

    private String DeviceID;

    public String getSkey() {
        return Skey;
    }

    public void setSkey(String skey) {
        this.Skey = skey;
    }

    public String getSid() {
        return Sid;
    }

    public void setSid(String sid) {
        this.Sid = sid;
    }

    public String getUin() {
        return Uin;
    }

    public void setUin(String uin) {
        this.Uin = uin;
    }

    public String getDeviceID() {
        return DeviceID;
    }

    public void setDeviceID(String deviceID) {
        DeviceID = deviceID;
    }
}
