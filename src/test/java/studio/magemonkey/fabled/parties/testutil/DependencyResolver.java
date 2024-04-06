package studio.magemonkey.fabled.parties.testutil;

import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class DependencyResolver {
    private static List<String> repositories = List.of("https://s01.oss.sonatype.org/content/repositories/snapshots/");

    public static File resolve(String dependency) throws FileNotFoundException {
        String[] pieces = dependency.split(":");
        String path = "/home/travja/.m2/repository/" + (pieces[0] + "/" + pieces[1]).replace('.', '/')
                + "/" + pieces[2] + "/" + pieces[1] + "-" + pieces[2] + ".jar";
        System.out.println(path);
        return new File(path);
//        return downloadFabled(pieces[0], pieces[1], pieces[2]);
    }

    private static File downloadFabled(String groupId, String artifact, String version) throws FileNotFoundException {
        String url = findSkillUrl(groupId, artifact, version);
        log.info("Downloading " + artifact + " from " + url);
        try (BufferedInputStream inputStream = new BufferedInputStream(new URL(url).openStream());
             FileOutputStream fileOS = new FileOutputStream(artifact + ".jar")) {
            byte data[] = new byte[1024];
            int  byteContent;
            while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
                fileOS.write(data, 0, byteContent);
            }

            log.info("Download complete");
            return new File(artifact + ".jar");
        } catch (IOException e) {
            // handles IO exceptions
            throw new RuntimeException("Could not fetch Fabled dependency");
        }
    }

    private static String findSkillUrl(String groupId, String artifact, String version) throws FileNotFoundException {
        StringBuilder html = new StringBuilder();
        for (String rep : repositories) {
            String repository = rep + (groupId + "/" + artifact).replace('.', '/') + "/" + version + "/";
            try (BufferedReader read = new BufferedReader(new InputStreamReader(new URL(repository).openStream()))) {
                String line;
                while ((line = read.readLine()) != null) {
                    html.append(line);
                }
            } catch (IOException e) {
                // handles IO exceptions
                throw new RuntimeException("Could not fetch Fabled repository");
            }

            if (html.length() == 0) return null;

            String text = html.toString();
            Pattern pat = Pattern.compile(
                    "<a href=\"("
                            + (repository + artifact + "-" + version.replace("-SNAPSHOT", ""))
                            .replace("/", "\\/")
                            .replace(".", "\\.")
                            + "-?[^>]*?(?<!sources)(?<!javadocs)"
                            + "\\.jar)\">");
            Matcher mat = pat.matcher(text);
            String  url = "NO_URL";
            while (mat.find()) {
                url = mat.group(1);
            }
            if (!url.equals("NO_URL"))
                return url;
        }

        throw new FileNotFoundException("Couldn't locate " + groupId + ":" + artifact + ":" + version);
    }
}
