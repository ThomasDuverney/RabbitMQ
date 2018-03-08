package ring;

import java.util.HashMap;
import java.util.Random;

public class Main {

	public static void main(String[] args) throws Exception{
		int nbNodes = 50;
		HashMap<Integer, Thread> threadMap = new HashMap<Integer, Thread>();
		if (args.length > 1){
			try {
				nbNodes = Integer.parseInt(args[1]);
			} catch(NumberFormatException e){
				System.out.println("Format incorrect : "+args[1]+" n'est pas un integer.");
			}
		}
		Random r = new Random();
		Thread t;
		for(int i=0; i<nbNodes; i++){
			int index = r.nextInt(nbNodes*10);
			t = new Thread(new Node(i+"", ((i+1)%nbNodes)+"", i==nbNodes-1, index));
			threadMap.put(index, t);
			t.start();
		}
	}

}
