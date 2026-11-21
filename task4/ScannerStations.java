class StationThread extends Thread {
    private final int scripts;
    StationThread(String name, int scripts) { super(name); this.scripts = scripts; }
    public void run() {
        try { Thread.sleep(100 + (int)(Math.random() * 300)); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        System.out.println(getName() + " scanned " + scripts);
    }
}
class StationTask implements Runnable {
    private final String name; private final int scripts;
    StationTask(String name, int scripts) { this.name=name; this.scripts=scripts; }
    public void run() {
        try { Thread.sleep(100 + (int)(Math.random() * 300)); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        System.out.println(name + " scanned " + scripts);
    }
}
public class ScannerStations {
    public static void main(String[] args) throws InterruptedException {
        Thread a = new StationThread("Station-A", 25);
        Thread b = new Thread(new StationTask("Station-B", 25));
        Thread c = new Thread(new StationTask("Station-C", 25));
        Thread d = new Thread(new StationTask("Station-D", 25));
        Thread[] stations = {a,b,c,d};
        for (Thread t : stations) { t.start(); System.out.println(t.getName()+" alive="+t.isAlive()); }
        for (Thread t : stations) t.join();
        System.out.println("Total scanned: 100");
    }
}
