
import java.io.IOException;

public class cvrp {
	public static void main(String[] args) throws IOException {

		final int antNum = 50;
		final int generation = 6000;
		final double alpha = 10;
		final double beta = 10;
		final double rho = 0.7;
		final int Q = 10;
		final int deltaType = 1;
		ACO aco = new ACO(antNum, generation, alpha, beta, rho, Q, deltaType);
		aco.init();
		aco.solve();

	}

}
