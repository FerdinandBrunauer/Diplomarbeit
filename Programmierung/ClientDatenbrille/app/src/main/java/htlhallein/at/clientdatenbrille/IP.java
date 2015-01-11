package htlhallein.at.clientdatenbrille;

class IP {
    private String ipAdress;
    private int ipAdressRaw;
    private String subnet;
    private int subnetRaw;

    IP(int ipAdressRaw, String ipAdress, int subnetRaw, String subnet) {
        this.ipAdressRaw = ipAdressRaw;
        this.ipAdress = ipAdress;
        this.subnetRaw = subnetRaw;
        this.subnet = subnet;
    }

    public String getIpAdress() {
        return ipAdress;
    }

    public int getIpAdressRaw() {
        return ipAdressRaw;
    }

    public String getSubnet() {
        return subnet;
    }

    public int getSubnetRaw() {
        return subnetRaw;
    }
}