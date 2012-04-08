/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package actors;

import java.util.logging.Level;
import java.util.logging.Logger;
import process.Actor;
import process.DispatcherFinishException;
import process.IWaitCondition;
import queues.QueueForTransactions;
import queues.Store;
import rnd.Randomable;
import widgets.Diagram;

/**
 * Класс представляющий модель багажного отдела
 *
 * @author lordgal
 */
public class LuggageDept extends Actor {

    //генератор количества багажа у пассажира
    private static Randomable prodRandGenerator;
    //очередь пассажиров
    private static QueueForTransactions passengersQue;
    //хранилище багажа 
    private static Store bagInDeptCount;
    //максимальное количество багажа в отделении
    private static int maxBagInDeptCount;
    //время моделирования
    private static double modelingTime;

    //инициализация статических полей класса
    public static void _init(Randomable productiv, double workingTime, int maxBagCnt) {
        prodRandGenerator = productiv;
        modelingTime = workingTime;
        passengersQue = new QueueForTransactions();
        passengersQue.init();
        bagInDeptCount = new Store();
        bagInDeptCount.init();

        maxBagInDeptCount = maxBagCnt;
    }

    public LuggageDept(Diagram passengerQueDiag, Diagram bagStoreDiagram, double maxBagCnt) {

        passengersQue.setPainter(passengerQueDiag.getPainter());
        bagInDeptCount.setPainter(bagStoreDiagram.getPainter());

    }

    @Override
    protected void rule() {
        //условие появления багажа в отделении
        bagInDeptCount.setDispatcher(getDispatcher());
        IWaitCondition bagSize = new IWaitCondition() {

            @Override
            public boolean testCondition() {
                return bagInDeptCount.getSize() > 0;
            }

            @Override
            public String toString() {
                return ("В отделе нету багажа");

            }
        };

        //условия наличия пассажиров в очереди
        IWaitCondition isAnyPassenger = new IWaitCondition() {

            @Override
            public boolean testCondition() {
                return getPassengersQue().size() > 0;
            }

            @Override
            public String toString() {
                return ("В очереди нету пассажиров");


            }
        };
        while (getDispatcher().getCurrentTime() < modelingTime) {
            try {
                //ждем появления багажа
                System.out.println("Ждем багажа");
                waitForCondition(bagSize);
            } catch (DispatcherFinishException ex) {
                System.out.println("Диспетчер завершил работу");
                return;
            }
            try {
                //ждать появления пассаажиров
                System.out.println("Ждем пассажиров");
                waitForCondition(isAnyPassenger);
            } catch (DispatcherFinishException ex) {
                System.out.println("Диспетчер закончил работу");
                return;
            }
            //выбираем пассажира из очереди
            Passenger pass = (Passenger) (getPassengersQue().peekFirst());
            getDispatcher().printToProtocol(getNameForProtocol() + " обслуживает пассажира " + pass.getNameForProtocol());

            //а прибыл ли багаж нашего пассажира?
            if (Math.random() > 0.4) {
                while (bagInDeptCount.getSize() > 0 && !pass.giveBag()) {
                    bagInDeptCount.remove(1);
                }
            }
            //задерживаемся на время обработки пассажира
            holdForTime(prodRandGenerator.next());
            pass.setProcessed();
            getDispatcher().printToProtocol(getNameForProtocol() + " обслужил пассажира " + pass.getNameForProtocol());
            //выбрасываем пассажира из очереди
            getPassengersQue().remove(pass);
        }
    }

    public double getFreeBagSpace() {
        return maxBagInDeptCount - bagInDeptCount.getSize();
    }

    public boolean putBag() {
        if (getFreeBagSpace() > 0) {
            bagInDeptCount.add(1);
            return true;
        }
        return false;
    }

    public static QueueForTransactions getPassengersQue() {
        return passengersQue;
    }
}
