package se.inera.certificate.overvakning;

public class StopWatch {

    private enum StopWatchState {
        STOPPED,
        STARTED;
    }
    
    private StopWatchState state = StopWatchState.STOPPED;
    
    private long startTime = 0;
    private long stopTime = 0;
    
    public void start() {
        
        if (state.equals(StopWatchState.STARTED)) {
            throw new IllegalStateException("Already running");
        }
        
        this.startTime = System.currentTimeMillis();
        this.state = StopWatchState.STARTED;
        
    }
    
    public void stop() {
        
        if (!state.equals(StopWatchState.STARTED)) {
            throw new IllegalStateException("Must be running to be stopped");
        }
        
        this.stopTime = System.currentTimeMillis();
        this.state = StopWatchState.STOPPED;
    }
    
    public long getTime() {
        
        if (!state.equals(StopWatchState.STOPPED)) {
            throw new IllegalStateException("Must be stopped to get time");
        }
        
        return (stopTime - startTime);
    }
    
    public void reset() {
        
        if (!state.equals(StopWatchState.STOPPED)) {
            this.state = StopWatchState.STOPPED;
        }
        
        this.startTime = 0;
        this.stopTime = 0;
        
    }
}
