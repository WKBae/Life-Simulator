package net.wkbae.lifesimulator;

public interface SimulationListener {
	
	public void simulationStarted(Simulation simulation);
	
	public void simulationPainterUpdated(Simulation simulation, SimulationPainter painter);
	
	public void simulationSettingChanged(Simulation simulation, SimulationSetting setting);
	
	public void simulationStopped(Simulation simulation);
	
	public void simulationFinishing(Simulation simulation);
	
}
