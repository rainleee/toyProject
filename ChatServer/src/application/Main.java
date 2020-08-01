package application;
	
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;


public class Main extends Application {
	
	//여러개의 쓰레드를 안전하게 사용하게 해줄수있는 대표적인 lib
	public static ExecutorService threadPool;
	public static Vector<Client> clients = new Vector<Client>();
	
	ServerSocket serverSocket;
	
	//서버를 구동시켜서 클라이언트의 연결을 기다리는 메소드
	public void startServer(String IP, int port) {
		try {
			//서버소켓 객체를 생성 후 바인드해주기.
			serverSocket = new ServerSocket();
			serverSocket.bind( new InetSocketAddress(IP, port) );
		} catch (Exception e) {
			try {
				e.printStackTrace();
				//만약 서버소켓이 닫혀있지 않다면 
				if (!serverSocket.isClosed()) {
					stopServer();
				}
				return;
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		
		//클라이언트가 접속할 때까지 계속 기다린느 쓰레드입니다.
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Socket socket = serverSocket.accept();
						clients.add(new Client(socket));
						System.out.println("[클라이언트 접속] "
								+ socket.getRemoteSocketAddress()
								+ " : " + Thread.currentThread().getName());
					} catch (Exception e) {
						//만약 서버소켓이 닫혀있지 않다면, 즉 비정상적으로 됐다면
						if (!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}
				}
				
			}
		};
		//쓰레드풀 초기화 
		threadPool = Executors.newCachedThreadPool();
		threadPool.submit(thread);
	}
	
	//서버의 작동을 중지시키는 메소드 
	public void stopServer() {
		try {
			//현재 작동중인 모든 소켓 닫기
			Iterator<Client> iterator = clients.iterator();
			while (iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			
			//서버 소켓 객체 닫기 
			if (serverSocket != null && serverSocket.isClosed()) {
				serverSocket.close();
			}
			
			//쓰레드 풀 종료 
			if (threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdown();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void start(Stage primaryStage) {
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
