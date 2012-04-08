/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package actors;

import java.util.logging.Level;
import java.util.logging.Logger;
import process.Actor;
import process.Dispatcher;
import process.DispatcherFinishException;
import process.IWaitCondition;
import queues.QueueForTransactions;
import rnd.Randomable;

/**
 *
 * @author lordgal
 */
public class Passenger extends Actor {

    private double cameTime;
    private double leftTime;
    // private Dispatcher dispatcher;
    private boolean isGetBag;
    private double bagCnt;
    private int givenBag;
    private boolean isProcessed;
    private static Randomable eatTime;
    private static QueueForTransactions bagQueue;
    private static Airport airport;

    public static void _init(Randomable eat, QueueForTransactions bagQ, Airport port) {
        // think = brain;
        eatTime = eat;
        bagQueue = bagQ;
        airport = port;

    }

    public Passenger(double bagC) {
        super();
        isGetBag = false;
        bagCnt = bagC;
        givenBag = 0;

    }

    @Override
    protected void rule() {
        //время прибытия в аэропорт
        cameTime = getDispatcher().getCurrentTime();
        leftTime = cameTime;
        //зарегистрировались в аэропорту
        airport.passengerCame();
        //если у нас был багаж ждем пока нам его не выдадут
        while (!isGetBag && bagCnt != 0) {
            isProcessed = false;
            //А не пойти ли нам покушать?           
            if (Math.random() >= 0.3) {
                //идем кушать
                System.out.println("Пассажир " + getNameForProtocol() + " идет кушать");
                holdForTime(eatTime.next());
                System.out.println("Пассажир " + getNameForProtocol() + " покушал");

            }
            //становимся в очередь за сумкой
            bagQueue.addLast(this);
            try {
                //ждем пока нас обслужат в очереди
                waitForCondition(new IWaitCondition() {

                    @Override
                    public boolean testCondition() {
                        return isProcessed;
                    }

                    @Override
                    public String toString() {
                        return "Ждет пока придет его очередь";
                    }
                });
            } catch (DispatcherFinishException ex) {
                System.out.println("Диспетчер завершил работу");
                leftTime = getDispatcher().getCurrentTime();
                return;
            }
            //bagQueue.remove(this);
        }

        //время покидания аэропорта
        leftTime = getDispatcher().getCurrentTime();
        airport.passengerLeft();
        System.out.println("Пассажир " + getNameForProtocol() + " покинул аеропорт");
    }

    //узнать время покидания аэропорта
    public double getStayTime() {
        return leftTime - cameTime;
    }

    //узнать время прибытия
    public double getCameTime() {
        return cameTime;
    }

    //вручить пасажиру сумку
    public boolean giveBag() {
        if (givenBag == bagCnt) {
            isGetBag = true;
        } else {
            givenBag++;
            isGetBag = false;
        }
        return isGetBag;
    }

    //Обрабатываем чувака
    public void setProcessed() {
        isProcessed = true;
    }

    //а не обработан ли я)
    public boolean isProcessed() {
        return isProcessed;
    }
}
