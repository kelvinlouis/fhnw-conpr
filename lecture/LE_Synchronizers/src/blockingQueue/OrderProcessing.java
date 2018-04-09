package blockingQueue;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class OrderProcessing {
	
	public static void main(String[] args) {
		int nCustomers = 10;
		int nValidators = 2;
		int nProcessors = 3;

		final BlockingQueue<Order> validatorQueue = new LinkedBlockingQueue<>();
		final BlockingQueue<Order> processingQueue = new LinkedBlockingQueue<>();

		for (int i = 0; i < nCustomers; i++) {
			new Customer("" + i, validatorQueue).start();
		}

		for (int i = 0; i < nValidators; i++) {
			new OrderValidator(validatorQueue, processingQueue).start();
		}

		for (int i = 0; i < nProcessors; i++) {
			new OrderProcessor(validatorQueue).start();
		}
	}
	
	static class Order {
		public final String customerName;
		public final int itemId;
		public Order(String customerName, int itemId) {
			this.customerName = customerName;
			this.itemId = itemId;
		}
		
		@Override
		public String toString() {
			return "Order: [name = " + customerName + " ], [item = " + itemId +" ]";  
		}
	}
	
	
	static class Customer extends Thread {
		private final BlockingQueue<Order> queue;

		public Customer(String name, BlockingQueue<Order> queue) {
			super(name);
			this.queue = queue;
		}
		
		private Order createOrder() {
			Order o = new Order(getName(), (int) (Math.random()*100));
			System.out.println("Created:   " + o);
			return o;
		}
		
		private void handOverToValidator(Order o) throws InterruptedException {
			queue.put(o);
		}
		
		@Override
		public void run() {
			try {
				while(true) {
					Order o = createOrder();
					handOverToValidator(o);
					Thread.sleep((long) (Math.random()*1000));
				}
			} catch (InterruptedException e) {}
		}
	}
	
	
	static class OrderValidator extends Thread {
		BlockingQueue<Order> incomingQueue;
		BlockingQueue<Order> outgoingQueue;

		public OrderValidator(BlockingQueue<Order> iQueue, BlockingQueue<Order> oQueue) {
			incomingQueue = iQueue;
			outgoingQueue = oQueue;
		}
		
		public Order getNextOrder() throws InterruptedException {
			return incomingQueue.take();
		}
		
		public boolean isValid(Order o) {
			return o.itemId < 50;
		}
		
		public void handOverToProcessor(Order o) throws InterruptedException {
			outgoingQueue.put(o);
		}
		
		@Override
		public void run() {
			try {
				while(true) {
					Order o = getNextOrder();
					if(isValid(o)) {
						handOverToProcessor(o);
					} else {
						System.err.println("Destroyed: " + o);
					}
					Thread.sleep((long) (Math.random()*1000));
				}
			} catch (InterruptedException e) {}
		}
	}
	
	
	static class OrderProcessor extends Thread {
		BlockingQueue<Order> queue;
		public OrderProcessor(BlockingQueue<Order> queue) {
			this.queue = queue;
		}
		
		public Order getNextOrder() throws InterruptedException {
			return queue.take();
		}
		
		public void processOrder(Order o) {
			System.out.println("Processed: " + o);
		}
		
		@Override
		public void run() {
			try {
				while(true) {
					Order o = getNextOrder();
					processOrder(o);
					Thread.sleep((long) (Math.random()*1000));
				}
			} catch (InterruptedException e) {}
		}
	}
}
