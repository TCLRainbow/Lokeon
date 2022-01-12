package chingdim.lokeon.aas;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Filter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Lokeon_aaS {
    private static final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    private static final Ec2Client ec2 = Ec2Client.builder().region(Region.EU_WEST_1).build();
    private static String ip;
    //Check via sudo update-alternatives --config java
    private static final String JAVA_8_PATH = "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.292.b10-3.fc34.x86_64/jre/bin/java";

    private static HttpRequest.Builder buildHttpRequest(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://%s/%s", ip, path)));
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Filter isRunning = Filter.builder().name("instance-state-name").values("running").build();
        Filter isNameDimBot = Filter.builder().name("tag:Name").values("DimBot").build();
        DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                .filters(isNameDimBot, isRunning).build();
        DescribeInstancesResponse response = ec2.describeInstances(request);
        ip =  response.reservations().get(0).instances().get(0).publicIpAddress();

        HttpRequest bootRequest = buildHttpRequest("boot")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        client.send(bootRequest, HttpResponse.BodyHandlers.discarding());
        //String serverDir = client.send(bootRequest, HttpResponse.BodyHandlers.ofString()).body();
        //System.out.println("server: " + serverDir);
        FileWriter fw = new FileWriter("ip"); // serverDir + "/ip"
        fw.write(ip);
        fw.close();
        //String exe = serverDir.startsWith("s") ? "java" : JAVA_8_PATH;
        String exe = "java";

        //noinspection InfiniteLoopStatement
        while (true) {
            ProcessBuilder pb = new ProcessBuilder(exe, "-Xmx4G", "-jar", "paper.jar");
            //pb.directory(new File("./" + serverDir));
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            Process p = pb.start();
            p.waitFor();
            int code = p.exitValue();
            System.out.println("Server.jar exited with code " + code);
            HttpRequest exitRequest = buildHttpRequest("exit")
                    .POST(HttpRequest.BodyPublishers.ofString(String.valueOf(code)))
                    .build();
            client.send(exitRequest, HttpResponse.BodyHandlers.discarding());
        }

    }
}
