package htlhallein.at.clientdatenbrille.htlhallein.at.clientdatenbrille.parallel;

public class ParallelTest {
    int k;

    public ParallelTest() {
        k = 0;
        Parallel.For(0, 10, new LoopBody<Integer>() {
            public void run(Integer i) {
                k += i;
                System.out.println(i);
            }
        });
        System.out.println("Sum = " + k);
    }
}