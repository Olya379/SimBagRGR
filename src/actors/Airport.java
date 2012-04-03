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
import simulation.queues.QueueForTransactions;
import simulation.queues.Store;
import simulation.rnd.Erlang;
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
    private static Randomable bagGenerator;
    private int passengersCount;
    private Dispatcher dispatcher;
    private Diagram passDiagram;
    private Diagram statisticDiagram;
    private Diagram bagWaitDiagram;
    private int modelingTime;
    private int flightNum;
    private ArrayList<Passenger> passengers;
    private Histo histo;
    private JTextComponent textOutput;
    private int maxPlanecount;
    private QueueForTransactions bagQue;
    private Store bagCountInAirport;   //один пассажир привозит с собой одну единицу багажа
                            //хранит количество багажа в аэропорту

    public Airport( int modelTime, int maxPlaneCnt, Randomable palne, 
                    Randomable passenger, Diagram passengerDiagram, 
                    Diagram statDiagram, Diagram bagDiagramm, 
                    JTextComponent text, Randomable bag
            ) {
        dispatcher = new Dispatcher();
        bagQue = new QueueForTransactions();
        passengersCount = 0;
        flightNum = 0;
        passDiagram = passengerDiagram;
        statisticDiagram = statDiagram;
        bagWaitDiagram = bagDiagramm;
        modelingTime = modelTime;
        textOutput = text;
        maxPlanecount = maxPlaneCnt;
        bagGenerator = bag;
        passengers = new ArrayList();
        Passenger._init(new Erlang(),bagQue,this);

    }

    public void passengerCame() {
        passDiagram.getPainter().drawToXY((float) dispatcher.getCurrentTime(), ++passengersCount);
    }

    public void passengerLeft() {
        passDiagram.getPainter().drawToXY((float) dispatcher.getCurrentTime(), --passengersCount);
    }

    public double getBagCountInAirport(){
        return bagCountInAirport.getSize();
    }
    
    public boolean getBagFromAirport(){
         if(bagCountInAirport.getSize() > 0){
            bagCountInAirport.remove(1);
            return true;
        }
        return false;
    }
    
    
    @Override
    protected void rule() {
        while (dispatcher.getCurrentTime() < modelingTime && flightNum <= maxPlanecount) {
            holdForTime(planeTimeGenerator.next());
            flightNum++;
            double arrivals = passangersGenerator.next();
            passengersCount += arrivals;
            for (int i = 0; i < arrivals; i++) {
                double bagTmp = bagGenerator.next();
                bagCountInAirport.add(bagTmp);
                Passenger pass = new Passenger(bagTmp);
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
