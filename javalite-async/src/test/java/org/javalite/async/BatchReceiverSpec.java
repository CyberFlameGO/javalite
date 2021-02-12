package org.javalite.async;

import org.javalite.common.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jms.JMSException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.javalite.test.jspec.JSpec.the;

/**
 * @author igor on 8/8/17.
 */
public class BatchReceiverSpec {
    private static final String QUEUE_NAME = "queue1";
    private Async async;
    private String asyncRoot;

    @Before
    public void before() throws IOException {
        asyncRoot = Files.createTempDirectory(UUID.randomUUID().toString()).toFile().getCanonicalPath();
        async = new Async(asyncRoot, false, new QueueConfig(QUEUE_NAME));
        async.start();
    }

    @After
    public void after() throws IOException {
        async.stop();
        Util.recursiveDelete(Paths.get(asyncRoot));
    }


    @Test
    public void shouldReceiveMessages() throws JMSException {
        int count = 0;
        for (int i = 0; i < 50; i++) {
            async.sendTextMessage(QUEUE_NAME, "hello " + i);
        }
        try (BatchReceiver br = async.getBatchReceiver(QUEUE_NAME, 100)) {
            List<String> messages = br.receiveTextMessages(500);
            for (String ignored : messages) {
                count++;
            }
        }
        the(count).shouldBeEqual(50);

    }

    @Test
    public void shouldRollbackCommitTransaction() throws JMSException {

        for (int i = 0; i < 3; i++) {
            async.sendTextMessage(QUEUE_NAME, "hello " + i);
        }

        BatchReceiver br = async.getBatchReceiver(QUEUE_NAME, 100);
        List<String> messages = br.receiveTextMessages(500);
        the(messages.size()).shouldBeEqual(3);

        br.rollback(); // <<< -- pushes messages back into queue

        messages = br.receiveTextMessages(500);
        the(messages.size()).shouldBeEqual(3);

        br.commit(); // <<< -- finally deletes messages from queue
        messages = br.receiveTextMessages(500);
        the(messages.size()).shouldBeEqual(0);
        br.close();
    }
}
