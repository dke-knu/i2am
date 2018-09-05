package sub;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class PlanManager {

    public static void main(String[] args) {
        Socket socket = null;
        Scanner input = new Scanner(System.in);
        String message = null;

            try {
                socket = new Socket();
                System.out.println("[연결 요청]");
                socket.connect(new InetSocketAddress("localhost", 5004));
                System.out.println("[연결 성공]");

                DataOutputStream os = new DataOutputStream(socket.getOutputStream());

                while(true) {

                    System.out.print("[보낼 메시지를 입력하세요]: ");
                    //message = input.nextLine();
                    message = "{\"decision\":\"deletion\","
                            + "\"topic\":\"hajin_src\""
                            + "}";

                    os.writeUTF(message);
                    os.flush();
                    System.out.println("[데이터 보내기 성공]");
                }

            } catch (Exception e) {
            }

            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e1) {
                }

            }
    }
}
