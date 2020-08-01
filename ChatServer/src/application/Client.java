package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {

	Socket socket;

	// Constructor
	public Client() {
	}

	public Client(Socket socket) {
		this.socket = socket;
		receive();
	}

	// getRemote는 ip주소를 출력하는 메소드
	// 클라이언트로부터 메세지를 전달받는 메소드입니다.
	private void receive() {
		Runnable thread = new Runnable() {
			// run을 가지고 있어야
			@Override
			public void run() {
				try {
					while (true) {
						InputStream in = socket.getInputStream();
						byte[] buffer = new byte[512];
						int length = in.read(buffer);

						// length가 -1이면 에러가 난것이므로 Exception 발생
						while (length == -1)
							throw new IOException();
						System.out.println("[메세지 수신 성공] " + socket.getRemoteSocketAddress() + " : "
								+ Thread.currentThread().getName());

						// chaSet을 해주는 constructor
						String message = new String(buffer, 0, length, "UTF-8");
						// 받은 메세지를 클라이언트의 수만큼 보내준다.
						for (Client client : Main.clients) {
							client.send(message);
						}
					}
				} catch (Exception e) {
					try {
						System.out.println("[메세지 수신 오류]" + socket.getRemoteSocketAddress() + " : "
								+ Thread.currentThread().getName());
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}

			}
		};
		Main.threadPool.submit(thread);

	}

	// 클라이언트에게 메세지를 전송하는 메소드입니다.
	private void send(String message) {

		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					// 현재 버퍼에 저장되어 있는 내용을 클라이어느로 전송하고 버퍼를 비운다.
					out.flush();
				} catch (Exception e) {
					try {
						System.out.println("[메세지 송신 오류]" + socket.getRemoteSocketAddress() + " : "
								+ Thread.currentThread().getName());
						// 여기있는 Client 객체는 다 지워라.
						Main.clients.remove(Client.this);
						socket.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		
		Main.threadPool.submit(thread);
	}

}
