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
import simulation.queues.QueueForTransactions;
import simulation.rnd.Randomable;

/**
 *
 * @author lordgal
 */
public class Carrier extends Actor {
    
    //количество багажа в тележке
    private double bagCapacity;  
    //количество багажа  втележке
    private double realBagCap;
    //время путешествия тележки туда-обратно+погрузка/выгрузка
    private Randomable travelTime;  
    //моделька аеропорта
    private Airport airport;
    //багажное отделение
    private LuggageDept dept;
    //
    private Randomable productivity;
    //время моделирования
    private double modelingTime;
    //очередь на получение багажа в аеропорту
    //используется только для возможности отрисовки
    //т.к. самолетов много и все происходит паралельно тележки сами добавляют и 
    //удаляют себя из очереди
    private static QueueForTransactions loadQue;
    
    

    public Carrier(double bagCap, Randomable travTime, Airport port, Randomable product) {
        this.airport = port;
        this.travelTime = travTime;
        this.bagCapacity = bagCap;
        this.productivity = product;
    }

    @Override
    protected void rule() {
        //наличие багажа в аэропорту
        IWaitCondition isAnyBag = new IWaitCondition() {

            @Override
            public boolean testCondition() {
                return airport.getBagCountInAirport() > 0;
            }
        };
        
        //наличие свободного места в грузовом отделении
        IWaitCondition isAnyFreeSpace = new IWaitCondition() {

            @Override
            public boolean testCondition() {
                return dept.getFreeBagSpace() > 0;
            }
        };
        
        //моделирование пока время не закончится
        while (getDispatcher().getCurrentTime() < modelingTime) {
            //едем к самолету
            holdForTime(travelTime.next()); 
            try {
                waitForCondition(isAnyBag);
            } catch (DispatcherFinishException ex) {
                System.out.println("Диспетчер закончил работу");
            }
            //если в тележке есть еще место И в аеропорту еще есть багаж
            //то берем одну единицу багажа из аеропорта
            while (realBagCap != bagCapacity && airport.getBagFromAirport()) {
                realBagCap++;
            }
            
            //загружаемся и едем обратно 
            holdForTime(travelTime.next());
            //если есть пустое место в багажном отделении
            //то начинаем разгрузку иначе ждем
            while (true) {
                try {
                    waitForCondition(isAnyFreeSpace);
                } catch (DispatcherFinishException ex) {
                    System.out.println("Диспетчер закончил работу");
                }
                
                while (realBagCap > 0 && dept.putBag()) {
                    realBagCap--;
                    //мы же тратим время на разгрузку)
                    holdForTime(productivity.next());
                }
                
                //разгрузились?
                if(realBagCap == 0){
                    //уезжаем и едем к аеропорту
                    break;
                }
            }
        }



    }
}