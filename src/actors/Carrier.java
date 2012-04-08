/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package actors;

import process.Actor;
import process.DispatcherFinishException;
import process.IWaitCondition;
import rnd.Randomable;

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
    private static Randomable travelTime;
    //моделька аеропорта
    private static Airport airport;
    //багажное отделение
    private static LuggageDept dept;
    //время которое тратится на 
    private static Randomable productivity;
    //время моделирования
    private static double modelingTime;


    public static void _init(Randomable travTime, Airport port, Randomable product, int modelTime, LuggageDept dpt) {
        airport = port;
        travelTime = travTime;
        productivity = product;
        modelingTime = modelTime;
        dept = dpt;
    }

    public Carrier(double bagCap) {

        this.bagCapacity = bagCap;
        this.realBagCap = 0;

    }

    @Override
    protected void rule() {
        //наличие багажа в аэропорту
        IWaitCondition isAnyBag = new IWaitCondition() {

            @Override
            public boolean testCondition() {
                return airport.getBagCountInAirport() > 0;
            }
            
            @Override
            public String toString(){
                return "Ждем появления багажа в аэропорту";
            }
        };

        //наличие свободного места в грузовом отделении
        IWaitCondition isAnyFreeSpace = new IWaitCondition() {

            @Override
            public boolean testCondition() {
                return dept.getFreeBagSpace() > 0;
            }
            
             @Override
            public String toString(){
                return "Ждем появления свободного места в грузовом отделении";
            }
        };

        //моделирование пока время не закончится
        while (getDispatcher().getCurrentTime() < modelingTime) {
            //едем к самолету
            System.out.println("Тележка " + getNameForProtocol() + " едет к самолету");
            holdForTime(travelTime.next());
            try {
                waitForCondition(isAnyBag);
            } catch (DispatcherFinishException ex) {
                System.out.println("Диспетчер закончил работу");
            }
            //если в тележке есть еще место И в аеропорту еще есть багаж
            //то берем одну единицу багажа из аеропорта
            while (realBagCap != bagCapacity && airport.getBagFromAirport()) {
                System.out.println("Тедежка " + getNameForProtocol() + " загружает одну единицу багажа");
                realBagCap++;
                //мы же тратим время на загрузку)
                holdForTime(productivity.next());
            }

            //загружаемся и едем обратно 
            System.out.println("Тедежка " + getNameForProtocol() + " едет в отделение для выгрузки");
            holdForTime(travelTime.next());
            //если есть пустое место в багажном отделении
            //то начинаем разгрузку иначе ждем
            while (true) {
                try {
                    System.out.println("Тележка " + getNameForProtocol() + " ждет пока освободится место в отделении");
                    waitForCondition(isAnyFreeSpace);
                } catch (DispatcherFinishException ex) {
                    System.out.println("Диспетчер закончил работу");
                    return;
                }

                while (realBagCap > 0 && dept.putBag()) {
                    System.out.println("Тедежка " + getNameForProtocol() + " выгружает одну единицу багажа");
                    realBagCap--;
                    //мы же тратим время на разгрузку)
                    holdForTime(productivity.next());
                }

                //разгрузились?
                if (realBagCap == 0) {
                    //уезжаем и едем к аеропорту
                    break;
                }
            }
        }



    }
}