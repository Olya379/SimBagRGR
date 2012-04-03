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
import simulation.queues.Store;
import simulation.rnd.Randomable;
import simulation.widgets.Diagram;

/**
 * Класс представляющий модель багажного отдела
 *
 * @author lordgal
 */
public class LuggageDept extends Actor {

    //генератор количества багажа у пассажира
    private static Randomable prodRandGenerator;
    //очередь пассажиров
    public QueueForTransactions passengersQue;
    //хранилище багажа 
    private Store bagInDeptCount;
    //максимальное количество багажа в отделении
    private int maxBagInDeptCount;
    //время моделирования
    private static double modelingTime;

    //инициализация статических полей класса
    public static void _init(Randomable productiv, double workingTime) {
        prodRandGenerator = productiv;
        modelingTime = workingTime;
    }

    public LuggageDept(Diagram passengerQueDiag, Diagram bagStoreDiagram, double maxBagCnt) {

        passengersQue.setPainter(passengerQueDiag.getPainter());
        bagInDeptCount.setPainter(bagStoreDiagram.getPainter());

    }

    @Override
    protected void rule() {
        //условие появления багажа в отделении
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
                return passengersQue.size() > 0 && !((Passenger) (passengersQue.peekFirst())).isProcessed();
            }

            @Override
            public String toString() {
                return ("В очереди нету пассажиров");


            }
        };
        while (getDispatcher().getCurrentTime() < modelingTime) {
            try {
                //ждем появления багажа
                waitForCondition(bagSize);
            } catch (DispatcherFinishException ex) {
                Logger.getLogger(LuggageDept.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                //ждать появления пассаажиров
                waitForCondition(isAnyPassenger);
            } catch (DispatcherFinishException ex) {
                Logger.getLogger(LuggageDept.class.getName()).log(Level.SEVERE, null, ex);
            }
            //выбираем пассажира из очереди
            Passenger pass = (Passenger) (passengersQue.peekFirst());
            getDispatcher().printToProtocol(getNameForProtocol() + " обслуживает пассажира " + pass.getNameForProtocol());
            
            //а прибыл ли багаж нашего пассажира?
            if (Math.random() > 0.5) {
                while (bagInDeptCount.getSize() > 0 && !pass.giveBag()) {
                    bagInDeptCount.remove(1);
                }
            }
            //задерживаемся на время обработки пассажира
            holdForTime(prodRandGenerator.next());
            pass.setProcessed();
            getDispatcher().printToProtocol(getNameForProtocol() + " обслужил пассажира " + pass.getNameForProtocol());
            //выбрасываем пассажира из очереди
            passengersQue.remove(pass);
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
}
