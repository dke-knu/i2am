package org.apache.storm.messaging.jxio;

import org.apache.storm.Config;
import org.apache.storm.messaging.IConnection;
import org.apache.storm.messaging.IContext;
import org.apache.storm.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * Created by seokwoo on 17. 3. 17.
 * <p>
 * 메시지 플러그인 용으로 구현되야한다.
 * TransportFactory에서 플러그인이 정해지면 해당 플러그인의 prepare(storm_conf) 함수를 호출한다.
 * 플러그인은 storm.yaml의 storm.messaging.transport로 정해진다 기본 값은 org.apache.storm.messaging.netty.Context
 * <p>
 * 워커 프로세스가 초기화될 때 다음과 같은 라인이 실행된다. storm-2.0.0 기준 WorkerState.java
 * this.mqContext = (null != mqContext) ? mqContext : TransportFactory.makeContext(topologyConf);
 */
public class Context implements IContext {
    @SuppressWarnings("rawtypes")
    private Map storm_conf;
    private Map<String, IConnection> connections;
    private ScheduledThreadPoolExecutor clientScheduleService;
    private boolean isEnablePortal = false;
    /*
    *
    *    Enqueue a task message to be sent to server
    *    private void scheduleConnect(long delayMs) {
    *    scheduler.newTimeout(new Connect(dstAddress), delayMs, TimeUnit.MILLISECONDS);
    * */
//    private HashedWheelTimer clientScheduleService;

    /*
    * 메시지 플러그인이 시작시 호출
    * @param storm_conf Storm Configuration
    * */
    @SuppressWarnings("rawtypes")
    public void prepare(Map storm_conf) {
        this.storm_conf = storm_conf;
        connections = new HashMap<>();
        ThreadFactory workerFactory = new JxioRenameThreadFactory("[Client-connection-scheduler]");
        clientScheduleService = new ScheduledThreadPoolExecutor(
                Utils.getInt(storm_conf.get(Config.STORM_MESSAGING_JXIO_CLIENT_WORKER_THREADS)),
                workerFactory);
        isEnablePortal = Utils.getBoolean(storm_conf.get(Config.STORM_MESSAGING_JXIO_PORTAL_HANDLER), true);

    }

    /*
    * 메시지 플러그인을 종료할 때 호출
    *
    * WorkerState의 Shutdown() 함수에서 다음과 같이 호출
    * workerState.mqContext.term();
    * */
    @Override
    public synchronized void term() {
        clientScheduleService.shutdown();

        for (IConnection conn : connections.values()) {
            conn.close();
        }

        connections = null;

//        //we need to release resources associated with client channel factory
//        clientChannelFactory.releaseExternalResources();


    }

    /*
    * 서버측에서 호출 소켓 생성?
    * @param storm_id topology ID
    * @param port 아마도 supervisor.slots에 명시된 포트를 사용할 것
    *
    * WorkerState의 생성자에서 다음과 같이 호출
    * this.receiver = this.mqContext.bind(topologyId, port);
    * -> bind는 여기서 Server 객체를 생성함으로써 수행한다.
    * */
    @Override
    public synchronized IConnection bind(String storm_id, int port) {
        IConnection server = null;
        if(!isEnablePortal) {
            server = new ServerNoPortal(storm_conf, port);
        } else {
            server = new Server(storm_conf, port);
        }
        connections.put(key(storm_id, port), server);
        return server;
    }

    /*
    * 서버에 연결을 요청
    * @param storm_id topology ID
    * @param host server IP
    * @param port server port
    *
    * WorkerState에서 refreshconnection 함수를 통해 주기적으로 서버와의 연결을 재설정
    * task.refresh.poll.secs, ZK에서 할당이 변경될 떄마다 호출
    * -> connection은 여기서 Client 객체를 생성함으로써 수행한다.
    * */
    @Override
    public synchronized IConnection connect(String storm_id, String host, int port) {
        //서버 객체의 ip, port와 겹칠 경우 서버 객체를 리턴??
        IConnection connection = connections.get(key(host,port));
        if(connection != null)
        {
            return connection;
        }
        //스케줄러는 필요할꺼 같다..
        IConnection client = new Client(storm_conf, clientScheduleService, host, port, this);
        return client;
    }

    synchronized void removeClient(String host, int port) {
        if (connections != null) {
            connections.remove(key(host, port));
        }
    }

    private String key(String host, int port) {
        return String.format("%s:%d", host, port);
    }
}


