/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package actors;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.text.JTextComponent;
import simulation.process.Actor;
import simulation.process.Dispatcher;
import simulation.rnd.Randomable;
import simulation.stat.Histo;
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
    private Diagram statisticDiagram;
    private int modelingTime;
    private int flightNum;
    private ArrayList<Passenger> passengers;
    private Histo histo;
    private JTextComponent textOutput;
    private int maxPlanecount;

    public Airport(int modelTime, int maxPlaneCnt, Randomable palne, Randomable passenger, Diagram passengerDiagram, Diagram statDiagram, JTextComponent text) {
        dispatcher = new Dispatcher();
        passengersCount = 0;
        flightNum = 0;
        passDiagram = passengerDiagram;
        statisticDiagram = statDiagram;
        modelingTime = modelTime;
        textOutput = text;
        maxPlanecount = maxPlaneCnt;
        passengers = new ArrayList();
        Passenger._init();
        Carrier._init();
    }

    public void passengerCame() {
        passDiagram.getPainter().drawToXY((float) dispatcher.getCurrentTime(), ++passengersCount);
    }

    public void passengerLeft() {
        passDiagram.getPainter().drawToXY((float) dispatcher.getCurrentTime(), --passengersCount);
    }

    @Override
    protected void rule() {
        while (dispatcher.getCurrentTime() < modelingTime && flightNum <= maxPlanecount) {
            holdForTime(planeTimeGenerator.next());
            flightNum++;
            double arrivals = passangersGenerator.next();
            passengersCount += arrivals;
            for (int i = 0; i < arrivals; i++) {
                Passenger pass = new Passenger();
                pass.setNameForProtocol("Passenger " + (i + 1) + " from flight " + flightNum);
                passengers.add(pass);
                dispatcher.addStartingActor(pass);
            }
        }
        /*
         * Тут мы построим гистограмму времени проведденного в аэропорту
         * 
         */
        histo = new Histo(0,modelingTime,modelingTime / 10);
        for (Iterator<Passenger> it = passengers.iterator(); it.hasNext();) {
            Passenger passenger = it.next();
            histo.add(passenger.getStayTime());           
        }
        histo.showRelFrec(statisticDiagram);
        textOutput.setText(histo.toString());
    }
    
    public Histo getHisto(){
        return histo;
    }
}
