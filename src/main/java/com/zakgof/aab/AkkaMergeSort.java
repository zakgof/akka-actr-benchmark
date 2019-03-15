package com.zakgof.aab;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class AkkaMergeSort {

	public static void main(String[] args) throws InterruptedException {

		final ActorSystem system = ActorSystem.create("akkasort");
		
		Random random = new Random(0L);
		int[] input = IntStream.range(0, 1 << 20).map(i -> random.nextInt()).toArray();
		
		final ActorRef master = system.actorOf(MasterActor.props(), "master");
		master.tell(input, ActorRef.noSender());
		
		Thread.sleep(60000);
	}
	
	private static class ResultMessage {
		
		private final int[] array;
		private final int side;
		
		ResultMessage(int[] array, int side) {
			this.array = array; 
			this.side = side;
		}
	}

	private static class MasterActor extends AbstractActor {

		static public Props props() {
			return Props.create(MasterActor.class, () -> new MasterActor());
		}

		private long start;

		@Override
		public Receive createReceive() {
			return receiveBuilder()
			    .match(int[].class, this::start) 
				.match(ResultMessage.class, this::finish)
				.build();
		}

		private void start(int[] array) {
			System.err.println("Started !!! ");
			start = System.currentTimeMillis();
			ActorRef sorter = context().actorOf(Sorter.props(-1), "c");
			sorter.tell(array, self());
		}
		
		private void finish(ResultMessage result) {
			long end = System.currentTimeMillis();
			System.err.println("AKKA Finished !!! " + (end - start)); // + "   " + Arrays.toString(result.getArray()));
			System.exit(0);
		}
	}

	private static class Sorter extends AbstractActor {

		private final int side;
		private int[][] res = new int[2][];
		private ActorRef upstream;
		
		public Sorter(int side) {
			this.side = side;
		}

		static public Props props(int side) {
			return Props.create(Sorter.class, () -> new Sorter(side));
		}

		@Override
		public Receive createReceive() {
			return receiveBuilder()
				.match(int[].class, this::run)
				.match(ResultMessage.class, this::result)
				.build();
		}

		private void run(int[] array) {
			upstream = sender();
			if (array.length == 1)
				upstream.tell(new ResultMessage(array, side), self());
			else {
				int[] left  = Arrays.copyOfRange(array, 0, array.length / 2);
				int[] right = Arrays.copyOfRange(array, array.length / 2, array.length);
				
				ActorRef a = context().actorOf(Sorter.props(0), "a");
				ActorRef b = context().actorOf(Sorter.props(1), "b");
				
				a.tell(left, self());
				b.tell(right, self());
			}
		}
		
		private void result(ResultMessage result) {
			res[result.side] = result.array;
			if (res[0] != null && res[1] != null) {
				int[] resultarray = merge(res[0], res[1]);
				upstream.tell(new ResultMessage(resultarray, side), self());
			}
		}

		public static int[] merge(int[] a, int[] b) {
			int[] answer = new int[a.length + b.length];
			int i = a.length - 1, j = b.length - 1, k = answer.length;
			while (k > 0)
				answer[--k] = (j < 0 || (i >= 0 && a[i] >= b[j])) ? a[i--] : b[j--];
			return answer;
		}
	
	}

}
