package ring;

import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class Node implements Runnable{
	private ConnectionFactory factory;
	private String queueNameRecv;
	private String queueNameSend;
	private Channel recv;
	private Channel send;
	private boolean initiator;
	private int id;
	
	public Node(String queueNameRecv, String queueNameSend, boolean initiator, int id) throws Exception{
		this.factory = new ConnectionFactory();
		this.factory.setHost("localhost");
		this.recv = this.factory.newConnection().createChannel();
		this.send = this.factory.newConnection().createChannel();
		this.queueNameRecv = queueNameRecv;
		this.queueNameSend = queueNameSend;
		this.recv.queueDeclare(this.queueNameRecv, false, false, false, null);
		this.send.queueDeclare(this.queueNameSend, false, false, false, null);
		this.initiator = initiator;
		this.id = id;
	}
	
	@Override
	public void run() {
		try {
			if (initiator){
				send("ELECTION;"+id);
				initiator = false;
			}
			receive();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void receive() throws Exception{
		System.out.println(" ["+this.id+"] En attente d'un message");
		Consumer consumer = new DefaultConsumer(recv) {
		  @Override
		  public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
		      throws IOException {
				try {
					String msg = new String(body, "UTF-8");
					System.out.println(" ["+id+"] Receive message "+msg);
					if(msg.startsWith("ELECTION;")){
						int precID = Integer.parseInt(msg.split(";")[1]);
						if (precID != id){
							if (precID > id)
								precID = id;
							send("ELECTION;"+precID);
						} else {
							initiator = true;
							send("HELLO le gollum!");
						}
					} else if(!initiator){
						send(msg);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
		  }
		};
		recv.basicConsume(this.queueNameRecv, true, consumer);
	}
	
	private void send(String message) throws Exception{
	    send.basicPublish("", this.queueNameSend, null, message.getBytes("UTF-8"));
	    System.out.println(" ["+this.id+"] Sent '" + message + "'");
	}
	
}
