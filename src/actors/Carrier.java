/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package actors;

import java.util.logging.Level;
import java.util.logging.Logger;
import simulation.process.Actor;
import simulation.process.DispatcherFinishException;
import simulation.process.IWaitCondition;
import simulation.rnd.Randomable;

/**
 *
 * @author lordgal
 */
public class Carrier extends Actor {

    private double bagCapacity;        //количество багажа в тележке
    private double realBagCap;
    private Randomable travelTime;  //время путешествия тележки туда-обратно+погрузка/выгрузка
    private Airport airport;        //
    private LuggageDept dept;
    private Randomable productivity;
    private double modelingTime;

    public Carrier(double bagCap, Randomable travTime, Airport port, Randomable product) {
        this.airport = port;
        this.travelTime = travTime;
        this.bagCapacity = bagCap;
        this.productivity = product;
    }

    @Override
    protected void rule() {
        IWaitCondition isAnyBag = new IWaitCondition() {

            @Override
            public boolean testCondition() {
                return airport.getBagCountInAirport() > 0;
            }
        };

        IWaitCondition isAnyFreeSpace = new IWaitCondition() {

            @Override
            public boolean testCondition() {
                return dept.getFreeBagSpace() > 0;
            }
        };

        while (getDispatcher().getCurrentTime() < modelingTime) {
            holdForTime(travelTime.next()); //едем к самолету
            try {
                waitForCondition(isAnyBag);
            } catch (DispatcherFinishException ex) {
                System.out.println("Диспетчер закончил работу");
            }
            while (realBagCap != bagCapacity && airport.getBagFromAirport()) {
                realBagCap++;
            }
            holdForTime(travelTime.next()); //загружаемся и едем обратно 
            while (true) {
                try {
                    waitForCondition(isAnyFreeSpace);
                } catch (DispatcherFinishException ex) {
                    System.out.println("Диспетчер закончил работу");
                }

                while (realBagCap > 0 && dept.putBag()) {
                    realBagCap--;
                }
                if(realBagCap == 0){
                    break;
                }
            }
        }



    }
}