import java.net.*;
import java.io.*;

public class NetBase {
    protected Socket socket;
    private BufferedReader br;
    private PrintWriter pw;

    public NetBase(Socket s) throws IOException {
        socket = s;

        InputStream is = socket.getInputStream();
        br = new BufferedReader(new InputStreamReader(is));
        OutputStream os = socket.getOutputStream();
        pw = new PrintWriter(os);
    }

    public void println(String str) {
        pw.println(str);
        pw.flush();
    }

    public String readLine() {
        try {
            return br.readLine();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
