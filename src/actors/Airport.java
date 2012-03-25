/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package actors;

import simulation.process.Actor;
import simulation.process.Dispatcher;
import simulation.rnd.Randomable;
import simulation.widgets.Diagram;

/**
 *
 * @author lordgal
 */
public class Airport extends Actor {

    private static Randomable planeTimeGenerator;
    private static Randomable passangersGenerator;
    private int passengersCount;
    private Dispatcher dispatcher;
    private Diagram passDiagram;
    
    public Airport(Randomable palne, Randomable passenger, Diagram passengerDiagram){
        dispatcher = new Dispatcher();
        passengersCount = 0;        
        passDiagram = passengerDiagram;
        Passenger._init();
    }
    
    
    public void passengerCame() {
	   passDiagram.getPainter().drawToXY((float) dispatcher.getCurrentTime(), ++passengersCount);
	}
	
	public void passengerLeft() {
		passDiagram.getPainter().drawToXY((float) dispatcher.getCurrentTime(), --passengersCount);
	}
    
    @Override
    protected void rule() {
        holdForTime(planeTimeGenerator.next());
        
    }
    
}
