import org.accelio.jxio.jxioConnection.Constants;
import org.accelio.jxio.jxioConnection.JxioConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.storm.messaging.TaskMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by seokwoo on 2017-04-10.
 */
public class TestClient extends Thread {
    private static final Log LOG = LogFactory.getLog(TestClient.class.getCanonicalName());
    private final long bytes;
    private final URI uri;
    private byte[] temp = new byte[Constants.MSGPOOL_BUF_SIZE];
    private JxioConnection connection;
    private InputStream input;
    private OutputStream output;

    public TestClient(URI uri, int index) {
        this.uri = uri;
        bytes = Long.parseLong(uri.getQuery().split("size=")[1]);
        temp = "hello server".getBytes();
        try {
            connection = new JxioConnection(uri);

            //생성시 스트림 타입에 맞게 요청 전송
            input = connection.getInputStream();
            output = connection.getOutputStream();
        } catch (ConnectException e) {
            LOG.error("Connection error");
            e.printStackTrace();
        }

    }

    public void run() {
        for (int i = 0; i < 10000; i++) {
            try {
                read();
                write();
            } catch (IOException e) {
                LOG.error("read, write error");
                e.printStackTrace();
                System.exit(1);
            }
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        try {
            input.close();
            output.close();
        } catch (IOException e) {
            LOG.error("close error");
            e.printStackTrace();
        }
    }

    public void read() throws IOException {
        long read = 0;
        int n = 0;
        ByteBuffer bb = null;

        while ((n = input.read(temp)) != -1) {
            read += n;
            bb = ByteBuffer.wrap(temp);
        }

        if (read != bytes)
            LOG.error("Number of bytes read " + read + " is different from number of bytes requested " + bytes);

        if (temp != null) {
            TaskMessage tm = new TaskMessage(0, null);
            tm.deserialize(bb);
            String str = new String(tm.message());
            LOG.info("received message is => " + str);
        } else
            LOG.info("No message received");
    }

    public void write() throws IOException {
        ByteBuffer bb;
        TaskMessage tm;
        for (int sent = 0; sent < 1000; sent++) {
            temp = ("Client message qwertyuiop" + sent).getBytes();
            tm = new TaskMessage(0, temp);

            output.write(tm.serialize().get());
        }
    }

    public void calcBW(long t) {
        long time = System.nanoTime() - t;
        LOG.info(this.toString() + " Time to transfer data in nano: " + time);
        float gigas = (float) bytes / 1000000000;
        float milli = (float) time / 1000000;
        LOG.info(this.toString() + " [----------------  BW[MB/s]: " + (gigas / milli * 1000000) + " ----------------]");
    }

    // rdma://$2:$3/data?stream=input&size=$6"\
    //./runJxioConnectionTest.sh c 36.0.0.149 4611 2 10000000000 1 1
    //                           ip, port, clients, 10GB, input, output

    /**
     * @param args
     */
    public static void main(String[] args) {
        String uriString;
        URI uri;
        try {
            ExecutorService es = Executors.newFixedThreadPool(4);
            // input
            for (int i = 7000; i <= 7003; i++) {
                uriString = String.format("rdma://%s:%s/data?size=%s", "114.70.235.43", String.valueOf(i), Constants.MSGPOOL_BUF_SIZE);
                uri = new URI(uriString);
                es.submit(new TestClient(uri, i));
            }

            es.shutdown();
            es.awaitTermination(5, TimeUnit.MINUTES);
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
