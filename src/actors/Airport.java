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

    //Промежутки времени между прилетом самолетов
    private static Randomable planeTimeGenerator;
    //количество пассажиров в самолете
    private static Randomable passangersGenerator;
    //количество багажа у пассажира
    private static Randomable bagGenerator;
    //количество пассажиров в аэропорту
    private int passengersCount;
    //диспетчер
    private Dispatcher dispatcher;
    //диаграмма отображающая количество пассажиров в аэропорту
    private Diagram passDiagram;
    //Диаграмма отображения статистики
    private Diagram statisticDiagram;
    //Диаграмма времени в очередях
    private Diagram statisticDiagram_que;
    //время моделирования
    private int modelingTime;
    //номер рейса по счету
    private int flightNum;
    //колекция прибывших пассажиров
    private ArrayList<Passenger> passengers;
    private Histo histo;
    private JTextComponent textOutput;
    private JTextComponent textOutput_que;
    //максимальное число самолетов(в модели используется чисто формально, введено на будущее)
    private int maxPlanecount;
    //хранит количество багажа в аэропорту
    //один пассажир привозит с собой n единиц багажа
    private Store bagCountInAirport;   
    

    public Airport(int modelTime, int maxPlaneCnt, Randomable plane,
            Randomable passenger, Diagram passengerDiagram,
            Diagram statDiagram,Diagram statDiagram_1, Diagram bagDiagramm,
            JTextComponent text,JTextComponent text_1, Randomable bag, Dispatcher dsp) {
        passengersCount = 0;
        flightNum = 0;

        textOutput_que=text_1;
        statisticDiagram_que=statDiagram_1;
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
        //по прибытию пассажир отмечается 
        passDiagram.getPainter().drawToXY((float) dispatcher.getCurrentTime(), ++passengersCount);
        System.out.println("Количество пассажиров в аэропорту: " + passengersCount);
    }

    public void passengerLeft() {
        //и уходя из аэропорта пассажир тоже отмечается
        passDiagram.getPainter().drawToXY((float) dispatcher.getCurrentTime(), --passengersCount);
        System.out.println("Количество пассажиров в аэропорту: " + passengersCount);
    }

    public double getBagCountInAirport() {
        return bagCountInAirport.getSize();
    }

    public boolean getBagFromAirport() {
        //забирает одну единицу багажа из аэропорта
        if (bagCountInAirport.getSize() > 0) {
            bagCountInAirport.remove(1);
            return true;
        }
        return false;
    }

    @Override
    protected void rule() {
        //пока не наступило время завершения моделирования или не превысили
        //количество самолетов
        while (dispatcher.getCurrentTime() < modelingTime && flightNum <= maxPlanecount) {
            //задерживаемся до следующего самолета
            holdForTime(planeTimeGenerator.next());
            //генерируем самолет
            flightNum++;
            //генерируем пассажиров
            double arrivals = passangersGenerator.next();
            System.out.println("Прибыл самолет с : " + arrivals + " пассажирами на борту");
            //Выдаем багаж пассажирам и отдаем их диспетчеру
            for (int i = 0; i < arrivals; i++) {
                double bagTmp = bagGenerator.next();
                bagCountInAirport.add(bagTmp);
                Passenger pass = new Passenger(bagTmp);
                pass.setNameForProtocol("Passenger " + (i + 1) + " from flight " + flightNum);
                passengers.add(pass);
                dispatcher.addStartingActor(pass);
            }
        }
        //считаем статистику и отрисовуем гистограму
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
            //не вижу смысла учитывать пассажиров которых не обслужили
            if(passenger.getStayTime() == 0){
                continue;
            }
            histo.add(passenger.getStayTime());
        }
        histo.showRelFrec(statisticDiagram);
        textOutput.setText(histo.toString());
        
        Histo hst = new Histo(0, modelingTime, modelingTime / 10);
        for (Iterator<Passenger> it = passengers.iterator(); it.hasNext();) {
            Passenger passenger = it.next();
            //не вижу смысла учитывать пассажиров которых не обслужили
            if(passenger.getStayTime() == 0){
                continue;
            }
            hst.add(passenger.getEatTimeSpend());
        }
        hst.showRelFrec(statisticDiagram_que);
        textOutput_que.setText(hst.toString());
    }
}
