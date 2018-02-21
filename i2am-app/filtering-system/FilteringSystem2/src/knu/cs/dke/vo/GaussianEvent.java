package knu.cs.dke.vo;

public class GaussianEvent {
	private double Numeric = 0.0;
	
	public double getNumeric() {
		return Numeric;
	}

	public void setNumeric(double numeric) {
		Numeric = numeric;
	}

	public GaussianEvent(double numeric) {
		super();
		Numeric = numeric;
	}

}
