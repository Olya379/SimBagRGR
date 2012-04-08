/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package actors;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.text.JTextComponent;
import process.Actor;
import process.Dispatcher;
import queues.Store;
import rnd.Randomable;
import stat.Histo;
import widgets.Diagram;

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
    private int modelingTime;
    private int flightNum;
    private ArrayList<Passenger> passengers;
    private Histo histo;
    private JTextComponent textOutput;
    private int maxPlanecount;
    private Store bagCountInAirport;   //один пассажир привозит с собой n единиц багажа
    //хранит количество багажа в аэропорту

    public Airport(int modelTime, int maxPlaneCnt, Randomable plane,
            Randomable passenger, Diagram passengerDiagram,
            Diagram statDiagram, Diagram bagDiagramm,
            JTextComponent text, Randomable bag, Dispatcher dsp) {
        passengersCount = 0;
        flightNum = 0;

        dispatcher = dsp;
        planeTimeGenerator = plane;
        passangersGenerator = passenger;
        passDiagram = passengerDiagram;
        statisticDiagram = statDiagram;
        modelingTime = modelTime;
        textOutput = text;
        maxPlanecount = maxPlaneCnt;
        bagGenerator = bag;
        passengers = new ArrayList();
        bagCountInAirport = new Store();
        bagCountInAirport.init();
        bagCountInAirport.setDispatcher(dsp);

    }

    public void passengerCame() {
        passDiagram.getPainter().drawToXY((float) dispatcher.getCurrentTime(), ++passengersCount);
        System.out.println("Количество пассажиров в аэропорту: " + passengersCount);
    }

    public void passengerLeft() {
        passDiagram.getPainter().drawToXY((float) dispatcher.getCurrentTime(), --passengersCount);
        System.out.println("Количество пассажиров в аэропорту: " + passengersCount);
    }

    public double getBagCountInAirport() {
        return bagCountInAirport.getSize();
    }

    public boolean getBagFromAirport() {
        if (bagCountInAirport.getSize() > 0) {
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
            System.out.println("Прибыл самолет с : " + arrivals + " пассажирами на борту");
            //  passengersCount += arrivals;
            for (int i = 0; i < arrivals; i++) {
                double bagTmp = bagGenerator.next();
                bagCountInAirport.add(bagTmp);
                Passenger pass = new Passenger(bagTmp);
                pass.setNameForProtocol("Passenger " + (i + 1) + " from flight " + flightNum);
                passengers.add(pass);
                dispatcher.addStartingActor(pass);
            }
        }
        drawDiagram();
    }

    public Histo getHisto() {
        return histo;
    }

    private void drawDiagram() {
        /*
         * Тут мы построим гистограмму времени проведденного в аэропорту
         *
         */
        histo = new Histo(0, modelingTime, modelingTime / 10);
        for (Iterator<Passenger> it = passengers.iterator(); it.hasNext();) {
            Passenger passenger = it.next();
            if(passenger.getStayTime() == 0){
                continue;
            }
            histo.add(passenger.getStayTime());
        }
        histo.showRelFrec(statisticDiagram);
        textOutput.setText(histo.toString());
    }
}
