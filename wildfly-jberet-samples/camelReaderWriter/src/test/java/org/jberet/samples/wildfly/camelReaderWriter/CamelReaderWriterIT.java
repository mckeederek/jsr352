/*
 * Copyright (c) 2016 Red Hat, Inc. and/or its affiliates.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Cheng Fang - Initial API and implementation
 */

package org.jberet.samples.wildfly.camelReaderWriter;

import java.net.URI;
import java.util.Arrays;
import javax.batch.runtime.BatchStatus;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.jberet.rest.client.BatchClient;
import org.jberet.samples.wildfly.common.BatchTestBase;
import org.junit.Assert;
import org.junit.Test;

public final class CamelReaderWriterIT extends BatchTestBase {

    /**
     * The full REST API URL, including scheme, hostname, port number, context path, servlet path for REST API.
     * For example, "http://localhost:8080/testApp/api"
     */
    private static final String restUrl = BASE_URL + "camelReaderWriter/api";

    private Client client = ClientBuilder.newClient();

    private BatchClient batchClient = new BatchClient(client, restUrl);

    @Override
    protected BatchClient getBatchClient() {
        return batchClient;
    }

    @Test
    public void testCamelWriter() throws Exception {
        runTest("/camel/writer");
    }

    @Test
    public void testCamelReader() throws Exception {
        runTest("/camel/reader");
    }

    @Test
    public void testCamelProcessor() throws Exception {
        runTest("/camel/processor");
    }

    @Test
    public void testCamelComponentJobs() throws Exception {
        final String[] result = runTestResult("/camel/jobs", String[].class);
        System.out.printf("Got jobs: %s%n", Arrays.toString(result));
    }

    @Test
    public void testCamelComponentJobName() throws Exception {
        runTest("/camel/jobs/" + CamelJobResource.componentJobName);
    }

    @Test
    public void testCamelComponentJobName2() throws Exception {
        runTest("/camel/jobs/" + CamelJobResource.componentJobName + "/");
    }

    @Test
    public void testCamelComponentJobNameStart() throws Exception {
        runTest("/camel/jobs/" + CamelJobResource.componentJobName + "/start");
    }

    @Test
    public void testCamelComponentJobInstances() throws Exception {
        final long[] jobInstanceIds = runTestResult("/camel/jobinstances", long[].class);
        System.out.printf("Got job instances: %s%n", Arrays.toString(jobInstanceIds));
    }

    private void runTest(final String resourceUrl) throws Exception {
        final WebTarget target = client.target(new URI(restUrl + resourceUrl));
        final long jobExecutionId = target.request().get(long.class);
        System.out.printf("Job execution id in response: %s%n", jobExecutionId);
        Assert.assertEquals(BatchStatus.COMPLETED, waitForJobExecutionDone(jobExecutionId));
    }

    private <T> T runTestResult(final String resourceUrl, final Class<T> resultType) throws Exception {
        final WebTarget target = client.target(new URI(restUrl + resourceUrl));
        return target.request().get(resultType);
    }

    private BatchStatus waitForJobExecutionDone(final long jobExecutionId) throws Exception {
        BatchStatus batchStatus;
        int numberOfSeconds = 0;
        int maxNumberOfSeconds = 200;
        do {
            Thread.sleep(1000);
            numberOfSeconds++;
            batchStatus = batchClient.getJobExecution(jobExecutionId).getBatchStatus();
        } while ((batchStatus == BatchStatus.STARTED || batchStatus == BatchStatus.STARTING || batchStatus == null)
                && numberOfSeconds < maxNumberOfSeconds);
        return batchStatus;
    }
}