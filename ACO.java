

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;

public class ACO {
	private Ant[] ants = null; // ant
	private final int cityNum = 250; // the number of city

	private int[] x; // X
	private int[] y; // Y
	private int[] demand;
	private static int tot_demand = 0;
	private double[][] distance; // distance matrix
	private double[][] pheromone; // pheromone matrix

	private double bestLength; // best distance
	private int[] bestTour; // best path
	private int[] optTour;
	private static int antNumber = 0;
	private int antNum; // the number of ant
	private int bestAntNum;
	private int recordPath[] = new int[50];
	private int generation; // the number of iteration
	private double alpha;
	private double beta;

	private double rho;
	private int Q;
	private int deltaType; // 0: Ant-quantity; 1: Ant-density; 2: Ant-cycle

	/**
	 * 
	 * 
	 * @param cityNum
	 * @param antNum
	 * @param generation
	 * @param alpha
	 * @param beta
	 * @param rho
	 * @param Q
	 */
	public ACO(int antNum, int generation, double alpha, double beta, double rho, int Q, int deltaType) {
		this.antNum = antNum;
		this.generation = generation;
		this.alpha = alpha;
		this.beta = beta;
		this.rho = rho;
		this.Q = Q;
		this.deltaType = deltaType;
		ants = new Ant[antNum];
	}

	/**
	 * 
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public void init() throws IOException {
		// get X Y demand
		x = Store.getX();
		y = Store.getY();
		demand = Store.getDemand();
		// get distance matrix
		getDistance(x, y);
		for (int i = 0; i < cityNum; i++) {
			tot_demand += demand[i];
		}

		pheromone = new double[cityNum][cityNum];
		double start = 1.0 / ((cityNum - 1) * antNum);
		for (int i = 0; i < cityNum; i++) {
			for (int j = 0; j < cityNum; j++) {
				pheromone[i][j] = start;
			}
		}

		// initial
		bestLength = Integer.MAX_VALUE;
		bestTour = new int[cityNum + 30];

		for (int i = 0; i < antNum; i++) {
			ants[i] = new Ant();
			ants[i].init(distance, alpha, beta, Q);
		}
	}

	/**
	 * 
	 * 
	 * @param x
	 * @param y
	 * @throws IOException
	 */
	private void getDistance(int[] x, int[] y) throws IOException {
		distance = new double[cityNum][cityNum];
		for (int i = 0; i < cityNum - 1; i++) {
			distance[i][i] = 0;
			for (int j = i + 1; j < cityNum; j++) {
				distance[i][j] = Math.sqrt((x[i] - x[j]) * (x[i] - x[j]) + (y[i] - y[j]) * (y[i] - y[j]));
				distance[j][i] = distance[i][j];
			}
		}
		distance[cityNum - 1][cityNum - 1] = 0;
	}

	/**
	 * solve CVRP
	 */
	public void solve() {
		int flag = 1;
		int flag2 = 0;
		int iteration = 1;
		int max = 0, min = 0;
		for (int g = 0; g < generation; g++) {
			antNumber = 0;
			for (int i = 0; i < cityNum; i++) {

				if (flag == 1 && ants[antNumber].getAllowedCities().size() != 0) {
					flag = ants[antNumber].selectNextCity(pheromone, demand, tot_demand, g);

				} else if (flag == 2) {
					i--;
					/*
					 * if (antNumber != 0) { Random random = new Random(); int x
					 * = random.nextInt(ants[antNumber].getTabu().size() - 1);
					 * int y = random.nextInt(ants[antNumber].getTabu().size() -
					 * 1); while (x == y) y =
					 * random.nextInt(ants[antNumber].getTabu().size() - 1); int
					 * temp = ants[antNumber].getTabu().get(x);
					 * ants[antNumber].getTabu().set(x,
					 * ants[antNumber].getTabu().get(y));
					 * ants[antNumber].getTabu().set(y, temp); }
					 */
					ants[antNumber].getTabu().add(ants[antNumber].getFirstCity());
					antNumber++;
					ants[antNumber].getTabu().clear();
					ants[antNumber].getTabu().addAll(ants[antNumber - 1].getTabu());
					ants[antNumber].getAllowedCities().clear();
					ants[antNumber].getAllowedCities().addAll(ants[antNumber - 1].getAllowedCities());
					flag = ants[antNumber].selectNextCity(pheromone, demand, tot_demand, g);

				} else if (ants[antNumber].getAllowedCities().size() == 0)
					break;
			}
			/***********/
			double recordDistance[] = new double[antNumber + 1];
			for (int i = 0; i <= antNumber; i++) {
				if (i == 0) {
					recordDistance[i] = ants[i].getTourLength(ants[i].getTabu().size());

				} else {
					recordDistance[i] = ants[i].getTourLength(ants[i].getTabu().size())
							- ants[i - 1].getTourLength(ants[i - 1].getTabu().size());

				}
			}
			for (int i = 0; i <= antNumber; i++) {
				if (recordDistance[min] > recordDistance[i]) {
					min = i;

				}

			}

			for (int i = 0; i <= antNumber; i++) {

				if (recordDistance[max] < recordDistance[i]) {
					max = i;

				}

			}

			/***********/

			double tot_distance = 0.0;

			ants[antNumber].getTabu().add(ants[antNumber].getFirstCity());

			tot_distance = ants[antNumber].getTourLength(ants[antNumber].getTabu().size());
			if (tot_distance < bestLength) {
				flag2 = 1;
				bestLength = tot_distance;
				bestAntNum = antNumber;
				int k;
				for (int j = 0; j <= antNumber; j++) {

					if (j == 0) {
						for (k = 0; k < ants[j].getTabu().size(); k++) {

							bestTour[k] = ants[j].getTabu().get(k).intValue();

						}
					} else {
						for (k = (ants[j - 1].getTabu().size()); k < ants[j].getTabu().size(); k++) {
							bestTour[k] = ants[j].getTabu().get(k).intValue();

						}
					}
					recordPath[j] = ants[j].getTabu().size();

				}

			}

			if (flag2 == 1) {

				// update pheromone matrix
				double[][] delta = ants[antNumber].getDelta();
				double[][] delta1 = ants[antNumber].getDelta();
				if (min != 0 && max != 0) {

					for (int i = 0; i < ants[min - 1].getTabu().size(); i++) {
						ants[min].getTabu().remove(ants[min - 1].getTabu().get(i));

					}
					for (int i = 0; i < ants[max - 1].getTabu().size(); i++) {
						ants[max].getTabu().remove(ants[max - 1].getTabu().get(i));

					}
				} else if (max == 0 && min != 0) {

					for (int i = 0; i < ants[min - 1].getTabu().size(); i++) {
						ants[min].getTabu().remove(ants[min - 1].getTabu().get(i));

					}
				} else if (min == 0 && max != 0) {

					for (int i = 0; i < ants[max - 1].getTabu().size(); i++) {
						ants[max].getTabu().remove(ants[max - 1].getTabu().get(i));

					}
				} else {

					ants[min].getTabu().remove(0);
					ants[max].getTabu().remove(0);
				}

				for (int i = 0; i < cityNum; i++) {
					for (int j : ants[min].getTabu()) {
						if (deltaType == 0) {
							delta[i][j] = Q; // Ant-quantity System
						}
						if (deltaType == 1) {
							delta[i][j] = Q / distance[i][j]; // Ant-density
																// System
						}

						if (deltaType == 2) {
							delta[i][j] = Q / ants[antNumber].getTourLength(ants[antNumber].getTabu().size());
						}
						// Ant-cycle // System }

					}
					for (int j : ants[max].getTabu()) {
						if (deltaType == 0) {
							delta1[i][j] = Q; // Ant-quantity System
						}
						if (deltaType == 1) {
							delta1[i][j] = Q / distance[i][j]; // Ant-density
																// System
						}

						if (deltaType == 2) {
							delta1[i][j] = Q / ants[antNumber].getTourLength(ants[antNumber].getTabu().size());
						}

					}
				}
				ants[min].setDelta(delta);
				ants[max].setDelta(delta1);

				// updatePheromone();
				for (int i = 0; i < cityNum; i++) {
					for (int j = 0; j < cityNum; j++) {
						pheromone[i][j] = pheromone[i][j] * rho;
					}
				}

				for (int i = 0; i < cityNum; i++) {
					for (int j = 0; j < cityNum; j++) {

						pheromone[i][j] += ants[min].getDelta()[i][j];
						pheromone[i][j] -= ants[max].getDelta()[i][j];

					}
				}
			}
			flag2 = 0;
			min = 0;
			max = 0;

			for (int i = 0; i < antNum; i++) {
				ants[i].init(distance, alpha, beta, Q);
			}
			antNumber = 0;
		}

		print();
	}

	private void print() {
		System.out.println("login xp16434 1626031");
		System.out.println("name Pan Xiaodong");
		System.out.println("algorithm Ant Colony Optimization");
		System.out.println("cost " + bestLength);
		for (int i = 0; i <= bestAntNum; i++) {
			if (i == 0) {
				for (int j = 0; j < recordPath[i]; j++) {
					if (j != recordPath[i] - 1) {
						System.out.print(bestTour[j] + 1 + "->");
					} else {
						System.out.print(bestTour[j] + 1 + "\n");
					}
				}
			} else {
				// System.out.println();
				for (int j = recordPath[i - 1] - 1; j < recordPath[i]; j++) {
					if (j != recordPath[i] - 1) {
						System.out.print(bestTour[j] + 1 + "->");
					} else {
						System.out.print(bestTour[j] + 1 + "\n");
					}
				}
			}
		}

	}

	/**
	 * 
	 * 
	 * @return
	 */
	public int[] getBestTour() {
		return bestTour;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public double getBestLength() {
		return bestLength;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public int[] getX() {
		return x;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public int[] getY() {
		return y;
	}
}
