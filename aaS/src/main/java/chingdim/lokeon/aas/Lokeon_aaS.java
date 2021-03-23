package chingdim.lokeon.aas;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Filter;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Lokeon_aaS {
    private static final HttpClient client = HttpClient.newBuilder().build();
    private static final Ec2Client ec2 = Ec2Client.builder().region(Region.EU_WEST_1).build();
    private static String ip;

    private static HttpRequest buildHttpRequest(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://%s/%s", ip, path)))
                .build();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Filter isRunning = Filter.builder().name("instance-state-name").values("running").build();
        Filter isNameDimBot = Filter.builder().name("tag:Name").values("DimBot").build();
        DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                .filters(isNameDimBot, isRunning).build();
        DescribeInstancesResponse response = ec2.describeInstances(request);
        ip =  response.reservations().get(0).instances().get(0).publicIpAddress();

        HttpRequest bootRequest = buildHttpRequest("boot");
        client.sendAsync(bootRequest, HttpResponse.BodyHandlers.discarding());

        FileWriter fw = new FileWriter("ip");
        fw.write(ip);
        fw.close();

        //noinspection InfiniteLoopStatement
        while (true) {
            ProcessBuilder pb = new ProcessBuilder("java", "-Xmx4G", "-jar", "server.jar");
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            Process p = pb.start();
            p.waitFor();
            int code = p.exitValue();
            System.out.println("Server.jar exited with code " + code);
            HttpRequest exitcodeRequest = buildHttpRequest("exitcode?code=" + code);
            client.sendAsync(exitcodeRequest, HttpResponse.BodyHandlers.discarding());
        }

    }
}
