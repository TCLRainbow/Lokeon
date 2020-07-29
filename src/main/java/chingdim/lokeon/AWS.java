package chingdim.lokeon;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2AsyncClient;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

class AWS {
    private static final Ec2AsyncClient ec2 = Ec2AsyncClient.builder().region(Region.EU_NORTH_1).build();

    String getInstanceIP() throws ExecutionException, InterruptedException {
        DescribeInstancesRequest request = DescribeInstancesRequest.builder().instanceIds("i-0684c778f22b3980b").build();
        CompletableFuture<DescribeInstancesResponse> future = ec2.describeInstances(request);
        DescribeInstancesResponse response = future.get();
        return response.reservations().get(0).instances().get(0).publicIpAddress();
    }
}