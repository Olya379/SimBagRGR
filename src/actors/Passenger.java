/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package actors;

import java.util.logging.Level;
import java.util.logging.Logger;
import simulation.process.Actor;
import simulation.process.Dispatcher;
import simulation.process.DispatcherFinishException;
import simulation.process.IWaitCondition;
import simulation.queues.QueueForTransactions;
import simulation.rnd.Randomable;

/**
 *
 * @author lordgal
 */
public class Passenger extends Actor {

    private double cameTime;
    private double leftTime;
    private Dispatcher dispatcher;
    private boolean isGetBag;
    private double bagCnt;
    private int givenBag;
    private boolean isProcessed;
    //private static Randomable think;
    private static Randomable eatTime;
    private static QueueForTransactions bagQueue;
    private static Airport airport;
    public static void _init(Randomable eat, QueueForTransactions bagQ, Airport port) {
        // think = brain;
        eatTime = eat;
        bagQueue = bagQ;
        
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
        cameTime = dispatcher.getCurrentTime();
        airport.passengerCame();
        while (!isGetBag) {
            isProcessed = false;
            if (Math.random() >= 0.5) {
                //идем кушать
                holdForTime(eatTime.next());
            }
            //становимся в очередь
            bagQueue.addLast(this);
            try {
                waitForCondition(new IWaitCondition() {

                    @Override
                    public boolean testCondition() {
                        return isProcessed;
                    }
                });
            } catch (DispatcherFinishException ex) {
                Logger.getLogger(Passenger.class.getName()).log(Level.SEVERE, null, ex);
            }
            bagQueue.remove(this);
        }

        //время покидания аэропорта
        leftTime = dispatcher.getCurrentTime();
        airport.passengerLeft();
    }

    public double getStayTime() {
        return leftTime - cameTime;
    }

    public double getCameTime() {
        return cameTime;
    }
    
    public boolean giveBag(){
        if(givenBag == bagCnt){ 
            isGetBag=true;
        }else{
            givenBag++;
            isGetBag = false;
        }         
         return isGetBag;
    }
    
    public void setProcessed(){
        isProcessed = true;
    }
}


