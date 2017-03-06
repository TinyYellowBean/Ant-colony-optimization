
import java.util.Random;
import java.util.ArrayList;

public class Ant {
	private ArrayList<Integer> tabu;
	private ArrayList<Integer> allowedCities;
	private double[][] delta;
	private double[][] distance;
	private double[][] eta;

	private double alpha;
	private double beta;
	private int Q;
	private double tourLength;
	private final int cityNum = 250;
	private int firstCity;
	private int currentCity;
	private int currentDemand = 0;
	private int tot_demand = 0;

	/**
	 * 
	 * 
	 * @param cityNum
	 */
	public Ant() {
		tourLength = 0.0;
	}

	/**
	 * 
	 * 
	 * @param distance
	 * @param alpha
	 * @param beta
	 */
	public void init(double[][] distance, double alpha, double beta, int Q) {
		this.alpha = alpha;
		this.beta = beta;
		this.Q = Q;
		this.distance = distance;

		tabu = new ArrayList<Integer>();

		delta = new double[cityNum][cityNum];
		allowedCities = new ArrayList<Integer>();
		for (int i = 0; i < cityNum; i++) {
			allowedCities.add(i);
			for (int j = 0; j < cityNum; j++) {
				delta[i][j] = 0.0;
			}
		}

		firstCity = 0;

		for (Integer integer : allowedCities) {
			if (integer.intValue() == firstCity) {
				allowedCities.remove(integer);
				break;
			}
		}

		tabu.add(firstCity);

		currentCity = firstCity;

		eta = new double[cityNum][cityNum];
		for (int i = 0; i < cityNum; i++) {
			eta[i][i] = 0;
			for (int j = i + 1; j < cityNum; j++) {
				eta[i][j] = 1.0 / distance[i][j];
				eta[j][i] = eta[i][j];
			}
		}
		eta[cityNum - 1][cityNum - 1] = 0;

	}

	/**
	 * 
	 * 
	 * @param pheromone
	 */
	public int selectNextCity(double[][] pheromone, int[] demand, int tot_demand, int g) {
		double[] probability = new double[cityNum]; 
		double sum = 0;
		int flag = 1;
		double q0 = 0.9;// Judge requirement of demand

		if (g >= 2500 && g < 3500) {
			alpha = 5;
			beta = 5;
			q0 = 0.6;

		}

		else if (g >= 3500) {
			alpha = 1;
			beta = 10;
			q0 = 0.2;

		}
		for (int i : allowedCities) {

			sum += Math.pow(pheromone[currentCity][i], alpha) * Math.pow(eta[currentCity][i], beta);

		}

		for (int i = 0; i < cityNum; i++) {
			if (allowedCities.contains(i)) {

				probability[i] = Math.pow(pheromone[currentCity][i], alpha) * Math.pow(eta[currentCity][i], beta) / sum;

			} else {
				probability[i] = 0;
			}
		}

		int selectCity = 0;
		Random random = new Random();
		double rand = random.nextDouble();
		Random random1 = new Random();
		double rand2 = random1.nextDouble();
		double sumPs = 0.0;
		for (int i = 0; i < cityNum; i++) {
			if (rand2 <= q0) {
				int max_index = 0;
				double max = 0;
				for (int j : allowedCities) {

					if (max < Math.pow(pheromone[currentCity][j], alpha) * Math.pow(eta[currentCity][j], beta)) {
						max = Math.pow(pheromone[currentCity][j], alpha) * Math.pow(eta[currentCity][j], beta);
						max_index = j;
					}

				}
				currentDemand += demand[max_index];
				if (currentDemand <= 500) {

					flag = 1;
					selectCity = max_index;
					tabu.add(selectCity);
					break;
				} else {
					flag = 2;
					currentDemand = 0;
					break;
				}
			} else {
				sumPs += probability[i];
				if (sumPs >= rand) {
					currentDemand += demand[i];
					if (currentDemand <= 500) {

						flag = 1;
						selectCity = i;
						tabu.add(selectCity);
						break;
					} else {
						flag = 2;
						currentDemand = 0;
						break;
					}
				}
			}

		}

		for (Integer i : allowedCities) {
			if (i.intValue() == selectCity) {
				allowedCities.remove(i);
				break;
			}
		}

		currentCity = selectCity;

		return flag;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	private double calculateTourLength(int listSize) {
		double length = 0;

		for (int i = 0; i < listSize - 1; i++) {
			length += distance[tabu.get(i)][tabu.get(i + 1)];
		}

		return length;
	}

	public ArrayList<Integer> getAllowedCities() {
		return allowedCities;
	}

	public void setAllowedCities(ArrayList<Integer> allowedCities) {
		this.allowedCities = allowedCities;
	}

	public double getTourLength(int listSize) {
		int l = listSize;
		tourLength = calculateTourLength(l);
		return tourLength;
	}

	public void setTourLength(int tourLength) {
		this.tourLength = tourLength;
	}

	public int getCityNum() {
		return cityNum;
	}

	public ArrayList<Integer> getTabu() {
		return tabu;
	}

	public void setTabu(ArrayList<Integer> tabu) {
		this.tabu = tabu;
	}

	public double[][] getDelta() {
		return delta;
	}

	public void setDelta(double[][] delta) {
		this.delta = delta;
	}

	public int getFirstCity() {
		return firstCity;
	}

	public void setFirstCity(int firstCity) {
		this.firstCity = firstCity;
	}
}
