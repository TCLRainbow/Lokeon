package chingdim.lokeon;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2AsyncClient;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Code for interacting via AWS API
 */
class AWS {
    // A concurrent client for sending requests to AWS API
    private static final Ec2AsyncClient ec2 = Ec2AsyncClient.builder().region(Region.EU_WEST_1).build();

    /**
     * Gets DimBot instance IP
     * @param instance The instance ID
     * @return IP
     * @throws ExecutionException Async task exception
     * @throws InterruptedException Async task exception
     */
    String getInstanceIP(String instance) throws ExecutionException, InterruptedException {
        DescribeInstancesRequest request = DescribeInstancesRequest.builder().instanceIds(instance).build();
        CompletableFuture<DescribeInstancesResponse> future = ec2.describeInstances(request);
        DescribeInstancesResponse response = future.get();
        return response.reservations().get(0).instances().get(0).publicIpAddress();
    }
}