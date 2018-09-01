package knu.cs.dke.topology_manager.topolgoies;

public class KalmanFilteringTopology extends ASamplingFilteringTopology {

	private double a_val;
	private double q_val;	
	private double h_val;
	private double x_val;
	private double p_val;
	private double r_val;
	
	private int target;
	
	public KalmanFilteringTopology(String createdTime, String plan, int index, String topologyType,
			Double a_val, Double q_val, Double h_val, Double x_val, Double p_val, Double r_val, int target) {
		super(createdTime, plan, index, topologyType);

		this.a_val = a_val;
		this.q_val = q_val;
		this.h_val = h_val;
		this.x_val = x_val;
		this.p_val = p_val;
		this.r_val = r_val;
				
		this.target = target;	
	}

	public double getQ_val() {
		return q_val;
	}

	public void setQ_val(double q_val) {
		this.q_val = q_val;
	}

	public double getR_val() {
		return r_val;
	}

	public void setR_val(double r_val) {
		this.r_val = r_val;
	}

	public int getTarget() {
		return target;
	}

	public void setTarget(int target) {
		this.target = target;
	}

	public double getA_val() {
		return a_val;
	}

	public void setA_val(double a_val) {
		this.a_val = a_val;
	}

	public double getH_val() {
		return h_val;
	}

	public void setH_val(double h_val) {
		this.h_val = h_val;
	}

	public double getX_val() {
		return x_val;
	}

	public void setX_val(double x_val) {
		this.x_val = x_val;
	}

	public double getP_val() {
		return p_val;
	}

	public void setP_val(double p_val) {
		this.p_val = p_val;
	}
}
