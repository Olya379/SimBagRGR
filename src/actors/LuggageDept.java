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

    private static Randomable prodRandGenerator;
    public QueueForTransactions passengersQue;
    private Store bagInDeptCount;
    private int maxBagInDeptCount;
    private static double modelingTime;

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
                waitForCondition(bagSize);
            } catch (DispatcherFinishException ex) {
                Logger.getLogger(LuggageDept.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                waitForCondition(isAnyPassenger);
            } catch (DispatcherFinishException ex) {
                Logger.getLogger(LuggageDept.class.getName()).log(Level.SEVERE, null, ex);
            }
            Passenger pass = (Passenger) (passengersQue.peekFirst());
            getDispatcher().printToProtocol(getNameForProtocol() + " обслуживает пассажира " + pass.getNameForProtocol());

            if (Math.random() > 0.5) {
                while (bagInDeptCount.getSize() > 0 && !pass.giveBag()) {
                    bagInDeptCount.remove(1);
                }
            }

            holdForTime(prodRandGenerator.next());
            pass.setProcessed();
            getDispatcher().printToProtocol(getNameForProtocol() + " обслужил пассажира " + pass.getNameForProtocol());
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
