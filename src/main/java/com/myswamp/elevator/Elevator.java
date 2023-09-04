package com.myswamp.elevator;


import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

enum State {
    IDLE, UP, DOWN, STOPPED
}

enum Direction {
    UPWARD, DOWNWARD
}
public class Elevator {


    private State state = State.STOPPED;
    private int bottomFloor;
    private int topFloor;
    private int currentFloor = 0;
    private BlockingQueue<Command> upwardQueue;
    private BlockingQueue<Command> downwardQueue;

    Elevator(int bottomFloor, int topFloor) {
        this.bottomFloor = bottomFloor;
        this.topFloor = topFloor;
        this.upwardQueue = new PriorityBlockingQueue<>(topFloor - bottomFloor,
                Comparator.comparingInt(c -> (c.getToFloor() - c.getAtFloor())));
        this.downwardQueue = new PriorityBlockingQueue<>(topFloor - bottomFloor,
                Comparator.comparingInt(c -> (c.getAtFloor() - c.getToFloor())));
    }

    public State getState() {
        return state;
    }

    public int getBottomFloor() {
        return bottomFloor;
    }

    public int getTopFloor() {
        return topFloor;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void start() throws InterruptedException {
        assert this.state == State.STOPPED;
        this.state = State.IDLE;

        while (true) {
            processCommands();

            if (this.state == State.STOPPED)
                break;
        }
    }

    public void stop() {
        assert this.currentFloor == 0;
        this.state = State.STOPPED;
    }

    public void issueCommand(Command command) {
        if(command instanceof OpenDoorCommand openDoorCommand) {
            onReceiveOpenDoorCommand(openDoorCommand);
        } else if(command instanceof MoveCommand moveCommand) {
            onReceiveMoveCommand(moveCommand);
        }
    }

    private void onReceiveOpenDoorCommand(OpenDoorCommand command) {
        if(command.getDirection() == Direction.UPWARD) {
            upwardQueue.offer(command);
        } else if(command.getDirection() == Direction.DOWNWARD) {
            downwardQueue.offer(command);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void onReceiveMoveCommand(MoveCommand command) {
        if(currentFloor > command.getToFloor()) {
            downwardQueue.offer(command);
        } else if(currentFloor < command.getToFloor()) {
            upwardQueue.offer(command);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void processCommands() throws InterruptedException {
        if(state == State.IDLE) {
            processDownwardsCommands();
            processUpwardCommands();
        } else if(state == State.UP) {
            processUpwardCommands();
        } else {
            processDownwardsCommands();
        }
        if (currentFloor == 0) {
            state = State.IDLE;
        } else if(currentFloor > 0) {
            state = State.DOWN;
        } else {
            state = State.UP;
        }
    }

    private void processDownwardsCommands() throws InterruptedException {
        while (!downwardQueue.isEmpty()) {
            state = State.DOWN;
            Command command = downwardQueue.poll();
            int toFloor = command.getToFloor();
            while(currentFloor != toFloor) {
                Thread.sleep(100);
                currentFloor -= 1;

                command = downwardQueue.poll();
                if(command != null) {
                    if(command.getToFloor() < toFloor) {
                        toFloor = command.getToFloor();
                    }
                }
            }
        }
    }

    private void processUpwardCommands() throws InterruptedException {
        while (!upwardQueue.isEmpty()) {
            state = State.UP;
            Command command = upwardQueue.poll();
            int toFloor = command.getToFloor();
            while(currentFloor != toFloor) {
                Thread.sleep(100);
                currentFloor += 1;

                command = upwardQueue.poll();
                if(command != null) {
                    if(command.getToFloor() > toFloor) {
                        toFloor = command.getToFloor();
                    }
                }
            }
        }
    }

}

abstract class Command {
    protected int atFloor;
    protected int toFloor;
    public int getAtFloor() {
        return atFloor;
    }

    public int getToFloor() {
        return toFloor;
    }

}

class OpenDoorCommand extends Command {
    private Direction direction;
    OpenDoorCommand(int atFloor, int toFloor, Direction direction) {
        this.atFloor = atFloor;
        this.toFloor = toFloor;
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }
}

class MoveCommand extends Command {
    MoveCommand(int atFloor, int toFloor) {
        this.atFloor = atFloor;
        this.toFloor = toFloor;
    }

}
